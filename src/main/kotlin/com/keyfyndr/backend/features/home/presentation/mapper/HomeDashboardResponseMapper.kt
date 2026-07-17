package com.keyfyndr.backend.features.home.presentation.mapper

import com.keyfyndr.backend.features.home.domain.model.HomeDashboard
import com.keyfyndr.backend.features.home.presentation.response.*

/**
 * Presentation-layer mapper: [HomeDashboard] domain model → [HomeDashboardResponse] DTO.
 *
 * Follows the same extension-function pattern used across the project
 * (e.g., Key.toResponse(), ChatMessage.toResponse()).
 */
fun HomeDashboard.toResponse(): HomeDashboardResponse = HomeDashboardResponse(
    userSummary = UserSummaryResponse(
        displayName = this.userSummary.displayName,
        profileImage = this.userSummary.profileImage
    ),
    keysPreview = this.keysPreview.map { key ->
        KeyPreviewResponse(
            id = key.id,
            title = key.title,
            imageUrl = key.imageUrl,
            status = key.status
        )
    },
    nearbySummary = NearbySummaryResponse(
        totalLostReports = this.nearbySummary.totalLostReports,
        totalFoundReports = this.nearbySummary.totalFoundReports,
        mapCenterLatitude = this.nearbySummary.mapCenterLatitude,
        mapCenterLongitude = this.nearbySummary.mapCenterLongitude,
        markers = this.nearbySummary.markers.map { marker ->
            NearbyMarkerResponse(
                latitude = marker.latitude,
                longitude = marker.longitude,
                type = marker.type
            )
        },
        nearestActivityDistanceMeters = this.nearbySummary.nearestActivityDistanceMeters,
        mapPreviewPath = this.nearbySummary.mapPreviewPath
    ),
    recentNearbyActivities = this.recentNearbyActivities.map { activity ->
        NearbyActivityResponse(
            id = activity.id,
            type = activity.type,
            title = activity.title,
            distanceMeters = activity.distanceMeters,
            reportedAt = activity.reportedAt
        )
    },
    unreadCounts = UnreadCountsResponse(
        unreadChats = this.unreadCounts.unreadChats,
        unreadNotifications = this.unreadCounts.unreadNotifications
    )
)
