package com.punchestracker.presentation.history

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
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

class HistoryPresenter(
    private val observeKickMoments: ObserveKickMomentsUseCase,
    private val deleteKickMoment: DeleteKickMomentUseCase,
    private val refreshKickMoments: RefreshKickMomentsUseCase,
    private val dateTimeFormatter: DateTimeFormatter,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val mutableState = MutableStateFlow(HistoryState())
    private val jobs = mutableListOf<Job>()
    val state: StateFlow<HistoryState> = mutableState

    init {
        jobs += scope.launch(dispatcher) {
            observeKickMoments().collect { moments ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        moments = moments.map(::toUi),
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

    fun onDelete(id: String) {
        scope.launch(dispatcher) {
            deleteKickMoment(id)
                .onFailure {
                    mutableState.update { state -> state.copy(errorMessage = "Не удалось удалить запись") }
                }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
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

data class HistoryState(
    val isLoading: Boolean = true,
    val moments: List<KickMomentUi> = emptyList(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean = !isLoading && moments.isEmpty()
}
