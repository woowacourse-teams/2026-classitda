package com.classitda.core.studio

import com.classitda.domain.model.studio.Studio
import com.classitda.domain.repository.studio.StudioRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class InstructorStudioContext(
    private val repository: StudioRepository,
) {
    private val mutex = Mutex()
    private var studios: List<Studio>? = null
    private var selectedStudio: Studio? = null

    suspend fun getStudios(): List<Studio> =
        mutex.withLock {
            studios ?: repository.getMyStudios().also { studios = it }
        }

    suspend fun selectStudio(studioId: String) {
        mutex.withLock {
            val studio =
                (studios ?: repository.getMyStudios().also { studios = it })
                    .firstOrNull { it.id.value == studioId }
                    ?: error("선택한 시설을 찾을 수 없습니다.")
            selectedStudio = studio
        }
    }

    suspend fun getSelectedStudio(): Studio =
        mutex.withLock {
            selectedStudio
                ?: (studios ?: repository.getMyStudios().also { studios = it })
                    .firstOrNull()
                    ?.also { selectedStudio = it }
                ?: error("사용할 수 있는 시설이 없습니다.")
        }
}
