package com.pheeeew.data.remote.sigh

import com.pheeeew.core.network.toApiException
import com.pheeeew.data.remote.common.dto.ErrorResponseDto
import com.pheeeew.data.remote.sigh.dto.SighCreateRequestDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureCollectionDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureDto
import com.pheeeew.domain.exception.ApiException
import com.pheeeew.domain.model.sigh.SighBounds
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException

class DefaultSighApi(
    private val client: HttpClient,
) : SighApi {
    override suspend fun getSighs(bounds: SighBounds): SighFeatureCollectionDto =
        executeRequest {
            client.get("/api/v1/sighs") {
                parameter("minLongitude", bounds.minLongitude)
                parameter("minLatitude", bounds.minLatitude)
                parameter("maxLongitude", bounds.maxLongitude)
                parameter("maxLatitude", bounds.maxLatitude)
            }
        }

    override suspend fun registerSigh(request: SighCreateRequestDto): SighFeatureDto =
        executeRequest {
            client.post("/api/v1/sighs") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
}

private suspend inline fun <reified T> executeRequest(block: suspend () -> HttpResponse): T =
    try {
        val response = block()
        response.throwIfFailed()
        response.body()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: ApiException) {
        throw exception
    } catch (exception: Throwable) {
        throw exception.toApiException()
    }

private suspend fun HttpResponse.throwIfFailed() {
    if (status.value in 200..299) return

    val error =
        runCatching {
            body<ErrorResponseDto>()
        }.getOrNull()
    val code = error?.code ?: "HTTP_${status.value}"
    val message = error?.message ?: "API 요청에 실패했습니다."

    throw when (status) {
        HttpStatusCode.BadRequest -> ApiException.InvalidRequest(code, message)
        HttpStatusCode.Unauthorized -> ApiException.Unauthorized(code, message)
        HttpStatusCode.Forbidden -> ApiException.Forbidden(code, message)
        HttpStatusCode.NotFound -> ApiException.NotFound(code, message)
        HttpStatusCode.Conflict -> ApiException.Conflict(code, message)
        else -> ApiException.Unknown(code, message)
    }
}
