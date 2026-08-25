package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.FacilityImageMutation
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility

/** Facility-only contract so facility networking can evolve independently from my-page demo flows. */
interface InstructorFacilityRepository {
    suspend fun getFacilities(): InstructorMyPageResult<FacilityList>

    suspend fun getFacility(facilityId: InstructorFacilityId): InstructorMyPageResult<ManagedFacility>

    suspend fun registerFacility(draft: FacilityRegistrationDraft): InstructorMyPageResult<Unit>

    suspend fun updateFacility(
        facilityId: InstructorFacilityId,
        original: ManagedFacility,
        draft: FacilityRegistrationDraft,
        imageMutation: FacilityImageMutation,
    ): InstructorMyPageResult<Unit>
}
