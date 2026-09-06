package com.punchestracker.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
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

    private class YieldingMemoryFileDataSource : KickMomentFileDataSource {
        var content: String? = null

        override suspend fun readText(): String? {
            yield()
            return content
        }

        override suspend fun writeTextAtomically(text: String) {
            yield()
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

    @Test
    fun concurrentAddsDoNotLoseMoments() = runTest {
        val repository = KickMomentRepositoryImpl(YieldingMemoryFileDataSource())

        coroutineScope {
            (1L..20L)
                .map { timestamp -> async { repository.addMoment(timestamp).getOrThrow() } }
                .awaitAll()
        }

        assertEquals(20, repository.observeMoments().value.size)
        assertEquals((20L downTo 1L).toList(), repository.observeMoments().value.map { it.timestampMillis })
    }
}
