package com.punchestracker.data

interface KickMomentFileDataSource {
    suspend fun readText(): String?
    suspend fun writeTextAtomically(text: String)
}
