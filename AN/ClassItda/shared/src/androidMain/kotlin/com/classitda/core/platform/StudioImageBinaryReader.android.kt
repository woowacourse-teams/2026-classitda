package com.classitda.core.platform

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CancellationException
import java.io.File

internal actual fun createStudioImageBinaryReader(): StudioImageBinaryReader = AndroidStudioImageBinaryReader

private object AndroidStudioImageBinaryReader : StudioImageBinaryReader {
    override suspend fun writeTo(
        handle: String,
        sink: ByteWriteChannel,
    ) {
        val file = File(handle)
        if (!file.name.startsWith("classitda-studio-image-") || !file.isFile) {
            throw StudioImageBinaryReadException()
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
        } catch (exception: StudioImageBinaryReadException) {
            throw exception
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            throw StudioImageBinaryReadException()
        }
    }
}

private const val BUFFER_SIZE = 32 * 1024
