package com.classitda.member.fixture;

import com.classitda.member.domain.Member;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member 기본_회원() {
        return Member.builder()
                .name("회원")
                .phoneNumber("+821012345678")
                .profileImageUrl(null)
                .build();
    }
}
