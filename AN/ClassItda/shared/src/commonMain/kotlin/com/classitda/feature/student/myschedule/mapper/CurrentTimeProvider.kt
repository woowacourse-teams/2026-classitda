package com.classitda.feature.student.myschedule.mapper

import kotlin.time.Clock
import kotlin.time.Instant

internal fun interface CurrentTimeProvider {
    fun now(): Instant
}

internal object SystemCurrentTimeProvider : CurrentTimeProvider {
    override fun now(): Instant = Clock.System.now()
}
