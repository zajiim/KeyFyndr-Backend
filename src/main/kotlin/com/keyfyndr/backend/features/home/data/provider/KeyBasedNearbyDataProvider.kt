package com.keyfyndr.backend.features.home.data.provider

import com.keyfyndr.backend.common.config.MapboxProperties
import com.keyfyndr.backend.features.home.domain.model.NearbyActivity
import com.keyfyndr.backend.features.home.domain.model.NearbyMarker
import com.keyfyndr.backend.features.home.domain.model.NearbySummary
import com.keyfyndr.backend.features.home.domain.provider.NearbyDataProvider
import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import com.keyfyndr.backend.features.key.domain.model.Key
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID
import kotlin.math.*

/**
 * NearbyDataProvider implementation backed by the Key feature's location data.
 *
 * Queries keys with LOST or FOUND status that have lat/lng coordinates,
 * filters to those within [MapboxProperties.nearbyRadiusMeters] of the user,
 * calculates distances using the Haversine formula, and builds the
 * summary/activity feed for the Home Dashboard.
 *
 * The [mapPreviewPath] field points to the backend's own map-preview proxy endpoint
 * (`/api/v1/home/map-preview`). The mobile client loads that path as an image
 * with a JWT Authorization header — the Mapbox token is never exposed to clients.
 *
 * When a dedicated LostReport/FoundReport feature is built, create a new
 * @Primary implementation to override this one — the API contract stays the same.
 */
@Component
class KeyBasedNearbyDataProvider(
    private val keyRepository: KeyRepository,
    private val mapboxProps: MapboxProperties
) : NearbyDataProvider {

    companion object {
        /** Earth's radius in meters for Haversine distance calculation */
        private const val EARTH_RADIUS_METERS = 6_371_000.0
    }

    override fun getNearbySummary(
        userId: UUID,
        userLat: Double?,
        userLng: Double?,
        theme: String
    ): NearbySummary {
        val allKeys = keyRepository.findAllByStatusInAndLocationNotNull(
            listOf(KeyStatus.LOST, KeyStatus.FOUND)
        )

        // Filter to keys within the configured radius when user location is available.
        // When location is unknown, fall back to all keys so the map is still useful.
        val nearbyKeys = if (userLat != null && userLng != null) {
            allKeys.filter { key ->
                haversineMeters(userLat, userLng, key.latitude!!, key.longitude!!) <= mapboxProps.nearbyRadiusMeters
            }
        } else {
            allKeys
        }

        val lostCount = nearbyKeys.count { it.status == KeyStatus.LOST }
        val foundCount = nearbyKeys.count { it.status == KeyStatus.FOUND }

        val markers = nearbyKeys.map { key ->
            NearbyMarker(
                latitude = key.latitude!!,
                longitude = key.longitude!!,
                type = key.status.name
            )
        }

        // Map center: user's location if available, otherwise centroid of all nearby markers
        val mapCenterLat: Double?
        val mapCenterLng: Double?
        if (userLat != null && userLng != null) {
            mapCenterLat = userLat
            mapCenterLng = userLng
        } else if (markers.isNotEmpty()) {
            mapCenterLat = markers.map { it.latitude }.average()
            mapCenterLng = markers.map { it.longitude }.average()
        } else {
            mapCenterLat = null
            mapCenterLng = null
        }

        // Nearest activity distance (only meaningful if user location is known)
        val nearestDistance = if (userLat != null && userLng != null && nearbyKeys.isNotEmpty()) {
            nearbyKeys.minOfOrNull { haversineMeters(userLat, userLng, it.latitude!!, it.longitude!!) }
        } else {
            null
        }

        // Build the relative path to our own map-preview proxy endpoint.
        // The Mapbox token is added server-side by MapboxStaticMapService — never here.
        val mapPreviewPath = buildMapPreviewPath(userLat, userLng, theme)

        return NearbySummary(
            totalLostReports = lostCount,
            totalFoundReports = foundCount,
            mapCenterLatitude = mapCenterLat,
            mapCenterLongitude = mapCenterLng,
            markers = markers,
            nearestActivityDistanceMeters = nearestDistance,
            mapPreviewPath = mapPreviewPath
        )
    }

    override fun getRecentNearbyActivities(
        userId: UUID,
        userLat: Double?,
        userLng: Double?,
        limit: Int
    ): List<NearbyActivity> {
        val allKeys = keyRepository.findAllByStatusInAndLocationNotNull(
            listOf(KeyStatus.LOST, KeyStatus.FOUND)
        )

        // Apply same radius filter as getNearbySummary
        val nearbyKeys = if (userLat != null && userLng != null) {
            allKeys.filter { key ->
                haversineMeters(userLat, userLng, key.latitude!!, key.longitude!!) <= mapboxProps.nearbyRadiusMeters
            }
        } else {
            allKeys
        }

        return nearbyKeys
            .map { key ->
                val distance = if (userLat != null && userLng != null) {
                    haversineMeters(userLat, userLng, key.latitude!!, key.longitude!!)
                } else {
                    0.0
                }
                NearbyActivityWithScore(key, distance)
            }
            // Sort by distance ascending (closest first)
            .sortedWith(compareBy { it.distanceMeters })
            .take(limit)
            .map { it.toNearbyActivity() }
    }

    /**
     * Builds a relative URL to the backend's map-preview proxy endpoint.
     * Returns null when location is unavailable (map can't be centered without it).
     *
     * Example: `/api/v1/home/map-preview?lat=12.971600&lng=77.594600&theme=dark`
     */
    private fun buildMapPreviewPath(userLat: Double?, userLng: Double?, theme: String): String? {
        if (userLat == null || userLng == null) return null
        return UriComponentsBuilder
            .fromPath("/api/v1/home/map-preview")
            .queryParam("lat", "%.6f".format(userLat))
            .queryParam("lng", "%.6f".format(userLng))
            .queryParam("theme", theme)
            .build()
            .toUriString()
    }

    /**
     * Haversine formula — calculates the great-circle distance in meters
     * between two WGS84 coordinate pairs.
     */
    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Internal helper pairing a key with its calculated distance,
     * used for sorting before mapping to the final NearbyActivity model.
     */
    private data class NearbyActivityWithScore(
        val key: Key,
        val distanceMeters: Double
    ) {
        fun toNearbyActivity(): NearbyActivity = NearbyActivity(
            id = key.id.toString(),
            type = key.status.name,
            title = key.title,
            distanceMeters = distanceMeters,
            reportedAt = key.lastStatusUpdateAt ?: key.createdAt
        )
    }
}

