package com.classitda.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.fixture.AuthAccountFixture;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.passproduct.domain.MemberPassProductStatus;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({MemberCleanupService.class, MemberCleanupServiceTest.FixedClockConfig.class})
@MySqlRepositoryTest
class MemberCleanupServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 31, 15, 30);

    private final MemberCleanupService memberCleanupService;
    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final EntityManager entityManager;

    @Autowired
    MemberCleanupServiceTest(
            MemberCleanupService memberCleanupService,
            MemberRepository memberRepository,
            AuthAccountRepository authAccountRepository,
            EntityManager entityManager
    ) {
        this.memberCleanupService = memberCleanupService;
        this.memberRepository = memberRepository;
        this.authAccountRepository = authAccountRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 정리_예정_시각이_되면_개인정보와_인증_계정을_정리하고_운영_이력을_유지한다() {
        // given
        Member owner = 회원을_저장한다("시설 대표", "01011111111");
        Member withdrawnMember = 탈퇴_회원을_저장한다(
                "탈퇴 대상",
                "01022222222",
                "https://example.com/profile.png",
                NOW.minusDays(7)
        );
        authAccountRepository.saveAndFlush(AuthAccountFixture.인증_계정(
                withdrawnMember.getId(),
                "withdrawn-google-subject",
                "withdrawn@example.com"
        ));

        Studio firstStudio = 시설을_저장한다(owner);
        Studio secondStudio = 시설을_저장한다(owner);
        StudioMembership instructorMembership = 소속을_저장한다(
                firstStudio,
                owner,
                SystemRole.OWNER,
                "대표 강사"
        );
        StudioMembership firstMembership = 소속을_저장한다(
                firstStudio,
                withdrawnMember,
                SystemRole.STUDENT,
                "첫 번째 시설 별칭"
        );
        StudioMembership secondMembership = 소속을_저장한다(
                secondStudio,
                withdrawnMember,
                SystemRole.STUDENT,
                "두 번째 시설 별칭"
        );
        OperationalHistoryIds historyIds = 운영_이력을_저장한다(
                firstStudio,
                instructorMembership,
                firstMembership
        );
        entityManager.flush();

        // when
        int firstProcessedCount = memberCleanupService.cleanupDueMembers(10);
        int secondProcessedCount = memberCleanupService.cleanupDueMembers(10);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(firstProcessedCount).isEqualTo(1);
        assertThat(secondProcessedCount).isZero();

        Member cleanedMember = memberRepository.findById(withdrawnMember.getId()).orElseThrow();
        assertThat(cleanedMember.getName()).isEqualTo(Member.WITHDRAWN_MEMBER_NAME);
        assertThat(cleanedMember.getPhoneNumber()).isNull();
        assertThat(cleanedMember.getProfileImageUrl()).isNull();
        assertThat(cleanedMember.getCleanedUpAt()).isEqualTo(NOW);
        assertThat(authAccountRepository.existsByMemberId(withdrawnMember.getId())).isFalse();

        List<StudioMembership> memberships = entityManager.createQuery("""
                        SELECT membership
                        FROM StudioMembership membership
                        WHERE membership.member.id = :memberId
                        ORDER BY membership.id
                        """, StudioMembership.class)
                .setParameter("memberId", withdrawnMember.getId())
                .getResultList();
        assertThat(memberships)
                .extracting(StudioMembership::getName)
                .containsExactly(Member.WITHDRAWN_MEMBER_NAME, Member.WITHDRAWN_MEMBER_NAME);
        assertThat(memberships)
                .extracting(StudioMembership::getStatus)
                .containsOnly(MembershipStatus.ACTIVE);

        MemberPassProduct memberPassProduct = entityManager.find(
                MemberPassProduct.class,
                historyIds.memberPassProductId()
        );
        assertThat(memberPassProduct.getMembership().getId()).isEqualTo(firstMembership.getId());
        assertThat(memberPassProduct.getStatus()).isEqualTo(MemberPassProductStatus.ACTIVE);

        ClassSessionEnrollment enrollment = entityManager.find(
                ClassSessionEnrollment.class,
                historyIds.enrollmentId()
        );
        assertThat(enrollment.getMembership().getId()).isEqualTo(firstMembership.getId());
        assertThat(enrollment.getMemberPassProduct().getId()).isEqualTo(memberPassProduct.getId());
        assertThat(enrollment.getAttendance().getResult()).isEqualTo(AttendanceResult.ATTENDED);
    }

    @Test
    void 정리_예정_시각_전에는_개인정보와_인증_계정을_유지한다() {
        // given
        Member withdrawnMember = 탈퇴_회원을_저장한다(
                "보관 중인 회원",
                "01033333333",
                "https://example.com/profile.png",
                NOW.minusDays(7).plusSeconds(1)
        );
        authAccountRepository.saveAndFlush(AuthAccountFixture.인증_계정(
                withdrawnMember.getId(),
                "retained-google-subject",
                "retained@example.com"
        ));

        // when
        int processedCount = memberCleanupService.cleanupDueMembers(10);
        entityManager.flush();
        entityManager.clear();

        // then
        Member retainedMember = memberRepository.findById(withdrawnMember.getId()).orElseThrow();
        assertThat(processedCount).isZero();
        assertThat(retainedMember.getName()).isEqualTo("보관 중인 회원");
        assertThat(retainedMember.getPhoneNumber()).isEqualTo("01033333333");
        assertThat(retainedMember.getProfileImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(retainedMember.getCleanedUpAt()).isNull();
        assertThat(authAccountRepository.existsByMemberId(withdrawnMember.getId())).isTrue();
    }

    @Test
    void 한_번에_배치_크기만큼만_정리한다() {
        // given
        Member firstMember = 탈퇴_회원을_저장한다(
                "첫 번째 회원",
                "01044444444",
                null,
                NOW.minusDays(8)
        );
        Member secondMember = 탈퇴_회원을_저장한다(
                "두 번째 회원",
                "01055555555",
                null,
                NOW.minusDays(7)
        );

        // when
        int processedCount = memberCleanupService.cleanupDueMembers(1);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(processedCount).isEqualTo(1);
        assertThat(memberRepository.findById(firstMember.getId()).orElseThrow().isCleanedUp()).isTrue();
        assertThat(memberRepository.findById(secondMember.getId()).orElseThrow().isCleanedUp()).isFalse();
    }

    @Test
    void 배치_크기가_1보다_작으면_정리할_수_없다() {
        assertThatThrownBy(() -> memberCleanupService.cleanupDueMembers(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 개인정보 정리 배치 크기는 1 이상이어야 합니다.");
    }

    private Member 회원을_저장한다(String name, String phoneNumber) {
        Member member = Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .build();
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Member 탈퇴_회원을_저장한다(
            String name,
            String phoneNumber,
            String profileImageUrl,
            LocalDateTime requestedAt
    ) {
        Member member = Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .profileImageUrl(profileImageUrl)
                .build();
        member.withdraw(requestedAt);
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Studio 시설을_저장한다(Member owner) {
        Studio studio = StudioFixture.기본_시설(owner);
        entityManager.persist(studio);
        entityManager.flush();
        return studio;
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            SystemRole systemRole,
            String name
    ) {
        StudioRole studioRole = systemRole.toStudioRole(studio);
        entityManager.persist(studioRole);
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .studioRole(studioRole)
                .name(name)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(NOW.minusMonths(3))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    private OperationalHistoryIds 운영_이력을_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            StudioMembership studentMembership
    ) {
        ClassType classType = ClassType.builder()
                .studio(studio)
                .name("요가")
                .build();
        entityManager.persist(classType);

        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name("10회권")
                .classForm(ClassForm.GROUP)
                .classTypes(List.of(classType))
                .totalCount(10)
                .validPeriodAmount(3)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(7)
                .build();
        entityManager.persist(passProduct);

        MemberPassProduct memberPassProduct = MemberPassProduct.builder()
                .membership(studentMembership)
                .passProduct(passProduct)
                .remainingCount(9)
                .remainingHoldDays(7)
                .status(MemberPassProductStatus.ACTIVE)
                .startedAt(LocalDate.from(NOW).minusMonths(1))
                .expiresAt(LocalDate.from(NOW).plusMonths(2))
                .build();
        entityManager.persist(memberPassProduct);

        ClassSession classSession = ClassSession.builder()
                .studioId(studio.getId())
                .instructorMembership(instructorMembership)
                .name("저녁 요가")
                .classForm(ClassForm.GROUP)
                .durationMinutes(60)
                .capacity(10)
                .startAt(NOW.minusDays(1))
                .build();
        entityManager.persist(classSession);

        ClassSessionEnrollment enrollment = ClassSessionEnrollment.reserved(
                studentMembership,
                classSession,
                memberPassProduct,
                NOW.minusDays(2)
        );
        enrollment.markAttended(classSession.getEndAt());
        entityManager.persist(enrollment);
        entityManager.flush();
        return new OperationalHistoryIds(memberPassProduct.getId(), enrollment.getId());
    }

    private record OperationalHistoryIds(Long memberPassProductId, Long enrollmentId) {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW.atZone(SERVICE_ZONE_ID).toInstant(), SERVICE_ZONE_ID);
        }
    }
}
