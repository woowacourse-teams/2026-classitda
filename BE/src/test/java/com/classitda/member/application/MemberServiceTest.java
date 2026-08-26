package com.classitda.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.member.fixture.MemberFixture;
import com.classitda.member.presentation.dto.MyNameUpdateRequest;
import com.classitda.member.presentation.dto.MyProfileResponse;
import com.classitda.studio.application.StudioMembershipTerminationService;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({MemberService.class, MemberServiceTest.FixedClockConfig.class, StudioMembershipTerminationService.class})
@MySqlRepositoryTest
class MemberServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 24, 15, 30);

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final StudioRepository studioRepository;
    private final EntityManager entityManager;

    @Autowired
    MemberServiceTest(
            MemberService memberService,
            MemberRepository memberRepository,
            AuthAccountRepository authAccountRepository,
            StudioRepository studioRepository,
            EntityManager entityManager
    ) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.authAccountRepository = authAccountRepository;
        this.studioRepository = studioRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 내_정보를_조회하면_이름과_번호와_소셜_이메일을_반환한다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.회원("김클래스", "01012345678"));
        소셜_계정을_저장한다(member.getId(), OauthProvider.GOOGLE, "member@example.com");

        // when
        MyProfileResponse response = memberService.findMe(member.getId());

        // then
        assertThat(response.name()).isEqualTo("김클래스");
        assertThat(response.phoneNumber()).isEqualTo("01012345678");
        assertThat(response.email()).isEqualTo("member@example.com");
    }

    @Test
    void 소셜_계정이_없으면_이메일은_null_이다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.기본_회원());

        // when
        MyProfileResponse response = memberService.findMe(member.getId());

        // then
        assertThat(response.email()).isNull();
    }

    @Test
    void 소셜_계정에_이메일이_없으면_이메일은_null_이다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.기본_회원());
        소셜_계정을_저장한다(member.getId(), OauthProvider.GOOGLE, null);

        // when
        MyProfileResponse response = memberService.findMe(member.getId());

        // then
        assertThat(response.email()).isNull();
    }

    @Test
    void 다른_회원의_소셜_이메일을_반환하지_않는다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.회원("김클래스", "01012345678"));
        Member other = memberRepository.saveAndFlush(MemberFixture.회원("이클래스", "01087654321"));
        소셜_계정을_저장한다(other.getId(), OauthProvider.GOOGLE, "other@example.com");

        // when
        MyProfileResponse response = memberService.findMe(member.getId());

        // then
        assertThat(response.email()).isNull();
    }

    @Test
    void 없는_회원의_정보를_조회하면_MEMBER_008을_던진다() {
        // when / then
        assertThatThrownBy(() -> memberService.findMe(-1L))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void 내_이름을_수정한다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.회원("김클래스", "01012345678"));

        // when
        memberService.updateName(member.getId(), MyNameUpdateRequest.from("이클래스"));
        memberRepository.flush();
        entityManager.clear();

        // then
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("이클래스");
    }

    @Test
    void 이름을_수정해도_휴대전화_번호는_그대로다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.회원("김클래스", "01012345678"));

        // when
        memberService.updateName(member.getId(), MyNameUpdateRequest.from("이클래스"));
        memberRepository.flush();
        entityManager.clear();

        // then
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getPhoneNumber()).isEqualTo("01012345678");
    }

    @Test
    void 빈_이름으로_수정하면_MEMBER_001을_던진다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.기본_회원());

        // when / then
        assertThatThrownBy(() -> memberService.updateName(member.getId(), MyNameUpdateRequest.from(" ")))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NAME_REQUIRED.getMessage());
    }

    @Test
    void 오십자를_넘는_이름으로_수정하면_MEMBER_003을_던진다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.기본_회원());
        String tooLongName = "가".repeat(51);

        // when / then
        assertThatThrownBy(() -> memberService.updateName(member.getId(), MyNameUpdateRequest.from(tooLongName)))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NAME_TOO_LONG.getMessage());
    }

    @Test
    void 탈퇴를_요청한_회원도_이름을_수정할_수_있다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.기본_회원());
        memberService.withdraw(member.getId());
        memberRepository.flush();
        entityManager.clear();

        // when
        memberService.updateName(member.getId(), MyNameUpdateRequest.from("이클래스"));
        memberRepository.flush();
        entityManager.clear();

        // then
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("이클래스");
    }

    @Test
    void 없는_회원의_이름을_수정하면_MEMBER_008을_던진다() {
        // when / then
        assertThatThrownBy(() -> memberService.updateName(-1L, MyNameUpdateRequest.from("이클래스")))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void 회원이_탈퇴하면_현재_시각과_7일_후의_정리_예정_시각을_저장한다() {
        // given
        Member member = memberRepository.saveAndFlush(MemberFixture.기본_회원());

        // when
        memberService.withdraw(member.getId());
        memberRepository.flush();
        entityManager.clear();

        // then
        Member withdrawnMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(withdrawnMember.getWithdrawalRequestedAt()).isEqualTo(NOW);
        assertThat(withdrawnMember.getCleanupScheduledAt()).isEqualTo(NOW.plusDays(7));
    }

    @Test
    void 시설_대표는_탈퇴할_수_없다() {
        // given
        Member owner = memberRepository.saveAndFlush(MemberFixture.기본_회원());
        studioRepository.saveAndFlush(StudioFixture.기본_시설(owner));

        // when / then
        assertMemberError(
                () -> memberService.withdraw(owner.getId()),
                MemberErrorCode.MEMBER_WITHDRAWAL_BLOCKED_BY_OWNED_STUDIO
        );
        assertThat(owner.getWithdrawalRequestedAt()).isNull();
    }

    @Test
    void 존재하지_않는_회원은_탈퇴할_수_없다() {
        assertMemberError(
                () -> memberService.withdraw(Long.MAX_VALUE),
                MemberErrorCode.MEMBER_NOT_FOUND
        );
    }

    private void assertMemberError(Runnable action, MemberErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(expected);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW.atZone(SERVICE_ZONE_ID).toInstant(), SERVICE_ZONE_ID);
        }
    }
    private void 소셜_계정을_저장한다(Long memberId, OauthProvider provider, String providerEmail) {
        authAccountRepository.saveAndFlush(AuthAccount.builder()
                .memberId(memberId)
                .provider(provider)
                .providerSubject("subject-" + memberId)
                .providerEmail(providerEmail)
                .build());
    }
}
