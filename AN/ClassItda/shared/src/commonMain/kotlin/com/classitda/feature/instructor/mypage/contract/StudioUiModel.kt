package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageSelection

data class StudioImageUiModel(
    val selection: StudioImageSelection,
) {
    val previewReference: String get() = selection.previewReference
}

/** Immutable studio data prepared for rendering. */
data class StudioUiModel(
    val id: InstructorStudioId,
    val name: String,
    val address: StudioAddress = StudioAddress(),
    val image: StudioImageUiModel? = null,
    val phoneNumber: String = "",
    val description: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
)

data class StudioListUiModel(
    val totalCount: Int,
    val studios: List<StudioUiModel>,
)

/** Form values owned by the UI. A ViewModel converts this to the repository draft. */
data class StudioInputUiModel(
    val image: StudioImageInputUiModel? = null,
    val name: String = "",
    val address: StudioAddress = StudioAddress(),
    val phoneNumber: String = "",
    val description: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
)

data class StudioImageInputUiModel(
    val selection: StudioImageSelection,
) {
    val previewReference: String get() = selection.previewReference
}

enum class StudioImageUiError {
    PERMISSION_DENIED,
    CAMERA_UNAVAILABLE,
    READ_FAILED,
    INVALID_MIME,
    FILE_TOO_LARGE,
    UPLOAD_UNAVAILABLE,
}
