package com.keyfyndr.backend.features.home.domain.model

import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import java.time.Instant

/**
 * Domain model representing the aggregated Home Dashboard data.
 *
 * This is a read-only composite model — the Home feature owns no data.
 * It aggregates slices from Auth (user), Key (keys + nearby), and Chat (unread counts).
 */
data class HomeDashboard(
    val userSummary: UserSummary,
    val keysPreview: List<KeyPreview>,
    val nearbySummary: NearbySummary,
    val recentNearbyActivities: List<NearbyActivity>,
    val unreadCounts: UnreadCounts
)

/**
 * Display name and profile image for the top of the home screen.
 * profileImage is nullable until the User model supports it.
 */
data class UserSummary(
    val displayName: String,
    val profileImage: String?
)

/**
 * Lightweight key preview for the "My Keys" carousel on the home screen.
 * Intentionally minimal — the full key details are available via GET /api/v1/keys/me.
 */
data class KeyPreview(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val status: KeyStatus
)

/**
 * Summary of nearby lost/found activity for the map section.
 *
 * All coordinates are in WGS84 (standard GPS).
 * [mapPreviewPath] is a relative path to the backend's map-preview proxy endpoint
 * (e.g. `/api/v1/home/map-preview?lat=12.97&lng=77.59&theme=dark`).
 * The mobile client loads this path (prefixed with the API base URL) as an image
 * using a JWT-authenticated image request — the Mapbox token is never exposed.
 */
data class NearbySummary(
    val totalLostReports: Int,
    val totalFoundReports: Int,
    val mapCenterLatitude: Double?,
    val mapCenterLongitude: Double?,
    val markers: List<NearbyMarker>,
    val nearestActivityDistanceMeters: Double?,
    /** Relative URL to the backend map-preview proxy. Null when token is unconfigured or location is unavailable. */
    val mapPreviewPath: String?
)

/**
 * A single pin on the nearby map.
 * type is "LOST" or "FOUND" — the mobile app decides the pin color/icon.
 */
data class NearbyMarker(
    val latitude: Double,
    val longitude: Double,
    val type: String
)

/**
 * A recent lost/found report shown in the activity feed below the map.
 * Ordered by a combination of distance and recency.
 */
data class NearbyActivity(
    val id: String,
    val type: String,
    val title: String,
    val distanceMeters: Double,
    val reportedAt: Instant
)

/**
 * Badge counts shown on the home screen.
 * unreadNotifications returns 0 until a Notification feature is implemented.
 */
data class UnreadCounts(
    val unreadChats: Int,
    val unreadNotifications: Int
)
