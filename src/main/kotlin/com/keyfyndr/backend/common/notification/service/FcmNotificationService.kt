package com.keyfyndr.backend.common.notification.service

import java.util.UUID

/**
 * Domain service interface for sending push notifications via FCM.
 *
 * Abstracting behind an interface allows easy stubbing in tests
 * and potential future support for other push providers.
 */
interface FcmNotificationService {

    /**
     * Sends a FCM data-only push notification to all registered devices
     * of the given receiver.
     *
     * @param receiverId  The UUID of the user who should receive the notification.
     * @param title       Notification title shown in the system tray.
     * @param body        Notification body text (typically the message content).
     * @param data        Additional key-value payload (e.g., type, senderId, conversationId).
     */
    fun sendChatMessageNotification(
        receiverId: UUID,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    )
}
