package com.classitda.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.member.fixture.MemberFixture;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void 회원_이름은_50자까지_허용한다() {
        // given
        String name = "가".repeat(50);

        // when
        Member member = MemberFixture.회원(name, "+821012345678");

        // then
        assertThat(member.getName()).isEqualTo(name);
    }

    @Test
    void 회원_이름이_비어_있으면_거부한다() {
        // given
        String[] invalidNames = {"", " "};

        // when / then
        for (String invalidName : invalidNames) {
            assertMemberError(
                    () -> MemberFixture.회원(invalidName, "+821012345678"),
                    MemberErrorCode.MEMBER_NAME_REQUIRED
            );
        }
        assertMemberError(
                () -> MemberFixture.회원(null, "+821012345678"),
                MemberErrorCode.MEMBER_NAME_REQUIRED
        );
    }

    @Test
    void 회원_이름이_50자를_초과하면_거부한다() {
        // given
        String name = "가".repeat(51);

        // when / then
        assertMemberError(
                () -> MemberFixture.회원(name, "+821012345678"),
                MemberErrorCode.MEMBER_NAME_TOO_LONG
        );
    }

    @Test
    void 회원_휴대전화_번호는_canonical_E164_형식이어야_한다() {
        // given
        String[] invalidPhoneNumbers = {"01012345678", "+8210-1234-5678", "+12025550123"};

        // when / then
        for (String invalidPhoneNumber : invalidPhoneNumbers) {
            assertMemberError(
                    () -> MemberFixture.회원("회원", invalidPhoneNumber),
                    MemberErrorCode.MEMBER_PHONE_NUMBER_INVALID
            );
        }
        assertMemberError(
                () -> MemberFixture.회원("회원", null),
                MemberErrorCode.MEMBER_PHONE_NUMBER_INVALID
        );
    }

    private void assertMemberError(Runnable action, MemberErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
