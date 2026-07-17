package com.keyfyndr.backend.features.home.domain.provider

import java.util.UUID

/**
 * Abstraction for fetching unread notification counts for the Home Dashboard.
 *
 * Returns 0 until a Notification feature is implemented.
 * The future implementation simply needs to provide a @Primary bean
 * implementing this interface — no API or use case changes required.
 */
interface NotificationCountProvider {

    /**
     * Returns the total number of unread notifications for the user.
     */
    fun countUnread(userId: UUID): Int
}
