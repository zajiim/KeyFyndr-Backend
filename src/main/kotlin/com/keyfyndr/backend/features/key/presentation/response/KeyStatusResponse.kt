package com.keyfyndr.backend.features.key.presentation.response

import com.keyfyndr.backend.features.key.domain.enums.KeyStatus

/**
 * Lightweight response returned after a status change operation (mark as lost/found).
 * Provides just enough info to confirm the operation succeeded.
 */
data class KeyStatusResponse(
    val id: String,
    val publicKeyId: String,
    val status: KeyStatus,
    val message: String
)
