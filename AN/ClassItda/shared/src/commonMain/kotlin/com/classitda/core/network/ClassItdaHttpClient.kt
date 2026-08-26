package com.classitda.core.network

import co.touchlab.kermit.Logger
import com.classitda.core.auth.AuthTokenStorage
import com.classitda.domain.model.auth.signup.LoginTokens
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

internal fun createClassItdaHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    tokenStorage: AuthTokenStorage? = null,
): HttpClient =
    HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
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
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, request ->
                if (cause is ResponseException) {
                    Logger.e("${request.method.value} ${request.url}: ${cause.response.status}", cause)
                }
            }
        }
        defaultRequest {
            url.takeFrom(config.baseUrl)
            header("X-API-Version", "1")
        }
        installBearerAuth(tokenStorage)
    }

internal fun createObjectStorageHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) {
        expectSuccess = false
    }

internal fun createObjectStorageHttpClient(): HttpClient =
    HttpClient {
        expectSuccess = false
    }

private fun io.ktor.client.HttpClientConfig<*>.installBearerAuth(tokenStorage: AuthTokenStorage?) {
    if (tokenStorage == null) return

    install(Auth) {
        bearer {
            sendWithoutRequest { request -> request.url.build().encodedPath != "/api/auth/tokens/refresh" }
            loadTokens {
                tokenStorage.read()?.let { tokens ->
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
                }
            }
            refreshTokens {
                val refreshToken = tokenStorage.read()?.refreshToken ?: return@refreshTokens null
                val refreshParams = this
                try {
                    val refreshed =
                        client
                            .post("api/auth/tokens/refresh") {
                                with(refreshParams) {
                                    this@post.markAsRefreshTokenRequest()
                                }
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequestDto(refreshToken))
                            }.body<RefreshTokenResponseDto>()
                    val tokens =
                        LoginTokens(
                            accessToken = refreshed.accessToken,
                            accessTokenExpiresInSeconds = refreshed.accessTokenExpiresIn,
                            refreshToken = refreshed.refreshToken,
                            refreshTokenExpiresInSeconds = refreshed.refreshTokenExpiresIn,
                        )
                    tokenStorage.write(tokens)
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: ClientRequestException) {
                    if (exception.response.status.value == 401) {
                        tokenStorage.clear()
                    }
                    null
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }
}
