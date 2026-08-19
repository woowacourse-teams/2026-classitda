package com.classitda.domain.repository.mypage

import com.classitda.domain.model.mypage.MyPass

interface MyPassRepository {
    suspend fun getMyPasses(): List<MyPass>
}
