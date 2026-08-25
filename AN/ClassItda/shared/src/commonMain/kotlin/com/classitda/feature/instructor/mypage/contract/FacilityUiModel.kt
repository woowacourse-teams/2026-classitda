package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId

data class FacilityImageUiModel(
    val selection: FacilityImageSelection,
) {
    val previewReference: String get() = selection.previewReference
}

/** Immutable facility data prepared for rendering. */
data class FacilityUiModel(
    val id: InstructorFacilityId,
    val name: String,
    val address: FacilityAddress = FacilityAddress(),
    val image: FacilityImageUiModel? = null,
    val phoneNumber: String = "",
    val description: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
)

data class FacilityListUiModel(
    val totalCount: Int,
    val facilities: List<FacilityUiModel>,
)

/** Form values owned by the UI. A ViewModel converts this to the repository draft. */
data class FacilityInputUiModel(
    val image: FacilityImageInputUiModel? = null,
    val name: String = "",
    val address: FacilityAddress = FacilityAddress(),
    val phoneNumber: String = "",
    val description: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
)

data class FacilityImageInputUiModel(
    val selection: FacilityImageSelection,
) {
    val previewReference: String get() = selection.previewReference
}

enum class FacilityImageUiError {
    PERMISSION_DENIED,
    CAMERA_UNAVAILABLE,
    READ_FAILED,
    INVALID_MIME,
    FILE_TOO_LARGE,
    UPLOAD_UNAVAILABLE,
}
