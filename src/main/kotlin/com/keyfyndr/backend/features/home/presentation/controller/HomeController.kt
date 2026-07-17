package com.keyfyndr.backend.features.home.presentation.controller

import com.keyfyndr.backend.common.ratelimit.MapRateLimiter
import com.keyfyndr.backend.common.response.ApiResponse
import com.keyfyndr.backend.features.home.data.service.MapboxStaticMapService
import com.keyfyndr.backend.features.home.domain.model.NearbyMarker
import com.keyfyndr.backend.features.home.domain.provider.NearbyDataProvider
import com.keyfyndr.backend.features.home.domain.usecase.GetHomeDashboardUseCase
import com.keyfyndr.backend.features.home.presentation.mapper.toResponse
import com.keyfyndr.backend.features.home.presentation.response.HomeDashboardResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller for the Home Dashboard feature.
 *
 * Architecture decisions:
 * - Single endpoint returning all dashboard sections in one response
 * - Thin controller — all aggregation logic lives in [GetHomeDashboardUseCase]
 * - User's current lat/lng are optional query params for distance calculations
 * - JWT required — userId extracted from the Authentication principal
 *
 * Mobile optimization:
 * - One network request replaces what would be 5+ separate calls
 * - Payload is compact: 5 key previews, 10 nearby activities, badge counts
 *
 * Map security:
 * - The dashboard response contains [HomeDashboardResponse.nearbySummary.mapPreviewPath],
 *   a relative path pointing to [getMapPreview].
 * - The mobile app loads that path as an image with its JWT header — the Mapbox
 *   access token is never included in any JSON response, only used server-side.
 */
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val getHomeDashboardUseCase: GetHomeDashboardUseCase,
    private val nearbyDataProvider: NearbyDataProvider,
    private val mapboxStaticMapService: MapboxStaticMapService,
    private val mapRateLimiter: MapRateLimiter
) {

    /**
     * GET /api/v1/home?latitude=12.9716&longitude=77.5946&theme=dark
     *
     * Returns the aggregated Home Dashboard data.
     * latitude/longitude are optional — if provided, nearby data includes
     * distance calculations and proper map centering.
     * theme is optional — "light" (default) or "dark" for the map image style.
     */
    @GetMapping
    fun getHomeDashboard(
        authentication: Authentication,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam(required = false, defaultValue = "light") theme: String
    ): ResponseEntity<ApiResponse<HomeDashboardResponse>> {
        val userId = extractUserId(authentication)

        val dashboard = getHomeDashboardUseCase.execute(userId, latitude, longitude, theme)

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Home dashboard retrieved successfully",
                data = dashboard.toResponse()
            )
        )
    }

    /**
     * GET /api/v1/home/map-preview?lat=12.9716&lng=77.5946&theme=dark
     *
     * Proxies the Mapbox Static Images API and returns the map as raw PNG bytes.
     *
     * ## Security
     * - JWT authenticated (Spring Security handles this via the existing filter chain).
     * - Rate limited: max [app.map.rate-limit.requests-per-minute] requests per user per minute
     *   using a Redis fixed-window counter (see [MapRateLimiter]).
     * - The Mapbox access token is added server-side and never returned to the client.
     *
     * ## Why a proxy instead of a direct Mapbox URL in the JSON?
     * Returning the Mapbox URL directly would embed the secret access token in the
     * API response, exposing it to anyone who inspects network traffic or decompiles
     * the app. This proxy endpoint keeps the token entirely server-side.
     *
     * @param lat   User's current latitude (map center)
     * @param lng   User's current longitude (map center)
     * @param theme "light" or "dark" map style
     * @return      PNG image bytes, or 503 if Mapbox is unconfigured, 429 if rate limited
     */
    @GetMapping("/map-preview", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getMapPreview(
        authentication: Authentication,
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(defaultValue = "light") theme: String
    ): ResponseEntity<ByteArray> {
        val userId = extractUserId(authentication)

        // Rate limiting check
        if (!mapRateLimiter.allowRequest(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", "20")
                .header("X-RateLimit-Remaining", "0")
                .header("Retry-After", "60")
                .build()
        }

        // Fetch nearby markers from the DB for this user's location
        val nearbySummary = nearbyDataProvider.getNearbySummary(userId, lat, lng, theme)
        val markers: List<NearbyMarker> = nearbySummary.markers

        // Fetch the image from Mapbox server-side (token never leaves the backend)
        val imageBytes = mapboxStaticMapService.fetchMapImage(lat, lng, markers, theme)
            ?: return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()

        val remaining = mapRateLimiter.remainingRequests(userId)

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .header("Cache-Control", "private, max-age=300") // 5-minute client cache
            .header("X-RateLimit-Limit", "20")
            .header("X-RateLimit-Remaining", remaining.toString())
            .body(imageBytes)
    }

    /**
     * Extracts the authenticated user's UUID from the JWT principal.
     * The JwtAuthenticationFilter stores the principal as a UUID object.
     */
    private fun extractUserId(authentication: Authentication): UUID =
        when (val principal = authentication.principal) {
            is UUID -> principal
            is String -> UUID.fromString(principal)
            else -> throw IllegalStateException("Unexpected principal type: $principal")
        }
}
