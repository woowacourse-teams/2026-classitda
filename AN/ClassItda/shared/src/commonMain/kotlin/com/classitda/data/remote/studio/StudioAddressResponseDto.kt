package com.classitda.data.remote.studio

import kotlinx.serialization.Serializable

@Serializable
internal data class StudioAddressResponseDto(
    val zonecode: String? = null,
    val roadAddress: String? = null,
    val jibunAddress: String? = null,
    val buildingName: String? = null,
    val detailAddress: String? = null,
)
