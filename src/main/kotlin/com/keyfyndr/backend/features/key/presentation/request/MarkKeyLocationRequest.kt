package com.keyfyndr.backend.features.key.presentation.request

/**
 * Optional location data sent when marking a key as LOST or FOUND.
 *
 * The mobile app captures the user's current coordinates and attaches them
 * so the backend can power nearby-key queries on the Home Dashboard.
 * Both fields are optional — the status change still works without location.
 */
data class MarkKeyLocationRequest(
    val latitude: Double? = null,
    val longitude: Double? = null
)
