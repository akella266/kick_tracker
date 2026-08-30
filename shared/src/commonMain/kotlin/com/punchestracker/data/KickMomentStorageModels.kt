package com.punchestracker.data

import kotlinx.serialization.Serializable

@Serializable
data class KickMomentFile(
    val moments: List<KickMomentRecord> = emptyList(),
)

@Serializable
data class KickMomentRecord(
    val id: String,
    val timestampMillis: Long,
)
