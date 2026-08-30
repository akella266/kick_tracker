package com.punchestracker.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KickMomentRepositoryImplTest {
    private class MemoryFileDataSource(initial: String? = null) : KickMomentFileDataSource {
        var content: String? = initial
        override suspend fun readText(): String? = content
        override suspend fun writeTextAtomically(text: String) {
            content = text
        }
    }

    @Test
    fun refreshReadsMomentsNewestFirst() = runTest {
        val source = MemoryFileDataSource(
            """
            {"moments":[{"id":"old","timestampMillis":10},{"id":"new","timestampMillis":20}]}
            """.trimIndent()
        )
        val repository = KickMomentRepositoryImpl(source, idProvider = { "unused" })

        repository.refresh().getOrThrow()

        assertEquals(listOf("new", "old"), repository.observeMoments().value.map { it.id })
    }

    @Test
    fun addMomentWritesJsonAndUpdatesState() = runTest {
        val source = MemoryFileDataSource()
        val repository = KickMomentRepositoryImpl(source, idProvider = { "fixed-id" })

        val result = repository.addMoment(30L).getOrThrow()

        assertEquals("fixed-id", result.id)
        assertEquals(30L, result.timestampMillis)
        assertEquals(listOf("fixed-id"), repository.observeMoments().value.map { it.id })
        assertTrue(source.content!!.contains("fixed-id"))
        assertTrue(source.content!!.contains("30"))
    }

    @Test
    fun deleteMomentRemovesOnlyMatchingId() = runTest {
        val source = MemoryFileDataSource(
            """
            {"moments":[{"id":"keep","timestampMillis":40},{"id":"remove","timestampMillis":50}]}
            """.trimIndent()
        )
        val repository = KickMomentRepositoryImpl(source, idProvider = { "unused" })
        repository.refresh().getOrThrow()

        repository.deleteMoment("remove").getOrThrow()

        assertEquals(listOf("keep"), repository.observeMoments().value.map { it.id })
        assertTrue(source.content!!.contains("keep"))
        assertTrue(!source.content!!.contains("remove"))
    }

    @Test
    fun corruptJsonRefreshReturnsFailureAndKeepsEmptyState() = runTest {
        val source = MemoryFileDataSource("not json")
        val repository = KickMomentRepositoryImpl(source, idProvider = { "unused" })

        val result = repository.refresh()

        assertTrue(result.isFailure)
        assertEquals(emptyList(), repository.observeMoments().value)
    }
}
