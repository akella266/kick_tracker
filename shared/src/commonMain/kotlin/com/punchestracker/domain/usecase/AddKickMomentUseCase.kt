package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class AddKickMomentUseCase(
    private val repository: KickMomentRepository,
) {
    suspend operator fun invoke(timestampMillis: Long) = repository.addMoment(timestampMillis)
}
