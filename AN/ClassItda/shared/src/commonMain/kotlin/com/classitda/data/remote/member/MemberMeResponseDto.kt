package com.classitda.data.remote.member

import kotlinx.serialization.Serializable

@Serializable
internal data class MemberMeResponseDto(
    val name: String = "",
)
