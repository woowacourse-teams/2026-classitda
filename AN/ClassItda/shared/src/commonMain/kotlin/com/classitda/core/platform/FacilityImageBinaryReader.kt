package com.classitda.core.platform

import io.ktor.utils.io.ByteWriteChannel

/** Reads a picker handle only while an upload body is being written. */
internal interface FacilityImageBinaryReader {
    suspend fun writeTo(
        handle: String,
        sink: ByteWriteChannel,
    )
}

internal expect fun createFacilityImageBinaryReader(): FacilityImageBinaryReader

internal class FacilityImageBinaryReadException : IllegalStateException()
