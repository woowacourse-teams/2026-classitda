package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.model.instructor.mypage.UploadedStudioImage

/** Uploads local image bytes and returns the object key accepted by the Studio request contract. */
interface StudioImageUploader {
    suspend fun upload(image: StudioImageSelection.Local): InstructorMyPageResult<UploadedStudioImage>
}
