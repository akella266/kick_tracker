package com.punchestracker.domain

import com.punchestracker.domain.usecase.AddKickMomentUseCase
import com.punchestracker.domain.usecase.DeleteKickMomentUseCase
import com.punchestracker.domain.usecase.ObserveKickMomentsUseCase
import com.punchestracker.domain.usecase.RefreshKickMomentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KickMomentUseCaseTest {
    private class FakeRepository : KickMomentRepository {
        val moments = MutableStateFlow(emptyList<KickMoment>())
        var deletedId: String? = null
        var refreshed = false

        override fun observeMoments() = moments

        override suspend fun addMoment(timestampMillis: Long): Result<KickMoment> {
            val moment = KickMoment(id = "moment-${timestampMillis}", timestampMillis = timestampMillis)
            moments.value = listOf(moment) + moments.value
            return Result.success(moment)
        }

        override suspend fun deleteMoment(id: String): Result<Unit> {
            deletedId = id
            moments.value = moments.value.filterNot { it.id == id }
            return Result.success(Unit)
        }

        override suspend fun refresh(): Result<Unit> {
            refreshed = true
            return Result.success(Unit)
        }
    }

    @Test
    fun addUseCaseReturnsMomentFromRepository() = runTest {
        val repository = FakeRepository()
        val result = AddKickMomentUseCase(repository).invoke(123_456L)

        assertEquals(KickMoment("moment-123456", 123_456L), result.getOrThrow())
        assertEquals(listOf(KickMoment("moment-123456", 123_456L)), repository.moments.value)
    }

    @Test
    fun deleteUseCaseDeletesSelectedId() = runTest {
        val repository = FakeRepository()
        DeleteKickMomentUseCase(repository).invoke("abc").getOrThrow()

        assertEquals("abc", repository.deletedId)
    }

    @Test
    fun observeUseCaseReturnsRepositoryFlow() = runTest {
        val repository = FakeRepository()
        repository.moments.value = listOf(KickMoment("1", 10L))

        assertEquals(listOf(KickMoment("1", 10L)), ObserveKickMomentsUseCase(repository).invoke().value)
    }

    @Test
    fun refreshUseCaseCallsRepositoryRefresh() = runTest {
        val repository = FakeRepository()
        RefreshKickMomentsUseCase(repository).invoke().getOrThrow()

        assertEquals(true, repository.refreshed)
    }
}
