package com.classitda.data.remote.instructor.mypage.studio

internal class StudioRemoteDataSource(
    private val api: StudioApi,
) {
    suspend fun getMine(): List<StudioResponseDto> = api.getMine()

    suspend fun get(studioId: Long): StudioResponseDto = api.get(studioId)

    suspend fun create(request: StudioCreateRequestDto) {
        api.create(request)
    }

    suspend fun update(
        studioId: Long,
        request: StudioUpdateRequestDto,
    ) {
        api.update(studioId, request)
    }

    suspend fun deleteImage(studioId: Long) {
        api.deleteImage(studioId)
    }
}
