package com.classitda.data.remote.auth.signup

import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleLoginRequestDto(
    val idToken: String,
)

@Serializable
internal data class LoginResponseDto(
    val status: LoginStatusDto,
    val accessToken: String? = null,
    val accessTokenExpiresIn: Long? = null,
    val refreshToken: String? = null,
    val refreshTokenExpiresIn: Long? = null,
    val signupToken: String? = null,
    val signupTokenExpiresIn: Long? = null,
)

@Serializable
internal enum class LoginStatusDto { REGISTERED, REGISTRATION_REQUIRED }

@Serializable
internal data class SignupTermResponseDto(
    val id: Long,
    val code: SignupTermCodeDto,
    val title: String,
    val url: String,
    val required: Boolean,
    val version: Int,
)

@Serializable
internal enum class SignupTermCodeDto { SERVICE_TERMS, PRIVACY_POLICY, MARKETING_CONSENT }

@Serializable
internal data class PhoneVerificationSendRequestDto(
    val phoneNumber: String,
)

@Serializable
internal data class PhoneVerificationResponseDto(
    val verificationId: String,
    val expiresInSeconds: Long,
    val resendAfterSeconds: Long,
)

@Serializable
internal data class PhoneVerificationConfirmRequestDto(
    val otp: String,
)

@Serializable
internal data class SignupRequestDto(
    val name: String,
    val agreedTermIds: List<Long>,
)

@Serializable
internal data class SignupResponseDto(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long,
)
