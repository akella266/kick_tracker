package com.punchestracker.domain.usecase

import com.punchestracker.domain.KickMomentRepository

class DeleteKickMomentUseCase(
    private val repository: KickMomentRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteMoment(id)
}
