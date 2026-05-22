package com.keyfyndr.backend.features.key.domain.enums

/**
 * Represents the lifecycle status of a physical key.
 *
 * State transitions:
 * - SAFE → LOST (owner reports key missing)
 * - LOST → FOUND (owner or someone reports key located)
 * - FOUND → SAFE (owner reclaims key)
 * - FOUND → CLAIMED (someone claims a found key — future feature)
 * - Any → INACTIVE (key is soft-deleted)
 */
enum class KeyStatus {
    SAFE,
    LOST,
    FOUND,
    CLAIMED,
    INACTIVE
}
