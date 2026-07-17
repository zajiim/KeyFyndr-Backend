package com.keyfyndr.backend.features.home.domain.usecase

import com.keyfyndr.backend.common.exception.ResourceNotFoundException
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import com.keyfyndr.backend.features.home.domain.model.*
import com.keyfyndr.backend.features.home.domain.provider.NearbyDataProvider
import com.keyfyndr.backend.features.home.domain.provider.NotificationCountProvider
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Assembles the Home Dashboard by aggregating data from multiple features.
 *
 * Data sources:
 * 1. UserRepository      → user summary (display name, profile image)
 * 2. KeyRepository        → latest 5 active keys preview
 * 3. NearbyDataProvider   → nearby lost/found summary + activity feed
 * 4. ChatMessageRepository → unread chat count
 * 5. NotificationCountProvider → unread notification count (stub until feature exists)
 *
 * Design decisions:
 * - Each data slice is fetched independently so a failure in one doesn't block others
 * - The use case does NOT own any data — it's pure aggregation
 * - User's current lat/lng are passed through for distance calculations
 * - Limited to 5 keys and 10 nearby activities to minimize payload size for mobile
 */
@Component
class GetHomeDashboardUseCase(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val nearbyDataProvider: NearbyDataProvider,
    private val chatMessageRepository: ChatMessageRepository,
    private val notificationCountProvider: NotificationCountProvider
) {

    companion object {
        /** Max keys shown in the home preview carousel */
        private const val KEYS_PREVIEW_LIMIT = 5

        /** Max nearby activities shown in the activity feed */
        private const val NEARBY_ACTIVITIES_LIMIT = 10
    }

    fun execute(userId: UUID, userLatitude: Double?, userLongitude: Double?, theme: String = "light"): HomeDashboard {
        // 1. User summary
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found with id: $userId")

        val userSummary = UserSummary(
            displayName = user.name,
            profileImage = null // User model doesn't have profileImage yet
        )

        // 2. Latest active keys preview
        val latestKeys = keyRepository.findLatestActiveByOwnerId(userId, KEYS_PREVIEW_LIMIT)
        val keysPreview = latestKeys.map { key ->
            KeyPreview(
                id = key.id.toString(),
                title = key.title,
                imageUrl = key.imageUrl,
                status = key.status
            )
        }

        // 3. Nearby summary + activities
        val nearbySummary = nearbyDataProvider.getNearbySummary(userId, userLatitude, userLongitude, theme)
        val recentActivities = nearbyDataProvider.getRecentNearbyActivities(
            userId, userLatitude, userLongitude, NEARBY_ACTIVITIES_LIMIT
        )

        // 4. Unread counts
        val unreadChats = chatMessageRepository.countUnreadMessages(userId)
        val unreadNotifications = notificationCountProvider.countUnread(userId)

        val unreadCounts = UnreadCounts(
            unreadChats = unreadChats,
            unreadNotifications = unreadNotifications
        )

        return HomeDashboard(
            userSummary = userSummary,
            keysPreview = keysPreview,
            nearbySummary = nearbySummary,
            recentNearbyActivities = recentActivities,
            unreadCounts = unreadCounts
        )
    }
}
