package com.classitda.member.fixture;

import com.classitda.member.domain.Member;
import com.classitda.member.domain.MemberTermAgreement;
import com.classitda.member.domain.Term;

public final class MemberTermAgreementFixture {

    private MemberTermAgreementFixture() {
    }

    public static MemberTermAgreement 약관_동의(
            Member member,
            Term term,
            boolean agreed
    ) {
        return MemberTermAgreement.builder()
                .member(member)
                .term(term)
                .agreed(agreed)
                .build();
    }
}
