package com.keyfyndr.backend.features.home.domain.provider

import com.keyfyndr.backend.features.home.domain.model.NearbyActivity
import com.keyfyndr.backend.features.home.domain.model.NearbySummary
import java.util.UUID

/**
 * Abstraction for fetching nearby lost/found data for the Home Dashboard.
 *
 * The default implementation uses KeyRepository to find keys with location data.
 * When a dedicated LostReport/FoundReport feature is built in the future,
 * a new implementation can replace this via @Primary or @ConditionalOnMissingBean
 * without any changes to the API contract or use case layer.
 */
interface NearbyDataProvider {

    /**
     * Builds the nearby summary (counts, map center, markers, nearest distance, map preview path)
     * relative to the user's current position.
     *
     * @param userId  The authenticated user's ID
     * @param userLat The user's current latitude (null if location unavailable)
     * @param userLng The user's current longitude (null if location unavailable)
     * @param theme   Map visual theme: "light" or "dark" (default "light")
     */
    fun getNearbySummary(userId: UUID, userLat: Double?, userLng: Double?, theme: String = "light"): NearbySummary

    /**
     * Returns the latest nearby reports ordered by a combination of distance and recency.
     *
     * @param userId  The authenticated user's ID
     * @param userLat The user's current latitude (null if location unavailable)
     * @param userLng The user's current longitude (null if location unavailable)
     * @param limit   Maximum number of activities to return
     */
    fun getRecentNearbyActivities(userId: UUID, userLat: Double?, userLng: Double?, limit: Int): List<NearbyActivity>
}
