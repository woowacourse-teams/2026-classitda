package com.classitda.domain.repository.member

import com.classitda.domain.model.member.Member

interface MemberRepository {
    suspend fun getMe(): Member
}
