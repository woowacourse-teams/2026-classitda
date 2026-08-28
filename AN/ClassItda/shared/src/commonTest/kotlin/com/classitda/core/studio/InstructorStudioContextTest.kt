package com.classitda.core.studio

import com.classitda.domain.model.studio.Studio
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.studio.StudioRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InstructorStudioContextTest {
    @Test
    fun `시설 목록 갱신에 실패하면 기존 캐시를 비우고 다음 조회를 다시 시도한다`() =
        runBlocking {
            var shouldFail = false
            var requestCount = 0
            val studio = studio("42")
            val context =
                InstructorStudioContext(
                    object : StudioRepository {
                        override suspend fun getMyStudios(): List<Studio> {
                            requestCount++
                            if (shouldFail) error("시설 목록 조회 실패")
                            return listOf(studio)
                        }
                    },
                )

            assertEquals(listOf(studio), context.getStudios())
            shouldFail = true
            assertFailsWith<IllegalStateException> { context.refreshStudios() }

            shouldFail = false
            assertEquals(listOf(studio), context.getStudios())
            assertEquals(3, requestCount)
        }

    @Test
    fun `선택한 시설을 저장하고 다음 앱 실행에서 복원한다`() =
        runBlocking {
            val studios = listOf(studio("1"), studio("2"))
            val storage = InMemoryInstructorStudioSelectionStorage()
            val repository =
                object : StudioRepository {
                    override suspend fun getMyStudios(): List<Studio> = studios
                }

            InstructorStudioContext(repository, storage).apply {
                getStudios()
                selectStudio("2")
            }

            val restoredContext = InstructorStudioContext(repository, storage)

            assertEquals(studio("2"), restoredContext.getSelectedStudio())
        }

    private fun studio(id: String) =
        Studio(
            id = StudioId(id),
            name = "시설 $id",
            address = "서울",
            phoneNumber = "0212345678",
            openTime = null,
            closeTime = null,
            imageUrl = null,
            description = null,
        )
}
