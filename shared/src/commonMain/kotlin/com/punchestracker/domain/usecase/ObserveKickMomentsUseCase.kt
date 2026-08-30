package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class ObserveKickMomentsUseCase(
    private val repository: KickMomentRepository,
) {
    operator fun invoke() = repository.observeMoments()
}
