package com.classitda.core.studio

import com.classitda.domain.model.studio.Studio
import com.classitda.domain.repository.studio.StudioRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class InstructorStudioContext(
    private val repository: StudioRepository,
) {
    private val mutex = Mutex()
    private var selectedStudio: Studio? = null

    suspend fun getSelectedStudio(): Studio =
        mutex.withLock {
            selectedStudio
                ?: repository
                    .getMyStudios()
                    .firstOrNull()
                    ?.also { selectedStudio = it }
                ?: error("사용할 수 있는 시설이 없습니다.")
        }
}
