package com.classitda.core.time

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal fun interface CurrentDateTimeProvider {
    fun now(): LocalDateTime
}

internal object DeviceCurrentDateTimeProvider : CurrentDateTimeProvider {
    override fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
