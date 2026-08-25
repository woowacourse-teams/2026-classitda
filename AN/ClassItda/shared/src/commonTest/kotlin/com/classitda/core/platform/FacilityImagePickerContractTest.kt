package com.classitda.core.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class FacilityImagePickerContractTest {
    @Test
    fun `허용된 이미지 선택은 Local 메타데이터를 보존한다`() {
        val selection =
            FacilityImagePickerSelection(
                handle = "opaque-handle",
                previewReference = "preview-reference",
                mimeType = "image/jpeg",
                fileName = "facility.jpg",
                sizeBytes = 1024,
            )

        assertNull(validateFacilityImagePickerSelection(selection))
        assertEquals("opaque-handle", selection.handle)
        assertEquals("preview-reference", selection.previewReference)
        assertEquals("image/jpeg", selection.mimeType)
        assertEquals("facility.jpg", selection.fileName)
        assertEquals(1024, selection.sizeBytes)
    }

    @Test
    fun `지원하지 않는 MIME과 확장자 및 5MB 초과는 사용자 오류로 구분한다`() {
        val invalidMime = validSelection(mimeType = "image/gif")
        val invalidExtension = validSelection(fileName = "facility.heic")
        val oversized = validSelection(sizeBytes = FACILITY_IMAGE_MAX_SIZE_BYTES + 1)
        val empty = validSelection(sizeBytes = 0)

        assertEquals(
            FacilityImagePickerError.INVALID_MIME,
            validateFacilityImagePickerSelection(invalidMime),
        )
        assertEquals(
            FacilityImagePickerError.INVALID_MIME,
            validateFacilityImagePickerSelection(invalidExtension),
        )
        assertEquals(
            FacilityImagePickerError.FILE_TOO_LARGE,
            validateFacilityImagePickerSelection(oversized),
        )
        assertEquals(
            FacilityImagePickerError.READ_FAILED,
            validateFacilityImagePickerSelection(empty),
        )
    }

    @Test
    fun `jpg jpeg png webp는 5MB까지 허용한다`() {
        val allowed =
            listOf(
                validSelection(mimeType = "image/jpeg", fileName = "facility.jpg"),
                validSelection(mimeType = "image/jpeg", fileName = "facility.jpeg"),
                validSelection(mimeType = "image/png", fileName = "facility.png"),
                validSelection(mimeType = "image/webp", fileName = "facility.webp"),
                validSelection(sizeBytes = FACILITY_IMAGE_MAX_SIZE_BYTES),
            )

        allowed.forEach { selection -> assertNull(validateFacilityImagePickerSelection(selection)) }
    }

    @Test
    fun `선택 취소와 오류는 Selected와 다른 결과 계약이다`() {
        assertIs<FacilityImagePickerResult.Cancelled>(FacilityImagePickerResult.Cancelled)

        val error = FacilityImagePickerResult.Error(FacilityImagePickerError.PERMISSION_DENIED)
        assertEquals(FacilityImagePickerError.PERMISSION_DENIED, error.reason)
    }

    private fun validSelection(
        mimeType: String = "image/png",
        fileName: String = "facility.png",
        sizeBytes: Long = 1024,
    ): FacilityImagePickerSelection =
        FacilityImagePickerSelection(
            handle = "opaque-handle",
            previewReference = "preview-reference",
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = sizeBytes,
        )
}
