package com.punchestracker.platform

import com.punchestracker.presentation.DateTimeFormatter
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970

class IosRussianDateTimeFormatter : DateTimeFormatter {
    private val formatter = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = "ru_RU")
        dateFormat = "d MMMM, HH:mm"
    }

    override fun format(timestampMillis: Long): String {
        val date = NSDate.dateWithTimeIntervalSince1970(timestampMillis.toDouble() / 1000.0)
        return formatter.stringFromDate(date)
    }
}
