package com.classitda.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.member.fixture.MemberFixture;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void 회원_이름은_50자까지_허용한다() {
        // given
        String name = "가".repeat(50);

        // when
        Member member = MemberFixture.회원(name, "01012345678");

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
                    () -> MemberFixture.회원(invalidName, "01012345678"),
                    MemberErrorCode.MEMBER_NAME_REQUIRED
            );
        }
        assertMemberError(
                () -> MemberFixture.회원(null, "01012345678"),
                MemberErrorCode.MEMBER_NAME_REQUIRED
        );
    }

    @Test
    void 회원_이름이_50자를_초과하면_거부한다() {
        // given
        String name = "가".repeat(51);

        // when / then
        assertMemberError(
                () -> MemberFixture.회원(name, "01012345678"),
                MemberErrorCode.MEMBER_NAME_TOO_LONG
        );
    }

    @Test
    void 회원_휴대전화_번호는_010으로_시작하는_숫자_11자리여야_한다() {
        // given
        String[] invalidPhoneNumbers = {"01112345678", "0101234567", "010-1234-5678", "+821012345678"};

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

    @Test
    void 탈퇴를_요청하면_요청_시각과_7일_후의_정리_예정_시각을_기록한다() {
        // given
        Member member = MemberFixture.기본_회원();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 8, 24, 15, 30);

        // when
        member.withdraw(requestedAt);

        // then
        assertThat(member.getWithdrawalRequestedAt()).isEqualTo(requestedAt);
        assertThat(member.getCleanupScheduledAt()).isEqualTo(requestedAt.plusDays(7));
        assertThat(member.isWithdrawalPending()).isTrue();
        assertThat(member.isCleanedUp()).isFalse();
    }

    @Test
    void 중복_탈퇴_요청은_최초_요청_시각과_정리_예정_시각을_유지한다() {
        // given
        Member member = MemberFixture.기본_회원();
        LocalDateTime firstRequestedAt = LocalDateTime.of(2026, 8, 24, 15, 30);
        member.withdraw(firstRequestedAt);

        // when
        member.withdraw(firstRequestedAt.plusDays(1));

        // then
        assertThat(member.getWithdrawalRequestedAt()).isEqualTo(firstRequestedAt);
        assertThat(member.getCleanupScheduledAt()).isEqualTo(firstRequestedAt.plusDays(7));
    }

    @Test
    void 탈퇴_요청_시각은_필수다() {
        // given
        Member member = MemberFixture.기본_회원();

        // when / then
        assertMemberError(
                () -> member.withdraw(null),
                MemberErrorCode.MEMBER_WITHDRAWAL_REQUESTED_AT_REQUIRED
        );
        assertThat(member.isWithdrawalPending()).isFalse();
    }

    @Test
    void 정리_예정_시각_전에는_개인정보를_정리할_수_없다() {
        // given
        Member member = MemberFixture.기본_회원();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 8, 24, 15, 30);
        member.withdraw(requestedAt);

        // when / then
        assertMemberError(
                () -> member.clearPersonalInformation(requestedAt.plusDays(7).minusNanos(1)),
                MemberErrorCode.MEMBER_CLEANUP_NOT_DUE
        );
        assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(member.isCleanedUp()).isFalse();
    }

    @Test
    void 개인정보_정리_시각은_필수다() {
        // given
        Member member = MemberFixture.기본_회원();
        member.withdraw(LocalDateTime.of(2026, 8, 24, 15, 30));

        // when / then
        assertMemberError(
                () -> member.clearPersonalInformation(null),
                MemberErrorCode.MEMBER_CLEANUP_OCCURRED_AT_REQUIRED
        );
        assertThat(member.isCleanedUp()).isFalse();
    }

    @Test
    void 탈퇴를_요청하지_않은_회원의_개인정보는_정리할_수_없다() {
        // given
        Member member = MemberFixture.기본_회원();

        // when / then
        assertMemberError(
                () -> member.clearPersonalInformation(LocalDateTime.of(2026, 8, 31, 15, 30)),
                MemberErrorCode.MEMBER_WITHDRAWAL_REQUIRED
        );
        assertThat(member.isCleanedUp()).isFalse();
    }

    @Test
    void 정리_예정_시각부터_개인정보를_정리하고_최초_정리_시각을_유지한다() {
        // given
        Member member = Member.builder()
                .name("회원")
                .phoneNumber("01012345678")
                .profileImageUrl("https://example.com/profile.png")
                .build();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 8, 24, 15, 30);
        LocalDateTime cleanedUpAt = requestedAt.plusDays(7);
        member.withdraw(requestedAt);

        // when
        member.clearPersonalInformation(cleanedUpAt);
        member.clearPersonalInformation(cleanedUpAt.plusHours(1));

        // then
        assertThat(member.getName()).isEqualTo(Member.WITHDRAWN_MEMBER_NAME);
        assertThat(member.getPhoneNumber()).isNull();
        assertThat(member.getProfileImageUrl()).isNull();
        assertThat(member.getCleanedUpAt()).isEqualTo(cleanedUpAt);
        assertThat(member.isWithdrawalPending()).isFalse();
        assertThat(member.isCleanedUp()).isTrue();
    }

    private void assertMemberError(Runnable action, MemberErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
