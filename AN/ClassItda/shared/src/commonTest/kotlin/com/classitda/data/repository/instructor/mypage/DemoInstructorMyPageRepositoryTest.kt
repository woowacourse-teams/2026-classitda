package com.classitda.data.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.StudioList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DemoInstructorMyPageRepositoryTest {
    @Test
    fun `demo 시설 목록과 상세 조회는 구조화 주소를 유지한다`() =
        runBlocking {
            val repository = DemoInstructorMyPageRepository()

            val studioList =
                assertIs<InstructorMyPageResult.Success<StudioList>>(repository.getStudios()).value
            assertEquals(1, studioList.studios.size)

            val studio =
                assertIs<InstructorMyPageResult.Success<ManagedStudio>>(
                    repository.getStudio(InstructorStudioId("studio-1")),
                ).value
            assertEquals("서울특별시 강남구 테헤란로", studio.address.displayAddress)
            assertEquals("5층 501호", studio.address.detailAddress)
        }

    @Test
    fun `demo 시설 생성과 수정 성공은 Unit을 반환한다`() =
        runBlocking {
            val repository = DemoInstructorMyPageRepository()
            val draft = StudioRegistrationDraft(name = "새 시설")

            assertEquals(InstructorMyPageResult.Success(Unit), repository.registerStudio(draft))
            assertEquals(
                InstructorMyPageResult.Success(Unit),
                repository.updateStudio(
                    InstructorStudioId("studio-1"),
                    ManagedStudio(InstructorStudioId("studio-1"), name = "기존 시설"),
                    draft,
                    StudioImageMutation.Unchanged,
                ),
            )
        }
}
