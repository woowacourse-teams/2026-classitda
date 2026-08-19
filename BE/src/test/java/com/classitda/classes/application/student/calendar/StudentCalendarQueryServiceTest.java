package com.classitda.classes.application.student.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.domain.Waiting;
import com.classitda.classes.domain.WaitingStatus;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.member.domain.Member;
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
import jakarta.persistence.EntityManagerFactory;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({
        StudentCalendarQueryService.class,
        StudentSessionAccessReader.class,
        StudentOwnedPassesReader.class,
        StudentCalendarSummaryReader.class,
        StudentCalendarQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class StudentCalendarQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDate RANGE_FROM = LocalDate.of(2026, 8, 15);
    private static final LocalDate RANGE_TO = LocalDate.of(2026, 8, 19);

    private final StudentCalendarQueryService queryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    StudentCalendarQueryServiceTest(
            StudentCalendarQueryService queryService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager,
            EntityManagerFactory entityManagerFactory
    ) {
        this.queryService = queryService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void 보유한_모든_수강권_종류의_출석_완료와_예약_확정과_대기_중_여부를_조회한다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("student-calendar-owner"), "학생 달력 시설");
        StudioMembership studentMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("student-calendar-member"),
                SystemRole.STUDENT
        );
        StudioMembership otherStudentMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("student-calendar-other"),
                SystemRole.STUDENT
        );
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("student-calendar-instructor"),
                SystemRole.INSTRUCTOR
        );
        ClassType yoga = 수업_종류를_저장한다(studio, "달력 요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "달력 필라테스");
        MemberPassProduct memberPassProduct = 보유_수강권을_저장한다(
                studentMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(yoga)),
                RANGE_FROM,
                RANGE_TO
        );
        MemberPassProduct pilatesPass = 보유_수강권을_저장한다(
                studentMembership,
                수강권을_저장한다(studio, ClassForm.INDIVIDUAL, List.of(pilates)),
                RANGE_FROM,
                RANGE_TO
        );

        ClassSession attendedSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "수강 완료", ClassForm.GROUP,
                LocalDate.of(2026, 8, 16).atTime(9, 0), ClassSessionStatus.OPENED
        );
        ClassSession futureAttendedSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "종료 전 출석 상태", ClassForm.GROUP,
                LocalDate.of(2026, 8, 19).atTime(9, 0), ClassSessionStatus.OPENED
        );
        ClassSession endedReservedSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "출석 처리 전 종료 수업", ClassForm.GROUP,
                LocalDate.of(2026, 8, 15).atTime(9, 0), ClassSessionStatus.OPENED
        );
        ClassSession reservedSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "예약 확정", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(11, 0), ClassSessionStatus.OPENED
        );
        ClassSession waitingSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "대기 중", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(12, 0), ClassSessionStatus.OPENED
        );
        ClassSession offeredSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "빈자리 제안", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(13, 0), ClassSessionStatus.OPENED
        );
        ClassSession canceledSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "취소 수업", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(14, 0), ClassSessionStatus.CANCELED
        );
        ClassSession otherClassTypeSession = 수업을_저장한다(
                studio, instructorMembership, pilates, "다른 종류", ClassForm.GROUP,
                LocalDate.of(2026, 8, 19).atTime(15, 0), ClassSessionStatus.OPENED
        );
        ClassSession otherClassFormSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "다른 형태", ClassForm.INDIVIDUAL,
                LocalDate.of(2026, 8, 17).atTime(16, 0), ClassSessionStatus.OPENED
        );
        ClassSession otherMemberSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "다른 회원 예약", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(17, 0), ClassSessionStatus.OPENED
        );
        수업을_저장한다(
                studio, instructorMembership, yoga, "예약 없는 완료 수업", ClassForm.GROUP,
                LocalDate.of(2026, 8, 16).atTime(11, 0), ClassSessionStatus.OPENED
        );
        ClassSession outOfRangeSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "기간 밖", ClassForm.GROUP,
                LocalDate.of(2026, 8, 20).atTime(11, 0), ClassSessionStatus.OPENED
        );

        예약을_저장한다(attendedSession, studentMembership, memberPassProduct, ReservationStatus.ATTENDED);
        예약을_저장한다(futureAttendedSession, studentMembership, memberPassProduct, ReservationStatus.ATTENDED);
        예약을_저장한다(endedReservedSession, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        예약을_저장한다(reservedSession, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        대기를_저장한다(waitingSession, studentMembership, WaitingStatus.WAITING);
        대기를_저장한다(offeredSession, studentMembership, WaitingStatus.OFFERED);
        예약을_저장한다(canceledSession, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        예약을_저장한다(otherClassTypeSession, studentMembership, pilatesPass, ReservationStatus.RESERVED);
        예약을_저장한다(otherClassFormSession, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        예약을_저장한다(otherMemberSession, otherStudentMembership, null, ReservationStatus.RESERVED);
        예약을_저장한다(outOfRangeSession, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        List<StudentCalendarSummary> summaries = queryService.findAll(
                studentMembership.getMember().getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        );
        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(summaries).containsExactly(
                new StudentCalendarSummary(LocalDate.of(2026, 8, 16), true, false, false),
                new StudentCalendarSummary(LocalDate.of(2026, 8, 18), false, true, true)
        );
        assertThat(queryCount).isEqualTo(6L);
    }

    @Test
    void 보유_수강권_이용_기간과_겹치는_날짜만_조회한다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("calendar-period-owner"), "달력 기간 시설");
        StudioMembership studentMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-period-member"),
                SystemRole.STUDENT
        );
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-period-instructor"),
                SystemRole.INSTRUCTOR
        );
        ClassType classType = 수업_종류를_저장한다(studio, "기간 요가");
        MemberPassProduct memberPassProduct = 보유_수강권을_저장한다(
                studentMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(classType)),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 18)
        );
        ClassSession before = 수업을_저장한다(
                studio, instructorMembership, classType, "수강권 기간 전", ClassForm.GROUP,
                LocalDate.of(2026, 8, 16).atTime(11, 0), ClassSessionStatus.OPENED
        );
        ClassSession within = 수업을_저장한다(
                studio, instructorMembership, classType, "수강권 기간 안", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(11, 0), ClassSessionStatus.OPENED
        );
        ClassSession after = 수업을_저장한다(
                studio, instructorMembership, classType, "수강권 기간 후", ClassForm.GROUP,
                LocalDate.of(2026, 8, 19).atTime(11, 0), ClassSessionStatus.OPENED
        );
        예약을_저장한다(before, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        예약을_저장한다(within, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        예약을_저장한다(after, studentMembership, memberPassProduct, ReservationStatus.RESERVED);
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudentCalendarSummary> summaries = queryService.findAll(
                studentMembership.getMember().getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        );

        // then
        assertThat(summaries).containsExactly(
                new StudentCalendarSummary(LocalDate.of(2026, 8, 18), false, true, false)
        );
    }

    @Test
    void 양끝을_포함한_42일을_허용하고_수강권_기간과_겹치지_않으면_집계하지_않는다() {
        // given
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = from.plusDays(41);
        Studio studio = 시설을_저장한다(회원을_저장한다("calendar-empty-owner"), "빈 달력 시설");
        StudioMembership studentMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-empty-member"),
                SystemRole.STUDENT
        );
        ClassType classType = 수업_종류를_저장한다(studio, "빈 달력 요가");
        보유_수강권을_저장한다(
                studentMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(classType)),
                to.plusDays(1),
                to.plusDays(30)
        );
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        List<StudentCalendarSummary> summaries = queryService.findAll(
                studentMembership.getMember().getId(),
                studio.getId(),
                from,
                to
        );
        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(summaries).isEmpty();
        assertThat(queryCount).isEqualTo(6L);
    }

    @ParameterizedTest
    @MethodSource("잘못된_조회_조건")
    void 날짜_범위가_올바르지_않으면_조회할_수_없다(
            LocalDate from,
            LocalDate to
    ) {
        // when / then
        assertThatThrownBy(() -> queryService.findAll(1L, 1L, from, to))
                .isInstanceOfSatisfying(ClassitdaException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_INPUT));
    }

    private Member 회원을_저장한다(String id) {
        Member member = StudioFixture.아이디가_다른_소유자(id);
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Studio 시설을_저장한다(Member owner, String name) {
        Studio studio = Studio.builder()
                .owner(owner)
                .name(name)
                .openTime(java.time.LocalTime.of(9, 0))
                .closeTime(java.time.LocalTime.of(22, 0))
                .build();
        entityManager.persist(studio);
        entityManager.flush();
        return studio;
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            SystemRole systemRole
    ) {
        StudioRole role = 역할을_조회하거나_저장한다(studio, systemRole);
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .name(member.getName())
                .studioRole(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
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

    private ClassType 수업_종류를_저장한다(Studio studio, String name) {
        return classTypeRepository.saveAndFlush(ClassTypeFixture.이름이_다른_수업_종류(studio, name));
    }

    private PassProduct 수강권을_저장한다(
            Studio studio,
            ClassForm classForm,
            List<ClassType> classTypes
    ) {
        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name(classForm + " 달력 수강권")
                .classForm(classForm)
                .classTypes(classTypes)
                .totalCount(10)
                .validPeriodAmount(3)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(7)
                .build();
        entityManager.persist(passProduct);
        entityManager.flush();
        return passProduct;
    }

    private MemberPassProduct 보유_수강권을_저장한다(
            StudioMembership membership,
            PassProduct passProduct,
            LocalDate startedAt,
            LocalDate expiresAt
    ) {
        MemberPassProduct memberPassProduct = MemberPassProduct.builder()
                .membership(membership)
                .passProduct(passProduct)
                .remainingCount(10)
                .remainingHoldDays(7)
                .status(MemberPassProductStatus.ACTIVE)
                .startedAt(startedAt)
                .expiresAt(expiresAt)
                .build();
        entityManager.persist(memberPassProduct);
        entityManager.flush();
        return memberPassProduct;
    }

    private ClassSession 수업을_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name,
            ClassForm classForm,
            LocalDateTime startAt,
            ClassSessionStatus status
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                classForm,
                60,
                12,
                startAt,
                status
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private void 예약을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            MemberPassProduct memberPassProduct,
            ReservationStatus status
    ) {
        entityManager.persist(Reservation.builder()
                .membership(membership)
                .classSession(classSession)
                .memberPassProduct(memberPassProduct)
                .status(status)
                .reservedAt(NOW.minusDays(1))
                .canceledAt(status == ReservationStatus.CANCELED ? NOW.minusHours(1) : null)
                .build());
    }

    private void 대기를_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            WaitingStatus status
    ) {
        entityManager.persist(Waiting.builder()
                .membership(membership)
                .classSession(classSession)
                .sequence(1)
                .status(status)
                .offeredAt(status == WaitingStatus.OFFERED ? NOW.minusMinutes(5) : null)
                .build());
    }

    private static Stream<Arguments> 잘못된_조회_조건() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        return Stream.of(
                Arguments.of(null, from),
                Arguments.of(from, null),
                Arguments.of(from.plusDays(1), from),
                Arguments.of(from, from.plusDays(42)),
                Arguments.of(LocalDate.MAX, LocalDate.MAX)
        );
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
