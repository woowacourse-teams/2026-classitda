package com.classitda.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.member.fixture.MemberFixture;
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

@Import({MemberService.class, MemberServiceTest.FixedClockConfig.class})
@MySqlRepositoryTest
class MemberServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 24, 15, 30);

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final StudioRepository studioRepository;
    private final EntityManager entityManager;

    @Autowired
    MemberServiceTest(
            MemberService memberService,
            MemberRepository memberRepository,
            StudioRepository studioRepository,
            EntityManager entityManager
    ) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.studioRepository = studioRepository;
        this.entityManager = entityManager;
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
}
