package com.classitda.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * API 호출에 사용할 공통 Ktor client를 생성한다.
 *
 * 아직 endpoint 호출이나 인증 헤더를 여기서 처리하지 않는다. 인증은
 * identity-access가 정해진 뒤 별도 plugin 또는 request 구성으로 연결한다.
 */
fun createNetworkClient(config: NetworkConfig): HttpClient =
    HttpClient {
        defaultRequest {
            url(config.baseUrl)
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }

        expectSuccess = false
    }
