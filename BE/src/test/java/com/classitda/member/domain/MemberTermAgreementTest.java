package com.classitda.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.member.fixture.MemberFixture;
import com.classitda.member.fixture.MemberTermAgreementFixture;
import com.classitda.member.fixture.TermFixture;
import org.junit.jupiter.api.Test;

class MemberTermAgreementTest {

    @Test
    void 약관_동의_회원은_필수이다() {
        // given
        Term term = TermFixture.기본_약관();

        // when / then
        assertThatThrownBy(() -> MemberTermAgreementFixture.약관_동의(
                null,
                term,
                true
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("약관 동의 회원은 필수입니다.");
    }

    @Test
    void 동의_약관은_필수이다() {
        // given
        Member member = MemberFixture.기본_회원();

        // when / then
        assertThatThrownBy(() -> MemberTermAgreementFixture.약관_동의(
                member,
                null,
                true
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동의 약관은 필수입니다.");
    }

    @Test
    void 동의하지_않은_약관은_저장_대상으로_생성할_수_없다() {
        // given
        Member member = MemberFixture.기본_회원();
        Term term = TermFixture.기본_약관();

        // when / then
        assertThatThrownBy(() -> MemberTermAgreementFixture.약관_동의(
                member,
                term,
                false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동의한 약관만 저장할 수 있습니다.");
    }
}
