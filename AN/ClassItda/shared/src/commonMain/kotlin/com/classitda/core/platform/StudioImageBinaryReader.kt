package com.classitda.core.platform

import io.ktor.utils.io.ByteWriteChannel

/** Reads a picker handle only while an upload body is being written. */
internal interface StudioImageBinaryReader {
    suspend fun writeTo(
        handle: String,
        sink: ByteWriteChannel,
    )
}

internal expect fun createStudioImageBinaryReader(): StudioImageBinaryReader

internal class StudioImageBinaryReadException : IllegalStateException()
