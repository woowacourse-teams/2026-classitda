package com.classitda.domain.repository.home

import com.classitda.domain.model.home.FacilityNotice

interface NoticeRepository {
    suspend fun getLatestNotice(): FacilityNotice?
}
