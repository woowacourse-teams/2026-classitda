package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft

/** Studio-only contract so studio networking can evolve independently from my-page demo flows. */
interface InstructorStudioRepository {
    suspend fun getStudios(): InstructorMyPageResult<StudioList>

    suspend fun getStudio(studioId: InstructorStudioId): InstructorMyPageResult<ManagedStudio>

    suspend fun registerStudio(draft: StudioRegistrationDraft): InstructorMyPageResult<Unit>

    suspend fun updateStudio(
        studioId: InstructorStudioId,
        original: ManagedStudio,
        draft: StudioRegistrationDraft,
        imageMutation: StudioImageMutation,
    ): InstructorMyPageResult<Unit>
}
