package com.kurban.calory.core.time

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface DayProvider {
    fun currentDayId(): String
}

class DefaultDayProvider(
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) : DayProvider {
    override fun currentDayId(): String {
        return Clock.System.now().toLocalDateTime(timeZone).date.toString()
    }
}
