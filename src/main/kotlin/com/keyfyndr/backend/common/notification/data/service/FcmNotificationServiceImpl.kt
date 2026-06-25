package com.keyfyndr.backend.common.notification.data.service

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import com.keyfyndr.backend.common.notification.service.FcmNotificationService
import com.keyfyndr.backend.features.auth.domain.repository.DeviceTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * FCM implementation of [FcmNotificationService].
 *
 * Key design decisions:
 * - Sends a DATA-ONLY message (no top-level `notification` block).
 *   This gives the Android client full control: when the chat screen is open
 *   for that sender, the app silently ignores the push without showing a banner.
 *   When backgrounded/killed, the Firebase service builds and shows the notification.
 * - Automatically cleans up stale (UNREGISTERED / INVALID_ARGUMENT) tokens from the DB
 *   to avoid accumulating dead tokens over time.
 * - Gracefully no-ops if Firebase was not successfully initialized (e.g., missing credentials).
 */
@Service
class FcmNotificationServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository
) : FcmNotificationService {

    private val logger = LoggerFactory.getLogger(FcmNotificationServiceImpl::class.java)

    override fun sendChatMessageNotification(
        receiverId: UUID,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        // Guard: Firebase may not be initialized (e.g., missing service-account.json in dev)
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase not initialized. Skipping push notification for user: $receiverId")
            return
        }

        val tokens = deviceTokenRepository.findByUserId(receiverId)
            .map { it.deviceToken }
            .filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            logger.debug("No device tokens found for user: $receiverId. Skipping FCM.")
            return
        }

        // Merge caller-supplied data with title/body so the Android service
        // can build a rich notification from the data payload alone.
        val fullData = buildMap<String, String> {
            put("title", title)
            put("body", body)
            putAll(data)
        }

        try {
            val message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(fullData)
                .build()

            val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)
            logger.info(
                "FCM multicast to user $receiverId: ${response.successCount} sent, " +
                    "${response.failureCount} failed out of ${tokens.size} token(s)."
            )

            // Collect stale tokens to clean up
            val staleTokens = mutableListOf<String>()
            response.responses.forEachIndexed { index, sendResponse ->
                if (!sendResponse.isSuccessful) {
                    val errorCode = (sendResponse.exception as? FirebaseMessagingException)
                        ?.messagingErrorCode
                    val token = tokens[index]
                    logger.warn("FCM failed for token [$token]: ${sendResponse.exception?.message}")

                    if (errorCode == MessagingErrorCode.UNREGISTERED ||
                        errorCode == MessagingErrorCode.INVALID_ARGUMENT
                    ) {
                        staleTokens.add(token)
                    }
                }
            }

            if (staleTokens.isNotEmpty()) {
                logger.info("Removing ${staleTokens.size} stale FCM token(s) for user: $receiverId")
                deviceTokenRepository.deleteByTokens(staleTokens)
            }
        } catch (e: Exception) {
            logger.error("Unexpected error sending FCM notification to user $receiverId: ${e.message}", e)
        }
    }
}
