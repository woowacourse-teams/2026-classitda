package com.classitda.data.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DemoInstructorMyPageRepositoryTest {
    @Test
    fun `demo 시설 목록과 상세 조회는 구조화 주소를 유지한다`() =
        runBlocking {
            val repository = DemoInstructorMyPageRepository()

            val facilityList =
                assertIs<InstructorMyPageResult.Success<FacilityList>>(repository.getFacilities()).value
            assertEquals(1, facilityList.facilities.size)

            val facility =
                assertIs<InstructorMyPageResult.Success<ManagedFacility>>(
                    repository.getFacility(InstructorFacilityId("facility-1")),
                ).value
            assertEquals("서울특별시 강남구 테헤란로", facility.address.displayAddress)
            assertEquals("5층 501호", facility.address.detailAddress)
        }

    @Test
    fun `demo 시설 생성과 수정 성공은 Unit을 반환한다`() =
        runBlocking {
            val repository = DemoInstructorMyPageRepository()
            val draft = FacilityRegistrationDraft(name = "새 시설")

            assertEquals(InstructorMyPageResult.Success(Unit), repository.registerFacility(draft))
            assertEquals(
                InstructorMyPageResult.Success(Unit),
                repository.updateFacility(InstructorFacilityId("facility-1"), draft),
            )
        }
}
