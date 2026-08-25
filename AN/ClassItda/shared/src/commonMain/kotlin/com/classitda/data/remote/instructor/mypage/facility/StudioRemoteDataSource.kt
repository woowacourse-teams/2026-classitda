package com.classitda.data.remote.instructor.mypage.facility

internal class StudioRemoteDataSource(
    private val api: StudioApi,
) {
    suspend fun getMine(): List<StudioResponseDto> = api.getMine()

    suspend fun get(studioId: Long): StudioResponseDto = api.get(studioId)
}
