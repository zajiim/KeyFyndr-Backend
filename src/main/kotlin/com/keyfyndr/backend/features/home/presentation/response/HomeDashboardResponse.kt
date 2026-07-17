package com.keyfyndr.backend.features.home.presentation.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import java.time.Instant

/**
 * Top-level response DTO for GET /api/v1/home.
 *
 * Single payload optimized for the mobile Home screen — the client
 * makes one request and gets all 5 dashboard sections in one response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class HomeDashboardResponse(
    val userSummary: UserSummaryResponse,
    val keysPreview: List<KeyPreviewResponse>,
    val nearbySummary: NearbySummaryResponse,
    val recentNearbyActivities: List<NearbyActivityResponse>,
    val unreadCounts: UnreadCountsResponse
)

data class UserSummaryResponse(
    val displayName: String,
    val profileImage: String?
)

data class KeyPreviewResponse(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val status: KeyStatus
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NearbySummaryResponse(
    val totalLostReports: Int,
    val totalFoundReports: Int,
    val mapCenterLatitude: Double?,
    val mapCenterLongitude: Double?,
    val markers: List<NearbyMarkerResponse>,
    val nearestActivityDistanceMeters: Double?,
    /**
     * Relative path to the backend map-preview proxy endpoint.
     * The mobile client prepends the API base URL and loads this as an image
     * with an `Authorization: Bearer <jwt>` header.
     * Null when the Mapbox token is unconfigured or the user's location is unknown.
     */
    val mapPreviewPath: String?
)

data class NearbyMarkerResponse(
    val latitude: Double,
    val longitude: Double,
    val type: String
)

data class NearbyActivityResponse(
    val id: String,
    val type: String,
    val title: String,
    val distanceMeters: Double,
    val reportedAt: Instant
)

data class UnreadCountsResponse(
    val unreadChats: Int,
    val unreadNotifications: Int
)
