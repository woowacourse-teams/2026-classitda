package com.pheeeew.data.remote.sigh

import com.pheeeew.data.remote.sigh.dto.SighCreateRequestDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureCollectionDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureDto
import com.pheeeew.domain.model.sigh.SighBounds
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class DefaultSighApi(
    private val client: HttpClient,
) : SighApi {
    override suspend fun getSighs(bounds: SighBounds): SighFeatureCollectionDto =
        client
            .get("/api/v1/sighs") {
                parameter("minLongitude", bounds.minLongitude)
                parameter("minLatitude", bounds.minLatitude)
                parameter("maxLongitude", bounds.maxLongitude)
                parameter("maxLatitude", bounds.maxLatitude)
            }.body()

    override suspend fun registerSigh(request: SighCreateRequestDto): SighFeatureDto =
        client
            .post("/api/v1/sighs") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
}
