package com.classitda.core.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorResponseDto(
    val code: String,
    val message: String,
)

// 서버 에러 바디(ErrorResponseDto)를 파싱해서 code/message를 뽑는다. 실패하면(바디가 없거나
// 스펙과 다르면) 상태 코드 설명으로 대체해, 어떤 응답이 와도 항상 code/message가 채워지게 한다.
internal suspend fun ResponseException.toErrorResponse(): ErrorResponseDto =
    runCatching { response.body<ErrorResponseDto>() }
        .getOrDefault(ErrorResponseDto(code = "UNKNOWN", message = response.status.description))
