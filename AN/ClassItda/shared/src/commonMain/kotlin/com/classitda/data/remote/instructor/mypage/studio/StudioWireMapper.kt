package com.classitda.data.remote.instructor.mypage.studio

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.model.instructor.mypage.UploadedStudioImage
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult

private const val ZONE_CODE_LENGTH = 5
private const val ROAD_ADDRESS_MAX_LENGTH = 255
private const val JIBUN_ADDRESS_MAX_LENGTH = 255
private const val BUILDING_NAME_MAX_LENGTH = 100
private const val DETAIL_ADDRESS_MAX_LENGTH = 100
private const val STUDIO_NAME_MAX_LENGTH = 50
private const val PHONE_NUMBER_MAX_LENGTH = 20
private val HH_MM_PATTERN = Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")

internal fun StudioAddress.toAddressRequestDto(): InstructorMyPageResult<AddressRequestDto> {
    if (!zoneCode.isFiveDigitZoneCode() || roadAddress.isBlank()) return invalidRequest()
    if (roadAddress.length > ROAD_ADDRESS_MAX_LENGTH) return invalidRequest()
    if (jibunAddress.length > JIBUN_ADDRESS_MAX_LENGTH) return invalidRequest()
    if (buildingName.length > BUILDING_NAME_MAX_LENGTH) return invalidRequest()
    if (detailAddress.length > DETAIL_ADDRESS_MAX_LENGTH) return invalidRequest()

    return InstructorMyPageResult.Success(
        AddressRequestDto(
            zoneCode = zoneCode,
            roadAddress = roadAddress,
            jibunAddress = jibunAddress.takeUnless(String::isBlank),
            buildingName = buildingName.takeUnless(String::isBlank),
            detailAddress = detailAddress.takeUnless(String::isBlank),
        ),
    )
}

internal fun StudioRegistrationDraft.toStudioCreateRequestDto(
    uploadedImage: UploadedStudioImage? = null,
): InstructorMyPageResult<StudioCreateRequestDto> {
    if (name.isBlank() || name.length > STUDIO_NAME_MAX_LENGTH) return invalidRequest()
    if (phoneNumber.isBlank() || phoneNumber.length > PHONE_NUMBER_MAX_LENGTH) return invalidRequest()
    if (!openingTime.isValidTime() || !closingTime.isValidTime()) return invalidRequest()
    if (closingTime.toMinutes() <= openingTime.toMinutes()) return invalidRequest()

    val addressDto =
        when (val result = address.toAddressRequestDto()) {
            is InstructorMyPageResult.Success -> result.value
            is InstructorMyPageResult.Failure -> return result
        }
    val objectKey =
        when (image) {
            null -> {
                if (uploadedImage != null) return invalidRequest()
                null
            }

            is StudioImageSelection.Local -> {
                uploadedImage?.objectKey ?: return invalidRequest()
            }

            is StudioImageSelection.Remote -> {
                return invalidRequest()
            }
        }

    return InstructorMyPageResult.Success(
        StudioCreateRequestDto(
            name = name,
            address = addressDto,
            phoneNumber = phoneNumber,
            openTime = openingTime,
            closeTime = closingTime,
            image = objectKey,
            description = description.takeUnless(String::isBlank),
        ),
    )
}

