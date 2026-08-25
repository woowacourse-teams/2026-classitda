package com.classitda.classes.application.student.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.student.BookingAvailability;
import com.classitda.classes.application.student.StudentBookingDecisionPolicy;
import com.classitda.classes.application.student.StudentBookingRelation;
import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.daily.StudentDailySessionAssembler;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailQueryService;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.member.domain.Member;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.passproduct.domain.MemberPassProductStatus;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import({
        StudentSessionDetailQueryService.class,
        StudentSessionAccessReader.class,
        StudentOwnedPassesReader.class,
        StudentDailySessionAssembler.class,
        StudentBookingDecisionPolicy.class,
        StudentEnrollmentDetailQueryService.class,
        StudentSessionDetailQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class StudentSessionDetailQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDateTime SESSION_START_AT = NOW.toLocalDate().atTime(20, 0);
    private static final AtomicLong PHONE_SEQUENCE = new AtomicLong(50_000_000L);

    private final StudentSessionDetailQueryService queryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;

    @Autowired
    StudentSessionDetailQueryServiceTest(
            StudentSessionDetailQueryService queryService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager
    ) {
        this.queryService = queryService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 활성_신청이_없어도_예약_가능한_수업_상세를_반환한다() {
        // given
        DetailContext context = 상세_조회_환경을_만든다("reservable", 2, true);

        // when
        StudentSessionDetailView result = queryService.findOne(
                context.studentMembership().getMember().getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result.enrollment()).isNull();
        assertThat(result.classSession().bookingDecision().bookingRelation())
                .isEqualTo(StudentBookingRelation.NONE);
        assertThat(result.classSession().bookingDecision().availability())
                .contains(BookingAvailability.RESERVABLE);
        assertThat(result.instructor())
                .isEqualTo(new StudentSessionDetailView.Instructor(
                        context.instructorMembership().getId(),
                        context.instructorMembership().getName(),
                        context.instructorMembership().getMember().getProfileImageUrl(),
                        context.studio().getName()
                ));
    }

    @Test
    void 정원이_가득_찬_수업은_대기_가능으로_반환한다() {
        // given
        DetailContext context = 상세_조회_환경을_만든다("waitlistable", 1, true);
        StudioMembership otherStudent = 학생_소속을_저장한다(context.studio(), "다른 학생");
        entityManager.persist(ClassSessionEnrollment.reservedWithoutPassProduct(
                otherStudent,
                context.classSession(),
                NOW.minusHours(1)
        ));
        entityManager.flush();

        // when
        StudentSessionDetailView result = queryService.findOne(
                context.studentMembership().getMember().getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result.classSession().reservedCount()).isEqualTo(1);
        assertThat(result.classSession().remainingCapacity()).isZero();
        assertThat(result.classSession().bookingDecision().availability())
                .contains(BookingAvailability.WAITLISTABLE);
    }

    @Test
    void 활성_대기_신청이_있으면_신청_정보와_대기_순서를_반환한다() {
        // given
        DetailContext context = 상세_조회_환경을_만든다("waiting", 1, true);
        StudioMembership earlierStudent = 학생_소속을_저장한다(context.studio(), "앞선 대기 학생");
        entityManager.persist(ClassSessionEnrollment.waiting(
                earlierStudent,
                context.classSession(),
                NOW.minusHours(2)
        ));
        ClassSessionEnrollment ownEnrollment = ClassSessionEnrollment.waiting(
                context.studentMembership(),
                context.classSession(),
                NOW.minusHours(1)
        );
        entityManager.persist(ownEnrollment);
        entityManager.flush();

        // when
        StudentSessionDetailView result = queryService.findOne(
                context.studentMembership().getMember().getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result.enrollment().id()).isEqualTo(ownEnrollment.getId());
        assertThat(result.enrollment().waitingPosition()).isEqualTo(2L);
        assertThat(result.classSession().bookingDecision().bookingRelation())
                .isEqualTo(StudentBookingRelation.WAITING);
        assertThat(result.classSession().bookingDecision().availability()).isEmpty();
    }

    @Test
    void 강사가_수강권_없이_등록한_활성_예약도_반환한다() {
        // given
        DetailContext context = 상세_조회_환경을_만든다("instructor-enrollment", 2, false);
        ClassSessionEnrollment ownEnrollment = ClassSessionEnrollment.reservedWithoutPassProduct(
                context.studentMembership(),
                context.classSession(),
                NOW.minusHours(1)
        );
        entityManager.persist(ownEnrollment);
        entityManager.flush();

        // when
        StudentSessionDetailView result = queryService.findOne(
                context.studentMembership().getMember().getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result.enrollment().id()).isEqualTo(ownEnrollment.getId());
        assertThat(result.enrollment().usedPass()).isNull();
        assertThat(result.classSession().bookingDecision().bookingRelation())
                .isEqualTo(StudentBookingRelation.RESERVED);
        assertThat(result.classSession().bookingDecision().availability()).isEmpty();
    }

    @Test
    void 취소된_수업과_호환_수강권이_없는_수업과_다른_시설_수업은_상세에서_숨긴다() {
        // given
        DetailContext canceled = 상세_조회_환경을_만든다("canceled", 2, true);
        canceled.classSession().cancel(NOW.minusMinutes(1));
        DetailContext uncovered = 상세_조회_환경을_만든다("uncovered", 2, false);
        DetailContext accessible = 상세_조회_환경을_만든다("accessible", 2, true);
        DetailContext otherStudio = 상세_조회_환경을_만든다("other-studio", 2, true);
        entityManager.flush();

        // when / then
        assertSessionNotFound(() -> queryService.findOne(
                canceled.studentMembership().getMember().getId(),
                canceled.studio().getId(),
                canceled.classSession().getId()
        ));
        assertSessionNotFound(() -> queryService.findOne(
                uncovered.studentMembership().getMember().getId(),
                uncovered.studio().getId(),
                uncovered.classSession().getId()
        ));
        assertSessionNotFound(() -> queryService.findOne(
                accessible.studentMembership().getMember().getId(),
                accessible.studio().getId(),
                otherStudio.classSession().getId()
        ));
    }

    private DetailContext 상세_조회_환경을_만든다(String suffix, int capacity, boolean ownsCompatiblePass) {
        Member owner = 회원을_저장한다("대표-" + suffix, null);
        Studio studio = 시설을_저장한다(owner, "상세 조회 시설-" + suffix);
        정책을_저장한다(studio);
        StudioMembership studentMembership = 학생_소속을_저장한다(studio, "학생-" + suffix);
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다(
                        "강사 회원-" + suffix,
                        "https://images.example.com/instructor-" + suffix + ".png"
                ),
                SystemRole.INSTRUCTOR,
                "표시 강사-" + suffix
        );
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "요가-" + suffix)
        );
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                "저녁 요가-" + suffix,
                "개인 수건을 준비해 주세요.",
                ClassForm.GROUP,
                60,
                capacity,
                SESSION_START_AT
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        if (ownsCompatiblePass) {
            수강권을_저장한다(studentMembership, studio, classType, suffix);
        }

        return new DetailContext(
                studio,
                studentMembership,
                instructorMembership,
                classSession
        );
    }

    private Member 회원을_저장한다(String name, String profileImageUrl) {
        Member member = Member.builder()
                .name(name)
                .phoneNumber("010%08d".formatted(PHONE_SEQUENCE.getAndIncrement()))
                .profileImageUrl(profileImageUrl)
                .build();
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Studio 시설을_저장한다(Member owner, String name) {
        Studio studio = Studio.builder()
                .owner(owner)
                .name(name)
                .address(StudioFixture.기본_주소())
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();
        entityManager.persist(studio);
        entityManager.flush();
        return studio;
    }

    private StudioMembership 학생_소속을_저장한다(Studio studio, String name) {
        return 소속을_저장한다(
                studio,
                회원을_저장한다(name, null),
                SystemRole.STUDENT,
                name
        );
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            SystemRole systemRole,
            String name
    ) {
        StudioRole role = 역할을_조회하거나_저장한다(studio, systemRole);
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .studioRole(role)
                .status(MembershipStatus.ACTIVE)
                .name(name)
                .joinedAt(NOW.minusMonths(1))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    private StudioRole 역할을_조회하거나_저장한다(Studio studio, SystemRole systemRole) {
        List<StudioRole> roles = entityManager.createQuery("""
                        SELECT role
                        FROM StudioRole role
                        WHERE role.studio.id = :studioId
                          AND role.systemRole = :systemRole
                        """, StudioRole.class)
                .setParameter("studioId", studio.getId())
                .setParameter("systemRole", systemRole)
                .getResultList();
        if (!roles.isEmpty()) {
            return roles.getFirst();
        }

        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private void 정책을_저장한다(Studio studio) {
        entityManager.persist(StudioPolicy.builder()
                .studio(studio)
                .reservationCloseMinutesBefore(30)
                .freeCancelMinutesBefore(60)
                .waitingOfferResponseMinutes(10)
                .build());
        entityManager.flush();
    }

    private void 수강권을_저장한다(
            StudioMembership membership,
            Studio studio,
            ClassType classType,
            String suffix
    ) {
        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name("그룹 수강권-" + suffix)
                .classForm(ClassForm.GROUP)
                .classTypes(List.of(classType))
                .totalCount(10)
                .validPeriodAmount(3)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(7)
                .build();
        entityManager.persist(passProduct);
        entityManager.persist(MemberPassProduct.builder()
                .membership(membership)
                .passProduct(passProduct)
                .remainingCount(10)
                .remainingHoldDays(7)
                .status(MemberPassProductStatus.ACTIVE)
                .startedAt(NOW.toLocalDate().minusMonths(1))
                .expiresAt(NOW.toLocalDate().plusMonths(1))
                .build());
        entityManager.flush();
    }

    private void assertSessionNotFound(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
    }

    private record DetailContext(
            Studio studio,
            StudioMembership studentMembership,
            StudioMembership instructorMembership,
            ClassSession classSession
    ) {
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Primary
        @Bean
        Clock clock() {
            return Clock.fixed(NOW.atZone(SERVICE_ZONE_ID).toInstant(), SERVICE_ZONE_ID);
        }
    }
}
