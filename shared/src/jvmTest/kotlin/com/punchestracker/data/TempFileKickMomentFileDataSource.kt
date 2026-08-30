package com.punchestracker.data

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class TempFileKickMomentFileDataSource(
    private val path: Path = Files.createTempFile("kick-moments", ".json"),
) : KickMomentFileDataSource {
    override suspend fun readText(): String? = if (path.exists()) path.readText() else null

    override suspend fun writeTextAtomically(text: String) {
        val tempPath = path.resolveSibling("${path.fileName}.tmp")
        tempPath.writeText(text)
        Files.move(tempPath, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    }
}
