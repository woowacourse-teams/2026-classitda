package com.classitda.core.network

import com.classitda.core.auth.AuthTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun createClassItdaHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    tokenStorage: AuthTokenStorage? = null,
): HttpClient =
    HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        defaultRequest {
            url.takeFrom(baseUrl)
            header("X-API-Version", "1")
        }
        installBearerAuth(tokenStorage)
    }

internal fun createConfiguredHttpClient(
    config: NetworkConfig,
    tokenStorage: AuthTokenStorage,
): HttpClient =
    HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            url.takeFrom(config.baseUrl)
            header("X-API-Version", "1")
        }
        installBearerAuth(tokenStorage)
    }

private fun io.ktor.client.HttpClientConfig<*>.installBearerAuth(tokenStorage: AuthTokenStorage?) {
    if (tokenStorage == null) return

    install(Auth) {
        bearer {
            loadTokens {
                tokenStorage.read()?.let { tokens ->
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
                }
            }
        }
    }
}
