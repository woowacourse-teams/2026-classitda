package com.classitda.domain.repository.home

import com.classitda.domain.model.home.Pass

interface PassRepository {
    suspend fun getPrimaryPass(): Pass?
}
