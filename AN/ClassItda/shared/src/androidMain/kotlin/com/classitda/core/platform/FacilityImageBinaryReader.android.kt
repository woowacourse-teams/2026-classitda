package com.classitda.core.platform

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import java.io.File

internal actual fun createFacilityImageBinaryReader(): FacilityImageBinaryReader = AndroidFacilityImageBinaryReader

private object AndroidFacilityImageBinaryReader : FacilityImageBinaryReader {
    override suspend fun writeTo(
        handle: String,
        sink: ByteWriteChannel,
    ) {
        val file = File(handle)
        if (!file.name.startsWith("classitda-facility-image-") || !file.isFile) {
            throw FacilityImageBinaryReadException()
        }

        try {
            file.inputStream().use { input ->
                val buffer = ByteArray(BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    sink.writeFully(buffer, 0, count)
                }
            }
        } catch (exception: FacilityImageBinaryReadException) {
            throw exception
        } catch (_: Throwable) {
            throw FacilityImageBinaryReadException()
        }
    }
}

private const val BUFFER_SIZE = 32 * 1024
