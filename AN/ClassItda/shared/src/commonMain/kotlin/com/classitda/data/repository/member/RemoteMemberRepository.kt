package com.classitda.data.repository.member

import com.classitda.data.remote.member.MemberApi
import com.classitda.data.remote.member.MemberMeResponseDto
import com.classitda.domain.model.member.Member
import com.classitda.domain.repository.member.MemberRepository

internal class RemoteMemberRepository(
    private val api: MemberApi,
) : MemberRepository {
    override suspend fun getMe(): Member = api.getMe().toDomain()
}

private fun MemberMeResponseDto.toDomain() = Member(name = name)
