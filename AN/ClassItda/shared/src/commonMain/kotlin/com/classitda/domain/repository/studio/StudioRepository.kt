package com.classitda.domain.repository.studio

import com.classitda.domain.model.studio.Studio

interface StudioRepository {
    suspend fun getMyStudios(): List<Studio>
}
