package com.keyfyndr.backend.features.home.data.service

import com.keyfyndr.backend.common.config.MapboxProperties
import com.keyfyndr.backend.features.home.domain.model.NearbyMarker
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**
 * Fetches map images from the Mapbox Static Images API.
 *
 * ## Security design
 * The Mapbox access token lives ONLY inside [MapboxProperties] (injected from the
 * `MAPBOX_API` environment variable). It is:
 *   - Never returned in any JSON response.
 *   - Never logged (the full URL is masked before logging).
 *   - Added to the Mapbox request server-side only.
 *
 * The mobile client receives the image bytes — it never sees the token.
 *
 * ## URL format (Mapbox Static Images v1)
 * ```
 * GET https://api.mapbox.com/styles/v1/{style}/static
 *      /{overlay}/{lon},{lat},{zoom}/{width}x{height}{@2x}
 *      ?access_token=TOKEN
 * ```
 * When `overlay` contains markers, the zoom/center is replaced with `auto`
 * so Mapbox fits all pins automatically.
 *
 * See: https://docs.mapbox.com/api/maps/static-images/
 */
@Service
class MapboxStaticMapService(
    private val props: MapboxProperties
) {
    private val logger = LoggerFactory.getLogger(MapboxStaticMapService::class.java)

    private val restClient: RestClient = RestClient.builder()
        .baseUrl("https://api.mapbox.com")
        .build()

    companion object {
        /** Blue dot — represents the user's current location */
        private const val USER_PIN_COLOR = "4A90D9"

        /** Red pin — LOST key */
        private const val LOST_PIN_COLOR = "E74C3C"

        /** Green pin — FOUND key */
        private const val FOUND_PIN_COLOR = "27AE60"

        private const val MAPBOX_STATIC_BASE = "/styles/v1"
    }

     /**
      * Fetches the static map image as raw bytes.
      *
      * @param userLat   User's current latitude (placed as a blue dot, null = omit)
      * @param userLng   User's current longitude (placed as a blue dot, null = omit)
      * @param markers   Nearby key markers to render on the map
      * @param theme     "light" or "dark" — selects the Mapbox style
      * @param zoom      Map zoom level (optional, if provided the map will center on user location with this zoom)
      * @return          PNG image bytes ready to stream to the mobile client,
      *                  or `null` if the token is unconfigured or the request fails
      */
     fun fetchMapImage(
         userLat: Double?,
         userLng: Double?,
         markers: List<NearbyMarker>,
         theme: String,
         zoom: Double? = null
     ): ByteArray? {
         if (props.accessToken.isBlank()) {
             logger.warn("MAPBOX_API token is not configured — map-preview endpoint will return 503.")
             return null
         }
 
         val style = if (theme == "dark") props.styleDark else props.styleLight
         val overlay = buildOverlay(userLat, userLng, markers)
         val retinaFlag = if (props.retina) "@2x" else ""
         val sizeSegment = "${props.imageWidth}x${props.imageHeight}$retinaFlag"
 
         // If a specific zoom is provided and user location is available, use it to center the map.
         // Otherwise, use `auto` bounding so Mapbox fits all markers in frame automatically.
         // Falls back to a world-level zoom if there are no markers at all.
         val positionSegment = if (zoom != null && userLat != null && userLng != null) {
             "${formatCoord(userLng)},${formatCoord(userLat)},$zoom"
         } else if (overlay.isNotBlank()) {
             "auto"
         } else {
             "0,0,1"
         }

        val url = buildUrl(style, overlay, positionSegment, sizeSegment)
        logMaskedUrl(url)

        return try {
            restClient.get()
                .uri(url)
                .accept(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG)
                .retrieve()
                .body(ByteArray::class.java)
        } catch (ex: Exception) {
            logger.error("Mapbox Static Images API request failed: ${ex.message}")
            null
        }
    }

    /**
     * Builds the pipe-separated overlay string containing all marker pins.
     *
     * Format per marker: `pin-s+RRGGBB(lng,lat)`
     * Markers are capped at [MapboxProperties.maxOverlayMarkers] to stay within
     * the Mapbox URL length limit (~8 KB).
     */
    private fun buildOverlay(userLat: Double?, userLng: Double?, markers: List<NearbyMarker>): String {
        val pins = mutableListOf<String>()

        // Key markers (LOST = red, FOUND = green) — nearest first, capped
        markers.take(props.maxOverlayMarkers).forEach { marker ->
            val color = if (marker.type == "FOUND") FOUND_PIN_COLOR else LOST_PIN_COLOR
            // Mapbox uses (longitude,latitude) order
            pins.add("pin-s+$color(${formatCoord(marker.longitude)},${formatCoord(marker.latitude)})")
        }

        // User location — added last so it renders on top of key pins
        if (userLat != null && userLng != null) {
            pins.add("pin-s+$USER_PIN_COLOR(${formatCoord(userLng)},${formatCoord(userLat)})")
        }

        return pins.joinToString(",")
    }

    private fun buildUrl(style: String, overlay: String, position: String, size: String): URI {
        val overlaySegment = if (overlay.isNotBlank()) "/$overlay" else ""
        return UriComponentsBuilder
            .fromUriString("https://api.mapbox.com/styles/v1/$style/static$overlaySegment/$position/$size")
            .queryParam("access_token", props.accessToken)
            .build(true)
            .toUri()
    }

    /** Logs the URL with the token replaced by `[REDACTED]` to prevent accidental log leaks. */
    private fun logMaskedUrl(url: URI) {
        val masked = url.toString().replace(
            Regex("access_token=[^&]+"),
            "access_token=[REDACTED]"
        )
        logger.debug("Fetching Mapbox static map: $masked")
    }

    /** Formats a coordinate to 6 decimal places — Mapbox recommends max 6 digits of precision. */
    private fun formatCoord(value: Double): String = "%.6f".format(value)
}
