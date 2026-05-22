package com.keyfyndr.backend.features.key.domain.service

/**
 * Domain service interface for generating unique public key identifiers.
 *
 * Public key IDs are human-readable identifiers (e.g., "KF-8G2X9P") used for
 * crowd-based key sighting and lookup. They are distinct from the internal UUID.
 *
 * The implementation must guarantee uniqueness against the database.
 */
interface KeyIdGeneratorService {

    /**
     * Generates a unique public key ID in the format KF-XXXXXX.
     *
     * @return A unique, human-readable public key identifier
     */
    fun generateUniqueKeyId(): String
}
