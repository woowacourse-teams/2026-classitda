package com.classitda.core.studio

import com.classitda.domain.model.studio.Studio
import com.classitda.domain.repository.studio.StudioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class InstructorStudioContext(
    private val repository: StudioRepository,
    private val selectionStorage: InstructorStudioSelectionStorage = InMemoryInstructorStudioSelectionStorage(),
) {
    private val mutex = Mutex()
    private var studios: List<Studio>? = null
    private var selectedStudio: Studio? = null

    suspend fun getStudios(): List<Studio> =
        mutex.withLock {
            studios
                ?: repository.getMyStudios().also {
                    studios = it
                    restoreSelectedStudio(it)
                }
        }

    suspend fun refreshStudios() {
        mutex.withLock {
            try {
                val refreshedStudios = repository.getMyStudios()
                studios = refreshedStudios
                restoreSelectedStudio(refreshedStudios)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                studios = null
                selectedStudio = null
                throw exception
            }
        }
    }

    suspend fun selectStudio(studioId: String) {
        mutex.withLock {
            val studio =
                (studios ?: repository.getMyStudios().also { studios = it })
                    .firstOrNull { it.id.value == studioId }
                    ?: error("선택한 시설을 찾을 수 없습니다.")
            selectedStudio = studio
            selectionStorage.save(studio.id.value)
        }
    }

    suspend fun getSelectedStudio(): Studio =
        mutex.withLock {
            selectedStudio ?: run {
                val availableStudios =
                    studios ?: repository.getMyStudios().also {
                        studios = it
                    }
                restoreSelectedStudio(availableStudios)
                selectedStudio ?: error("사용할 수 있는 시설이 없습니다.")
            }
        }

    suspend fun clearSelectedStudio() {
        mutex.withLock {
            selectedStudio = null
            selectionStorage.clear()
        }
    }

    private fun restoreSelectedStudio(availableStudios: List<Studio>) {
        val storedStudioId = selectedStudio?.id?.value ?: selectionStorage.read()
        val restoredStudio =
            availableStudios.firstOrNull { it.id.value == storedStudioId }
                ?: availableStudios.firstOrNull()
        selectedStudio = restoredStudio
        if (restoredStudio == null) {
            selectionStorage.clear()
        } else {
            selectionStorage.save(restoredStudio.id.value)
        }
    }
}
