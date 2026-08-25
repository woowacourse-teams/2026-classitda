package com.classitda.data.remote.instructor.mypage.facility

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AddressRequestDto(
    @SerialName("zonecode")
    val zoneCode: String,
    val roadAddress: String,
    val jibunAddress: String? = null,
    val buildingName: String? = null,
    val detailAddress: String? = null,
)

@Serializable
internal data class AddressResponseDto(
    @SerialName("zonecode")
    val zoneCode: String? = null,
    val roadAddress: String? = null,
    val jibunAddress: String? = null,
    val buildingName: String? = null,
    val detailAddress: String? = null,
)

@Serializable
internal data class StudioCreateRequestDto(
    val name: String,
    val address: AddressRequestDto,
    val phoneNumber: String,
    val openTime: String,
    val closeTime: String,
    val image: String? = null,
    val description: String? = null,
)

@Serializable
internal data class StudioUpdateRequestDto(
    val name: String? = null,
    val address: AddressRequestDto? = null,
    val phoneNumber: String? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
    val image: String? = null,
    val description: String? = null,
)

@Serializable
internal data class StudioResponseDto(
    val id: Long? = null,
    val name: String? = null,
    val address: AddressResponseDto? = null,
    val phoneNumber: String? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
    val image: String? = null,
    val description: String? = null,
)

@Serializable
internal data class ImageUploadUrlRequestDto(
    val extension: String,
    val size: Long,
)

@Serializable
internal data class ImageUploadUrlResponseDto(
    val objectKey: String? = null,
    val uploadUrl: String? = null,
    val contentType: String? = null,
)
