package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorMyPageSummary

interface InstructorMyPageSummaryRepository {
    suspend fun getSummary(): InstructorMyPageResult<InstructorMyPageSummary>
}
