package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class RefreshKickMomentsUseCase(
    private val repository: KickMomentRepository,
) {
    suspend operator fun invoke() = repository.refresh()
}
