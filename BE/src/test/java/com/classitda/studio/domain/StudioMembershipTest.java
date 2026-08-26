package com.classitda.studio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.member.domain.Member;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class StudioMembershipTest {

    @Test
    void 소속을_생성하면_시설이_부르는_이름을_가진다() {
        // given
        Studio studio = 기본_시설();

        // when
        StudioMembership studioMembership = 소속을_만든다(studio, "김철수");

        // then
        assertThat(studioMembership.getName()).isEqualTo("김철수");
        assertThat(studioMembership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    void 이름이_null이면_소속을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> 소속을_만든다(studio, null))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.INVALID_MEMBERSHIP_NAME));
    }

    @Test
    void 이름이_공백이면_소속을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> 소속을_만든다(studio, "   "))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.INVALID_MEMBERSHIP_NAME));
    }

    @Test
    void 이름이_51자면_소속을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> 소속을_만든다(studio, "가".repeat(51)))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.INVALID_MEMBERSHIP_NAME));
    }

    @Test
    void 이름이_1자면_소속을_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();

        // when
        StudioMembership studioMembership = 소속을_만든다(studio, "김");

        // then
        assertThat(studioMembership.getName()).hasSize(1);
    }

    @Test
    void 이름이_50자면_소속을_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();

        // when
        StudioMembership studioMembership = 소속을_만든다(studio, "가".repeat(50));

        // then
        assertThat(studioMembership.getName()).hasSize(50);
    }

    @Test
    void 개인정보를_정리하면_이름과_번호를_지우고_탈퇴_상태로_바꾼다() {
        // given
        StudioMembership studioMembership = 소속을_만든다(기본_시설(), "김철수");

        // when
        studioMembership.clearPersonalInformation();

        // then
        assertThat(studioMembership.getName()).isEqualTo(Member.WITHDRAWN_MEMBER_NAME);
        assertThat(studioMembership.getPhoneNumber()).isNull();
        assertThat(studioMembership.getStatus()).isEqualTo(MembershipStatus.WITHDRAWN);
    }

    @Test
    void 강사_역할이면_강사로_판정한다() {
        // given
        Studio studio = 기본_시설();

        // when
        StudioMembership instructorMembership = 역할이_다른_소속을_만든다(studio, SystemRole.INSTRUCTOR);
        StudioMembership studentMembership = 역할이_다른_소속을_만든다(studio, SystemRole.STUDENT);

        // then
        assertThat(instructorMembership.isInstructor()).isTrue();
        assertThat(studentMembership.isInstructor()).isFalse();
    }

    private Studio 기본_시설() {
        Member owner = StudioFixture.기본_소유자();
        return StudioFixture.기본_시설(owner);
    }

    private StudioMembership 소속을_만든다(Studio studio, String name) {
        return StudioMembership.builder()
                .studio(studio)
                .member(StudioFixture.기본_소유자())
                .phoneNumber(StudioFixture.기본_소유자().getPhoneNumber())
                .studioRole(SystemRole.STUDENT.toStudioRole(studio))
                .name(name)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    private StudioMembership 역할이_다른_소속을_만든다(Studio studio, SystemRole systemRole) {
        return StudioMembership.builder()
                .studio(studio)
                .member(StudioFixture.기본_소유자())
                .phoneNumber(StudioFixture.기본_소유자().getPhoneNumber())
                .studioRole(systemRole.toStudioRole(studio))
                .name("김철수")
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
