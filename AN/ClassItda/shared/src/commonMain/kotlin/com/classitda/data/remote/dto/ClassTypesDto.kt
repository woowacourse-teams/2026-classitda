package com.classitda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ClassTypeResponseDto(
    val id: Long,
    val name: String,
)

@Serializable
internal data class ClassTypeCreateRequestDto(
    val name: String,
)

@Serializable
internal data class ClassTypeUpdateRequestDto(
    val name: String,
)
