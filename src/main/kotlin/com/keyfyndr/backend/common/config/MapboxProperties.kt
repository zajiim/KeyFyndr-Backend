package com.keyfyndr.backend.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Typed configuration for the Mapbox Static Images API.
 *
 * All values are bound from `application.properties` under the `app.mapbox.*` prefix.
 * The access token is injected via the `MAPBOX_API` environment variable —
 * it is NEVER returned to any client response.
 *
 * See: https://docs.mapbox.com/api/maps/static-images/
 */
@Component
@ConfigurationProperties(prefix = "app.mapbox")
data class MapboxProperties(
    /** Mapbox access token — sourced from MAPBOX_API env var. Never exposed in responses. */
    var accessToken: String = "",

    /** Mapbox style ID for light theme (e.g. "mapbox/light-v11") */
    var styleLight: String = "mapbox/light-v11",

    /** Mapbox style ID for dark theme (e.g. "mapbox/dark-v11") */
    var styleDark: String = "mapbox/dark-v11",

    /** Output image width in pixels */
    var imageWidth: Int = 700,

    /** Output image height in pixels */
    var imageHeight: Int = 400,

    /** Whether to request a 2x retina image (@2x suffix) */
    var retina: Boolean = true,

    /** Nearby keys search radius in meters. Keys beyond this are excluded from the map. */
    var nearbyRadiusMeters: Double = 5000.0,

    /** Max marker pins to include in the Mapbox URL overlay (guards against URL length limits) */
    var maxOverlayMarkers: Int = 20
)
