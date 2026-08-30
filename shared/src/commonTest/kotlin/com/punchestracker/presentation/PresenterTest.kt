package com.punchestracker.presentation

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.KickMomentRepository
import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import com.punchestracker.presentation.history.HistoryPresenter
import com.punchestracker.presentation.main.MainPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PresenterTest {
    private class FakeFormatter : DateTimeFormatter {
        override fun format(timestampMillis: Long) = "formatted-$timestampMillis"
    }

    private class FakeRepository : KickMomentRepository {
        val moments = MutableStateFlow(emptyList<KickMoment>())
        var addResult: Result<KickMoment> = Result.success(KickMoment("new", 100L))
        var deleteResult: Result<Unit> = Result.success(Unit)

        override fun observeMoments() = moments

        override suspend fun addMoment(timestampMillis: Long): Result<KickMoment> {
            addResult.getOrNull()?.let { moments.value = listOf(it) + moments.value }
            return addResult
        }

        override suspend fun deleteMoment(id: String): Result<Unit> {
            if (deleteResult.isSuccess) moments.value = moments.value.filterNot { it.id == id }
            return deleteResult
        }

        override suspend fun refresh(): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun mainPresenterShowsOnlyFiveRecentMoments() = runTest {
        val repository = FakeRepository()
        repository.moments.value = (1L..6L).map { KickMoment("id-$it", it) }.sortedByDescending { it.timestampMillis }
        val presenter = mainPresenter(repository, this, StandardTestDispatcher(testScheduler))

        testScheduler.advanceUntilIdle()

        assertEquals(listOf("id-6", "id-5", "id-4", "id-3", "id-2"), presenter.state.value.recentMoments.map { it.id })
        presenter.close()
    }

    @Test
    fun mainPresenterRecordsMomentAndShowsRussianSuccessMessage() = runTest {
        val repository = FakeRepository()
        val presenter = mainPresenter(repository, this, StandardTestDispatcher(testScheduler))

        presenter.onRecordKick(100L)
        testScheduler.advanceUntilIdle()

        assertEquals("Запись сохранена", presenter.state.value.lastRecordedMessage)
        assertNull(presenter.state.value.errorMessage)
        presenter.close()
    }

    @Test
    fun historyPresenterDeletesMoment() = runTest {
        val repository = FakeRepository()
        repository.moments.value = listOf(KickMoment("delete-me", 10L))
        val presenter = historyPresenter(repository, this, StandardTestDispatcher(testScheduler))

        presenter.onDelete("delete-me")
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList(), presenter.state.value.moments)
        presenter.close()
    }

    private fun mainPresenter(
        repository: KickMomentRepository,
        scope: CoroutineScope,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
    ): MainPresenter {
        return MainPresenter(
            observeKickMoments = ObserveKickMomentsUseCase(repository),
            addKickMoment = AddKickMomentUseCase(repository),
            refreshKickMoments = RefreshKickMomentsUseCase(repository),
            dateTimeFormatter = FakeFormatter(),
            scope = scope,
            dispatcher = dispatcher,
        )
    }

    private fun historyPresenter(
        repository: KickMomentRepository,
        scope: CoroutineScope,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
    ): HistoryPresenter {
        return HistoryPresenter(
            observeKickMoments = ObserveKickMomentsUseCase(repository),
            deleteKickMoment = DeleteKickMomentUseCase(repository),
            refreshKickMoments = RefreshKickMomentsUseCase(repository),
            dateTimeFormatter = FakeFormatter(),
            scope = scope,
            dispatcher = dispatcher,
        )
    }
}
