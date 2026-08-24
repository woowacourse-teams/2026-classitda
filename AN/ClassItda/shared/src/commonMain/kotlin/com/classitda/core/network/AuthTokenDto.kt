package com.classitda.core.network

import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
internal data class RefreshTokenResponseDto(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long,
)
