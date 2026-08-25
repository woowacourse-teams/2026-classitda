package com.classitda.data.remote.instructor.mypage.facility

import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.UploadedFacilityImage
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

internal fun FacilityAddress.toAddressRequestDto(): InstructorMyPageResult<AddressRequestDto> {
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

internal fun FacilityRegistrationDraft.toStudioCreateRequestDto(
    uploadedImage: UploadedFacilityImage? = null,
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

            is FacilityImageSelection.Local -> {
                uploadedImage?.objectKey ?: return invalidRequest()
            }

            is FacilityImageSelection.Remote -> {
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

internal fun AddressResponseDto.toDomain(): InstructorMyPageResult<FacilityAddress> {
    val requiredZoneCode = zoneCode?.takeIf(String::isNotBlank) ?: return contractFailure()
    val requiredRoadAddress = roadAddress?.takeIf(String::isNotBlank) ?: return contractFailure()
    if (!requiredZoneCode.isFiveDigitZoneCode()) return contractFailure()

    return InstructorMyPageResult.Success(
        FacilityAddress(
            zoneCode = requiredZoneCode,
            roadAddress = requiredRoadAddress,
            jibunAddress = jibunAddress.orEmpty(),
            buildingName = buildingName.orEmpty(),
            detailAddress = detailAddress.orEmpty(),
        ),
    )
}

internal fun StudioResponseDto.toDomain(): InstructorMyPageResult<ManagedFacility> {
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
            else -> FacilityImageSelection.Remote(image)
        }

    return InstructorMyPageResult.Success(
        ManagedFacility(
            id = requiredId.toInstructorFacilityId(),
            name = requiredName,
            address = mappedAddress,
            image = mappedImage,
            phoneNumber = requiredPhoneNumber,
            description = description.orEmpty(),
            openingTime = requiredOpenTime,
            closingTime = requiredCloseTime,
        ),
    )
}

internal fun Long.toInstructorFacilityId(): InstructorFacilityId = InstructorFacilityId(toString())

internal fun InstructorFacilityId.toWireId(): InstructorMyPageResult<Long> =
    value.toLongOrNull()?.let { InstructorMyPageResult.Success(it) } ?: invalidRequest()

private fun String.isFiveDigitZoneCode(): Boolean = length == ZONE_CODE_LENGTH && all(Char::isDigit)

private fun String.isValidTime(): Boolean = HH_MM_PATTERN.matches(this)

private fun String.toMinutes(): Int = substringBefore(':').toInt() * 60 + substringAfter(':').toInt()

private fun invalidRequest() = InstructorMyPageResult.Failure(InstructorMyPageFailureReason.INVALID_REQUEST)

private fun contractFailure() = InstructorMyPageResult.Failure(InstructorMyPageFailureReason.CONTRACT)
