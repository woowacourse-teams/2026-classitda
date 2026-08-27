package com.classitda.data.remote.member

import kotlinx.serialization.Serializable

@Serializable
internal data class MyNameUpdateRequestDto(
    val name: String,
)
