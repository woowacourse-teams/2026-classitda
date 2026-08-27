package com.classitda.data.repository.studio

import com.classitda.data.remote.studio.StudioApi
import com.classitda.data.remote.studio.StudioResponseDto
import com.classitda.domain.model.studio.Studio
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.studio.StudioRepository
import kotlinx.datetime.LocalTime

internal class RemoteStudioRepository(
    private val api: StudioApi,
) : StudioRepository {
    override suspend fun getMyStudios(): List<Studio> =
        api
            .getMyStudios()
            .map(StudioResponseDto::toDomain)
}

private fun StudioResponseDto.toDomain() =
    Studio(
        id = StudioId(id.toString()),
        name = name,
        address = address.roadAddress ?: address.jibunAddress.orEmpty(),
        phoneNumber = phoneNumber,
        openTime = openTime?.let(LocalTime::parse),
        closeTime = closeTime?.let(LocalTime::parse),
        imageUrl = imageUrl,
        description = description,
    )
