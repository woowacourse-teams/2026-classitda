package com.classitda.core.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class StudioImagePickerContractTest {
    @Test
    fun `허용된 이미지 선택은 Local 메타데이터를 보존한다`() {
        val selection =
            StudioImagePickerSelection(
                handle = "opaque-handle",
                previewReference = "preview-reference",
                mimeType = "image/jpeg",
                fileName = "studio.jpg",
                sizeBytes = 1024,
            )

        assertNull(validateStudioImagePickerSelection(selection))
        assertEquals("opaque-handle", selection.handle)
        assertEquals("preview-reference", selection.previewReference)
        assertEquals("image/jpeg", selection.mimeType)
        assertEquals("studio.jpg", selection.fileName)
        assertEquals(1024, selection.sizeBytes)
    }

    @Test
    fun `지원하지 않는 MIME과 확장자 및 5MB 초과는 사용자 오류로 구분한다`() {
        val invalidMime = validSelection(mimeType = "image/gif")
        val invalidExtension = validSelection(fileName = "studio.heic")
        val oversized = validSelection(sizeBytes = STUDIO_IMAGE_MAX_SIZE_BYTES + 1)
        val empty = validSelection(sizeBytes = 0)

        assertEquals(
            StudioImagePickerError.INVALID_MIME,
            validateStudioImagePickerSelection(invalidMime),
        )
        assertEquals(
            StudioImagePickerError.INVALID_MIME,
            validateStudioImagePickerSelection(invalidExtension),
        )
        assertEquals(
            StudioImagePickerError.FILE_TOO_LARGE,
            validateStudioImagePickerSelection(oversized),
        )
        assertEquals(
            StudioImagePickerError.READ_FAILED,
            validateStudioImagePickerSelection(empty),
        )
    }

    @Test
    fun `jpg jpeg png webp는 5MB까지 허용한다`() {
        val allowed =
            listOf(
                validSelection(mimeType = "image/jpeg", fileName = "studio.jpg"),
                validSelection(mimeType = "image/jpeg", fileName = "studio.jpeg"),
                validSelection(mimeType = "image/png", fileName = "studio.png"),
                validSelection(mimeType = "image/webp", fileName = "studio.webp"),
                validSelection(sizeBytes = STUDIO_IMAGE_MAX_SIZE_BYTES),
            )

        allowed.forEach { selection -> assertNull(validateStudioImagePickerSelection(selection)) }
    }

    @Test
    fun `선택 취소와 오류는 Selected와 다른 결과 계약이다`() {
        assertIs<StudioImagePickerResult.Cancelled>(StudioImagePickerResult.Cancelled)

        val error = StudioImagePickerResult.Error(StudioImagePickerError.PERMISSION_DENIED)
        assertEquals(StudioImagePickerError.PERMISSION_DENIED, error.reason)
    }

    private fun validSelection(
        mimeType: String = "image/png",
        fileName: String = "studio.png",
        sizeBytes: Long = 1024,
    ): StudioImagePickerSelection =
        StudioImagePickerSelection(
            handle = "opaque-handle",
            previewReference = "preview-reference",
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = sizeBytes,
        )
}
