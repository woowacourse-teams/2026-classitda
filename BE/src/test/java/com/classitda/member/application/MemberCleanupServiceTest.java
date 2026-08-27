package com.classitda.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.fixture.AuthAccountFixture;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
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
import com.classitda.studio.application.StudioMembershipTerminationService;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlDataJpaTest;
import com.classitda.support.TestClockConfiguration;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(TestClockConfiguration.August31AtFifteenThirty.class)
@MySqlDataJpaTest
class MemberCleanupServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 31, 15, 30);

    private final MemberCleanupService memberCleanupService;
    private final MemberService memberService;
    private final StudioMembershipTerminationService studioMembershipTerminationService;
    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final EntityManager entityManager;

    @Autowired
    MemberCleanupServiceTest(
            MemberCleanupService memberCleanupService,
            MemberService memberService,
            StudioMembershipTerminationService studioMembershipTerminationService,
            MemberRepository memberRepository,
            AuthAccountRepository authAccountRepository,
            EntityManager entityManager
    ) {
        this.memberCleanupService = memberCleanupService;
        this.memberService = memberService;
        this.studioMembershipTerminationService = studioMembershipTerminationService;
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
                .extracting(StudioMembership::getPhoneNumber)
                .containsOnlyNulls();
        assertThat(memberships)
                .extracting(StudioMembership::getStatus)
                .containsOnly(MembershipStatus.WITHDRAWN);

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
                .phoneNumber(member.getPhoneNumber())
                .studioRole(studioRole)
                .name(name)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(NOW.minusMonths(3))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    @Test
    void 이력이_있는_소속을_종료하면_기록을_남기고_탈퇴_상태가_된다() {
        // given
        Member owner = 회원을_저장한다("이력 시설 대표", "01033333333");
        Member student = 회원을_저장한다("이력 회원", "01044444444");
        Studio studio = 시설을_저장한다(owner);
        StudioMembership instructorMembership = 소속을_저장한다(studio, owner, SystemRole.OWNER, "대표 강사");
        StudioMembership studentMembership = 소속을_저장한다(studio, student, SystemRole.STUDENT, "이력 별칭");
        OperationalHistoryIds historyIds = 운영_이력을_저장한다(studio, instructorMembership, studentMembership);
        entityManager.flush();

        // when
        studioMembershipTerminationService.terminate(studentMembership);
        entityManager.flush();
        entityManager.clear();

        // then
        StudioMembership terminated = entityManager.find(StudioMembership.class, studentMembership.getId());
        assertThat(terminated).isNotNull();
        assertThat(terminated.getStatus()).isEqualTo(MembershipStatus.WITHDRAWN);
        assertThat(terminated.getPhoneNumber()).isEqualTo("01044444444");

        ClassSessionEnrollment enrollment = entityManager.find(
                ClassSessionEnrollment.class, historyIds.enrollmentId());
        assertThat(enrollment.getAttendance().getResult()).isEqualTo(AttendanceResult.ATTENDED);
    }

    @Test
    void 소속을_종료하면_아직_시작하지_않은_예약이_취소된다() {
        // given
        Member owner = 회원을_저장한다("미래 시설 대표", "01055555555");
        Member student = 회원을_저장한다("미래 회원", "01066666666");
        Studio studio = 시설을_저장한다(owner);
        StudioMembership instructorMembership = 소속을_저장한다(studio, owner, SystemRole.OWNER, "대표 강사");
        StudioMembership studentMembership = 소속을_저장한다(studio, student, SystemRole.STUDENT, "미래 별칭");
        Long upcomingId = 미래_예약을_저장한다(studio, instructorMembership, studentMembership);
        entityManager.flush();

        // when
        studioMembershipTerminationService.terminate(studentMembership);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSessionEnrollment canceled = entityManager.find(ClassSessionEnrollment.class, upcomingId);
        assertThat(canceled.getState().getStatus()).isEqualTo(EnrollmentStatus.CANCELED);
    }

    @Test
    void 탈퇴를_요청하면_모든_소속이_종료된다() {
        // given
        Member owner = 회원을_저장한다("전파 시설 대표", "01077777777");
        Member student = 회원을_저장한다("전파 회원", "01088888888");
        Studio firstStudio = 시설을_저장한다(owner);
        Studio secondStudio = 시설을_저장한다(owner);
        StudioMembership firstInstructor = 소속을_저장한다(firstStudio, owner, SystemRole.OWNER, "대표 강사");
        StudioMembership secondInstructor = 소속을_저장한다(secondStudio, owner, SystemRole.OWNER, "대표 강사");
        StudioMembership first = 소속을_저장한다(firstStudio, student, SystemRole.STUDENT, "첫 별칭");
        StudioMembership second = 소속을_저장한다(secondStudio, student, SystemRole.STUDENT, "둘째 별칭");
        운영_이력을_저장한다(firstStudio, firstInstructor, first);
        운영_이력을_저장한다(secondStudio, secondInstructor, second);
        entityManager.flush();

        // when
        memberService.withdraw(student.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(entityManager.find(StudioMembership.class, first.getId()).getStatus())
                .isEqualTo(MembershipStatus.WITHDRAWN);
        assertThat(entityManager.find(StudioMembership.class, second.getId()).getStatus())
                .isEqualTo(MembershipStatus.WITHDRAWN);
    }

    private Long 미래_예약을_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            StudioMembership studentMembership
    ) {
        ClassSession classSession = ClassSession.builder()
                .studioId(studio.getId())
                .instructorMembership(instructorMembership)
                .name("다음 주 요가")
                .classForm(ClassForm.GROUP)
                .durationMinutes(60)
                .capacity(10)
                .startAt(NOW.plusDays(7))
                .build();
        entityManager.persist(classSession);

        ClassSessionEnrollment enrollment = ClassSessionEnrollment.reservedWithoutPassProduct(
                studentMembership,
                classSession,
                NOW.minusDays(1)
        );
        entityManager.persist(enrollment);
        entityManager.flush();
        return enrollment.getId();
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

}
