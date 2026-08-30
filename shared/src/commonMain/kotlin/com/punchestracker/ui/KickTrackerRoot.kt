package com.punchestracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.DateTimeFormatter
import com.punchestracker.presentation.history.HistoryPresenter
import com.punchestracker.presentation.main.MainPresenter
import com.punchestracker.ui.history.HistoryScreen
import com.punchestracker.ui.main.MainScreen
import com.punchestracker.ui.theme.AppTheme
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private enum class Screen { Main, History }

@OptIn(ExperimentalTime::class)
@Composable
fun KickTrackerRoot(
    observeKickMoments: ObserveKickMomentsUseCase,
    addKickMoment: AddKickMomentUseCase,
    deleteKickMoment: DeleteKickMomentUseCase,
    refreshKickMoments: RefreshKickMomentsUseCase,
    dateTimeFormatter: DateTimeFormatter,
) {
    val scope = rememberCoroutineScope()
    val mainPresenter = remember {
        MainPresenter(observeKickMoments, addKickMoment, refreshKickMoments, dateTimeFormatter, scope)
    }
    val historyPresenter = remember {
        HistoryPresenter(observeKickMoments, deleteKickMoment, refreshKickMoments, dateTimeFormatter, scope)
    }
    var screen by remember { mutableStateOf(Screen.Main) }

    DisposableEffect(Unit) {
        onDispose {
            mainPresenter.close()
            historyPresenter.close()
        }
    }

    AppTheme {
        when (screen) {
            Screen.Main -> {
                val state by mainPresenter.state.collectAsState()
                MainScreen(
                    state = state,
                    onRecordKick = { mainPresenter.onRecordKick(Clock.System.now().toEpochMilliseconds()) },
                    onOpenHistory = { screen = Screen.History },
                )
            }
            Screen.History -> {
                val state by historyPresenter.state.collectAsState()
                HistoryScreen(
                    state = state,
                    onBack = { screen = Screen.Main },
                    onDelete = historyPresenter::onDelete,
                )
            }
        }
    }
}
