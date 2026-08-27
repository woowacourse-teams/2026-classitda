package com.classitda.data.remote.studio

import kotlinx.serialization.Serializable

@Serializable
internal data class StudioResponseDto(
    val id: Long,
    val name: String,
    val address: StudioAddressResponseDto,
    val phoneNumber: String,
    val openTime: String? = null,
    val closeTime: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
)
