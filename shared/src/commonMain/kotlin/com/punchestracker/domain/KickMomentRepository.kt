package com.punchestracker.domain

import kotlinx.coroutines.flow.StateFlow

interface KickMomentRepository {
    fun observeMoments(): StateFlow<List<KickMoment>>
    suspend fun addMoment(timestampMillis: Long): Result<KickMoment>
    suspend fun deleteMoment(id: String): Result<Unit>
    suspend fun refresh(): Result<Unit>
}
