package com.keyfyndr.backend.features.home.data.provider

import com.keyfyndr.backend.features.home.domain.provider.NotificationCountProvider
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Stub implementation of [NotificationCountProvider].
 *
 * Always returns 0 until a Notification feature is implemented.
 * When a real NotificationRepository exists, create a new @Primary bean
 * implementing [NotificationCountProvider] — this stub will be superseded
 * automatically without any use case or API changes.
 */
@Component
class StubNotificationCountProvider : NotificationCountProvider {

    override fun countUnread(userId: UUID): Int = 0
}
