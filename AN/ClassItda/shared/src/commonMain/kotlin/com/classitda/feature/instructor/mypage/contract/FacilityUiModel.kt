package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorFacilityId

data class FacilityImageUiModel(
    val id: String,
    val previewReference: String,
)

/** Immutable facility data prepared for rendering. */
data class FacilityUiModel(
    val id: InstructorFacilityId,
    val name: String,
    val address: String,
    val representativeImageReference: String? = null,
    val images: List<FacilityImageUiModel> = emptyList(),
    val detailAddress: String = "",
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
    val images: List<FacilityImageInputUiModel> = emptyList(),
    val name: String = "",
    val address: String = "",
    val detailAddress: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
) {
    companion object {
        const val MAX_IMAGE_COUNT: Int = 5
    }
}

data class FacilityImageInputUiModel(
    val id: String,
    val previewReference: String,
)
