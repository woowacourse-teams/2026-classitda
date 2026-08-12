package com.classitda.member.fixture;

import com.classitda.member.domain.Member;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member 기본_회원() {
        return 회원("회원", "+821012345678");
    }

    public static Member 회원(String name, String phoneNumber) {
        return Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .profileImageUrl(null)
                .build();
    }
}
