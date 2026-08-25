package com.classitda.domain.model.instructor.mypage

data class ManagedFacility(
    val id: InstructorFacilityId,
    val name: String,
    val address: FacilityAddress = FacilityAddress(),
    val image: FacilityImageSelection? = null,
    val phoneNumber: String = "",
    val description: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
)

data class FacilityAddress(
    val zoneCode: String = "",
    val roadAddress: String = "",
    val jibunAddress: String = "",
    val buildingName: String = "",
    val detailAddress: String = "",
) {
    val displayAddress: String
        get() = roadAddress.ifBlank { jibunAddress }

    val hasBaseAddress: Boolean
        get() = displayAddress.isNotBlank()
}

sealed interface FacilityImageSelection {
    val previewReference: String

    data class Remote(
        val imageUrl: String,
    ) : FacilityImageSelection {
        init {
            require(imageUrl.isNotBlank()) { "시설 원격 이미지 URL은 비어 있을 수 없습니다." }
        }

        override val previewReference: String = imageUrl
    }

    data class Local(
        val handle: String,
        override val previewReference: String,
        val mimeType: String,
        val fileName: String,
        val sizeBytes: Long,
    ) : FacilityImageSelection {
        init {
            require(handle.isNotBlank()) { "시설 로컬 이미지 handle은 비어 있을 수 없습니다." }
            require(previewReference.isNotBlank()) { "시설 로컬 이미지 미리보기 참조는 비어 있을 수 없습니다." }
            require(mimeType.isNotBlank()) { "시설 로컬 이미지 MIME type은 비어 있을 수 없습니다." }
            require(fileName.isNotBlank()) { "시설 로컬 이미지 파일명은 비어 있을 수 없습니다." }
            require(sizeBytes >= 0) { "시설 로컬 이미지 크기는 음수일 수 없습니다." }
        }
    }
}

data class FacilityRegistrationDraft(
    val image: FacilityImageSelection? = null,
    val name: String = "",
    val address: FacilityAddress = FacilityAddress(),
    val phoneNumber: String = "",
    val description: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
)
