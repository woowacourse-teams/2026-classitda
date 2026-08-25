package com.classitda.core.platform

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSInputStream
import platform.Foundation.NSURL

internal actual fun createFacilityImageBinaryReader(): FacilityImageBinaryReader = IosFacilityImageBinaryReader

@OptIn(ExperimentalForeignApi::class)
private object IosFacilityImageBinaryReader : FacilityImageBinaryReader {
    override suspend fun writeTo(
        handle: String,
        sink: ByteWriteChannel,
    ) {
        if (!handle.contains("classitda-facility-image-")) {
            throw FacilityImageBinaryReadException()
        }
        val input = NSInputStream(NSURL.fileURLWithPath(handle))
        input.open()
        try {
            while (true) {
                val buffer = ByteArray(BUFFER_SIZE)
                val count =
                    buffer.usePinned { pinned ->
                        val bytesRead =
                            input.read(
                                pinned.addressOf(0).reinterpret(),
                                BUFFER_SIZE.toULong(),
                            )
                        bytesRead.toInt()
                    }
                if (count < 0) throw FacilityImageBinaryReadException()
                if (count == 0) break
                sink.writeFully(buffer.copyOf(count))
            }
        } finally {
            input.close()
        }
    }
}

private const val BUFFER_SIZE = 32 * 1024
