package com.classitda.domain.model.instructor.mypage

data class ManagedFacility(
    val id: InstructorFacilityId,
    val name: String,
    val address: String,
    val representativeImageReference: String? = null,
)

data class FacilityImageDraft(
    val id: String,
    val previewReference: String,
) {
    init {
        require(id.isNotBlank()) { "시설 이미지 임시 ID는 비어 있을 수 없습니다." }
    }
}

data class FacilityRegistrationDraft(
    val images: List<FacilityImageDraft> = emptyList(),
    val name: String = "",
    val address: String = "",
    val detailAddress: String = "",
    val phoneNumber: String = "",
    val description: String = "",
) {
    init {
        require(images.size <= MAX_IMAGE_COUNT) {
            "시설 이미지는 최대 ${MAX_IMAGE_COUNT}장까지 선택할 수 있습니다."
        }
    }

    companion object {
        const val MAX_IMAGE_COUNT: Int = 5
    }
}