internal fun ManagedStudio.toStudioUpdateRequestDto(
    draft: StudioRegistrationDraft,
    imageMutation: StudioImageMutation,
    uploadedImage: UploadedStudioImage? = null,
): InstructorMyPageResult<StudioUpdateRequestDto> {
    val name =
        draft.name.takeUnless { it == this.name }?.also {
            if (it.isBlank() || it.length > STUDIO_NAME_MAX_LENGTH) return invalidRequest()
        }
    val address =
        if (draft.address != this.address) {
            when (val result = draft.address.toAddressRequestDto()) {
                is InstructorMyPageResult.Success -> result.value
                is InstructorMyPageResult.Failure -> return result
            }
        } else {
            null
        }
    val phoneNumber =
        draft.phoneNumber.takeUnless { it == this.phoneNumber }?.also {
            if (it.isBlank() || it.length > PHONE_NUMBER_MAX_LENGTH) return invalidRequest()
        }
    val openingTimeChanged = draft.openingTime != this.openingTime
    val closingTimeChanged = draft.closingTime != this.closingTime
    if (openingTimeChanged || closingTimeChanged) {
        if (!draft.openingTime.isParsableTime() || !draft.closingTime.isParsableTime()) return invalidRequest()
        if (draft.closingTime.toMinutes() <= draft.openingTime.toMinutes()) return invalidRequest()
    }
    val mappedImage =
        when (imageMutation) {
            StudioImageMutation.Unchanged -> {
                if (draft.image != this.image) return invalidRequest()
                null
            }

            is StudioImageMutation.Replace -> {
                if (draft.image != imageMutation.image) return invalidRequest()
                uploadedImage?.objectKey ?: return invalidRequest()
            }

            StudioImageMutation.Remove -> {
                if (draft.image != null || this.image == null) return invalidRequest()
                null
            }
        }

    return InstructorMyPageResult.Success(
        StudioUpdateRequestDto(
            name = name,
            address = address,
            phoneNumber = phoneNumber,
            openTime = draft.openingTime.takeIf { openingTimeChanged },
            closeTime = draft.closingTime.takeIf { closingTimeChanged },
            image = mappedImage,
            description = draft.description.takeIf { it != this.description },
        ),
    )
}

internal fun AddressResponseDto.toDomain(): InstructorMyPageResult<StudioAddress> =
    InstructorMyPageResult.Success(
        StudioAddress(
            zoneCode = zoneCode.orEmpty(),
            roadAddress = roadAddress.orEmpty(),
            jibunAddress = jibunAddress.orEmpty(),
            buildingName = buildingName.orEmpty(),
            detailAddress = detailAddress.orEmpty(),
        ),
    )

internal fun StudioResponseDto.toDomain(): InstructorMyPageResult<ManagedStudio> {
    val requiredId = id ?: return contractFailure()
    val requiredName = name?.takeIf(String::isNotBlank) ?: return contractFailure()
    val requiredAddress = address ?: return contractFailure()
    val requiredPhoneNumber = phoneNumber?.takeIf(String::isNotBlank) ?: return contractFailure()
    val requiredOpenTime = openTime?.takeIf(String::isNotBlank) ?: return contractFailure()
    val requiredCloseTime = closeTime?.takeIf(String::isNotBlank) ?: return contractFailure()
    val mappedAddress =
        when (val result = requiredAddress.toDomain()) {
            is InstructorMyPageResult.Success -> result.value
            is InstructorMyPageResult.Failure -> return result
        }
    val mappedImage =
        when {
            image == null -> null
            image.isBlank() -> return contractFailure()
            else -> StudioImageSelection.Remote(image)
        }

    return InstructorMyPageResult.Success(
        ManagedStudio(
            id = requiredId.toInstructorStudioId(),
            name = requiredName,
            address = mappedAddress,
            image = mappedImage,
            phoneNumber = requiredPhoneNumber,
            description = description.orEmpty(),
            openingTime = requiredOpenTime.toStudioDisplayTime(),
            closingTime = requiredCloseTime.toStudioDisplayTime(),
        ),
    )
}

internal fun Long.toInstructorStudioId(): InstructorStudioId = InstructorStudioId(toString())

internal fun InstructorStudioId.toWireId(): InstructorMyPageResult<Long> =
    value.toLongOrNull()?.let { InstructorMyPageResult.Success(it) } ?: invalidRequest()

private fun String.isFiveDigitZoneCode(): Boolean = length == ZONE_CODE_LENGTH && all(Char::isDigit)

private fun String.isValidTime(): Boolean = HH_MM_PATTERN.matches(this)

private fun String.isParsableTime(): Boolean = matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d)?"))

private fun String.toMinutes(): Int = substringBefore(':').toInt() * 60 + substringAfter(':').take(2).toInt()

private fun String.toStudioDisplayTime(): String =
    if (matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d"))) {
        take(5)
    } else {
        this
    }

private fun invalidRequest() = InstructorMyPageResult.Failure(InstructorMyPageFailureReason.INVALID_REQUEST)

private fun contractFailure() = InstructorMyPageResult.Failure(InstructorMyPageFailureReason.CONTRACT)
