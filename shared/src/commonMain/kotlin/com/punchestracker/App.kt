package com.punchestracker

import androidx.compose.runtime.Composable
import com.punchestracker.domain.KickMomentRepository
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.DateTimeFormatter
import com.punchestracker.ui.KickTrackerRoot

@Composable
fun App(
    repository: KickMomentRepository,
    dateTimeFormatter: DateTimeFormatter,
) {
    KickTrackerRoot(
        observeKickMoments = ObserveKickMomentsUseCase(repository),
        addKickMoment = AddKickMomentUseCase(repository),
        deleteKickMoment = DeleteKickMomentUseCase(repository),
        refreshKickMoments = RefreshKickMomentsUseCase(repository),
        dateTimeFormatter = dateTimeFormatter,
    )
}
