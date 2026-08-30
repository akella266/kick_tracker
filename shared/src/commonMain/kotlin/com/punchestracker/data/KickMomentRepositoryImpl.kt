package com.punchestracker.data

import com.punchestracker.domain.KickMoment
import com.punchestracker.domain.KickMomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlinx.serialization.json.Json

class KickMomentRepositoryImpl(
    private val fileDataSource: KickMomentFileDataSource,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    },
    private val idProvider: () -> String = { createDefaultId() },
) : KickMomentRepository {
    private val moments = MutableStateFlow<List<KickMoment>>(emptyList())

    override fun observeMoments(): StateFlow<List<KickMoment>> = moments

    override suspend fun addMoment(timestampMillis: Long): Result<KickMoment> = runCatching {
        val current = readFileOrEmpty()
        val newMoment = KickMoment(id = idProvider(), timestampMillis = timestampMillis)
        val updated = (listOf(newMoment) + current).sortedByDescending { it.timestampMillis }
        writeMoments(updated)
        moments.value = updated
        newMoment
    }

    override suspend fun deleteMoment(id: String): Result<Unit> = runCatching {
        val current = readFileOrEmpty()
        val updated = current.filterNot { it.id == id }.sortedByDescending { it.timestampMillis }
        writeMoments(updated)
        moments.value = updated
    }

    override suspend fun refresh(): Result<Unit> = runCatching {
        moments.value = readFileOrEmpty().sortedByDescending { it.timestampMillis }
    }

    private suspend fun readFileOrEmpty(): List<KickMoment> {
        val text = fileDataSource.readText()?.takeIf { it.isNotBlank() } ?: return emptyList()
        return json.decodeFromString(KickMomentFile.serializer(), text)
            .moments
            .map { KickMoment(id = it.id, timestampMillis = it.timestampMillis) }
            .sortedByDescending { it.timestampMillis }
    }

    private suspend fun writeMoments(value: List<KickMoment>) {
        val file = KickMomentFile(
            moments = value
                .sortedByDescending { it.timestampMillis }
                .map { KickMomentRecord(id = it.id, timestampMillis = it.timestampMillis) }
        )
        fileDataSource.writeTextAtomically(json.encodeToString(KickMomentFile.serializer(), file))
    }

    companion object {
        @OptIn(kotlin.time.ExperimentalTime::class)
        fun createDefaultId(): String = "kick-${Clock.System.now().toEpochMilliseconds()}-${RandomId.next()}"
    }
}

private object RandomId {
    private var counter: Long = 0L

    fun next(): Long {
        counter += 1
        return counter
    }
}
