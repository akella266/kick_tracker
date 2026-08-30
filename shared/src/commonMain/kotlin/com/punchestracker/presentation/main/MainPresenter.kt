package com.punchestracker.presentation.main

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.DateTimeFormatter
import com.punchestracker.presentation.KickMomentUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainPresenter(
    private val observeKickMoments: ObserveKickMomentsUseCase,
    private val addKickMoment: AddKickMomentUseCase,
    private val refreshKickMoments: RefreshKickMomentsUseCase,
    private val dateTimeFormatter: DateTimeFormatter,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val mutableState = MutableStateFlow(MainState())
    private val jobs = mutableListOf<Job>()
    val state: StateFlow<MainState> = mutableState

    init {
        jobs += scope.launch(dispatcher) {
            observeKickMoments().collect { moments ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        recentMoments = moments.take(5).map(::toUi),
                    )
                }
            }
        }
        jobs += scope.launch(dispatcher) {
            refreshKickMoments().onFailure {
                mutableState.update { state -> state.copy(isLoading = false, errorMessage = "Не удалось загрузить историю") }
            }
        }
    }

    fun onRecordKick(timestampMillis: Long) {
        scope.launch(dispatcher) {
            addKickMoment(timestampMillis)
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(lastRecordedMessage = "Запись сохранена", errorMessage = null)
                    }
                }
                .onFailure {
                    mutableState.update { state ->
                        state.copy(lastRecordedMessage = null, errorMessage = "Не удалось сохранить запись")
                    }
                }
        }
    }

    fun clearMessages() {
        mutableState.update { it.copy(lastRecordedMessage = null, errorMessage = null) }
    }

    fun close() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private fun toUi(moment: KickMoment) = KickMomentUi(
        id = moment.id,
        formattedDateTime = dateTimeFormatter.format(moment.timestampMillis),
    )
}

data class MainState(
    val isLoading: Boolean = true,
    val recentMoments: List<KickMomentUi> = emptyList(),
    val lastRecordedMessage: String? = null,
    val errorMessage: String? = null,
)
