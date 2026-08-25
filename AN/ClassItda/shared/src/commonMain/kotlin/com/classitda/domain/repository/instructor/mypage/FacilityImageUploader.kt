package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.UploadedFacilityImage

/** Uploads local image bytes and returns the object key accepted by the Studio request contract. */
interface FacilityImageUploader {
    suspend fun upload(image: FacilityImageSelection.Local): InstructorMyPageResult<UploadedFacilityImage>
}
