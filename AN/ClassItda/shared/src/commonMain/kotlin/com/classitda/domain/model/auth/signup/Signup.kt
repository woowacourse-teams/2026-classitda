package com.classitda.domain.model.auth.signup

import kotlin.jvm.JvmInline

@JvmInline
value class GoogleIdToken(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Google ID 토큰은 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class SignupToken(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "회원가입 토큰은 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class SignupName(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "이름은 비어 있을 수 없습니다." }
        require(value.length <= 50) { "이름은 50자 이하여야 합니다." }
    }
}

@JvmInline
value class SignupPhoneNumber(
    val value: String,
) {
    init {
        require(Regex("^010[0-9]{8}$").matches(value)) { "휴대전화번호는 010XXXXXXXX 형식이어야 합니다." }
    }
}

@JvmInline
value class PhoneVerificationCode(
    val value: String,
) {
    init {
        require(Regex("^[0-9]{6}$").matches(value)) { "인증번호는 숫자 6자리여야 합니다." }
    }
}

@JvmInline
value class PhoneVerificationId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "휴대전화 인증 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class TermId(
    val value: Long,
) {
    init {
        require(value > 0) { "약관 ID는 양수여야 합니다." }
    }
}

data class LoginTokens(
    val accessToken: String,
    val accessTokenExpiresInSeconds: Long,
    val refreshToken: String,
    val refreshTokenExpiresInSeconds: Long,
) {
    init {
        require(accessToken.isNotBlank()) { "Access Token은 비어 있을 수 없습니다." }
        require(refreshToken.isNotBlank()) { "Refresh Token은 비어 있을 수 없습니다." }
        require(accessTokenExpiresInSeconds > 0) { "Access Token 만료 시간은 양수여야 합니다." }
        require(refreshTokenExpiresInSeconds > 0) { "Refresh Token 만료 시간은 양수여야 합니다." }
    }
}

sealed interface GoogleLoginResult {
    data object WithdrawalPending : GoogleLoginResult

    data class Registered(
        val tokens: LoginTokens,
    ) : GoogleLoginResult

    data class RegistrationRequired(
        val signupToken: SignupToken,
        val expiresInSeconds: Long,
    ) : GoogleLoginResult {
        init {
            require(expiresInSeconds > 0) { "회원가입 토큰 만료 시간은 양수여야 합니다." }
        }
    }
}

enum class SignupTermCode {
    SERVICE_TERMS,
    PRIVACY_POLICY,
    MARKETING_CONSENT,
}

data class SignupTerm(
    val id: TermId,
    val code: SignupTermCode,
    val title: String,
    val url: String,
    val required: Boolean,
    val version: Int,
) {
    init {
        require(title.isNotBlank()) { "약관 제목은 비어 있을 수 없습니다." }
        require(url.isNotBlank()) { "약관 URL은 비어 있을 수 없습니다." }
        require(version > 0) { "약관 버전은 양수여야 합니다." }
    }
}

data class PhoneVerificationChallenge(
    val id: PhoneVerificationId,
    val expiresInSeconds: Long,
    val resendAfterSeconds: Long,
) {
    init {
        require(expiresInSeconds > 0) { "인증번호 만료 시간은 양수여야 합니다." }
        require(resendAfterSeconds >= 0) { "인증번호 재전송 대기 시간은 음수일 수 없습니다." }
    }
}
