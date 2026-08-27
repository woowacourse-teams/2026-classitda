package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile

interface InstructorProfileRepository {
    suspend fun getProfile(): InstructorMyPageResult<InstructorAccountProfile>

    suspend fun updateProfileName(name: String): InstructorMyPageResult<InstructorAccountProfile>
}
