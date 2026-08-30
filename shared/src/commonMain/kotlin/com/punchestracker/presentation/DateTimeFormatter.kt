package com.punchestracker.presentation

interface DateTimeFormatter {
    fun format(timestampMillis: Long): String
}
