package com.classitda.classes.application.student.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
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
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
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
    void 보유한_모든_수강권_종류의_지난_예약과_예약_확정과_대기_중_여부를_조회한다() {
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
                LocalDate.of(2026, 8, 16).atTime(9, 0)
        );
        ClassSession endedReservedSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "출석 처리 전 종료 수업", ClassForm.GROUP,
                LocalDate.of(2026, 8, 15).atTime(9, 0)
        );
        ClassSession absentSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "결석", ClassForm.GROUP,
                LocalDate.of(2026, 8, 17).atTime(8, 0)
        );
        ClassSession startedWaitingSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "시작한 수업 대기", ClassForm.GROUP,
                LocalDate.of(2026, 8, 17).atTime(9, 0)
        );
        ClassSession reservedSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "예약 확정", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(11, 0)
        );
        ClassSession waitingSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "대기 중", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(12, 0)
        );
        ClassSession offeredSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "빈자리 제안", ClassForm.GROUP,
                LocalDate.of(2026, 8, 19).atTime(13, 0)
        );
        ClassSession canceledSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "취소 수업", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(14, 0)
        );
        canceledSession.cancel(canceledSession.getStartAt().minusMinutes(1));
        ClassSession otherClassTypeSession = 수업을_저장한다(
                studio, instructorMembership, pilates, "다른 종류", ClassForm.GROUP,
                LocalDate.of(2026, 8, 19).atTime(15, 0)
        );
        ClassSession otherClassFormSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "다른 형태", ClassForm.INDIVIDUAL,
                LocalDate.of(2026, 8, 17).atTime(16, 0)
        );
        ClassSession otherMemberSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "다른 회원 예약", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(17, 0)
        );
        수업을_저장한다(
                studio, instructorMembership, yoga, "예약 없는 완료 수업", ClassForm.GROUP,
                LocalDate.of(2026, 8, 16).atTime(11, 0)
        );
        ClassSession outOfRangeSession = 수업을_저장한다(
                studio, instructorMembership, yoga, "기간 밖", ClassForm.GROUP,
                LocalDate.of(2026, 8, 20).atTime(11, 0)
        );

        예약_신청을_저장한다(attendedSession, studentMembership, memberPassProduct, AttendanceResult.ATTENDED);
        예약_신청을_저장한다(endedReservedSession, studentMembership, memberPassProduct);
        예약_신청을_저장한다(absentSession, studentMembership, memberPassProduct, AttendanceResult.ABSENT);
        대기_신청을_저장한다(startedWaitingSession, studentMembership);
        예약_신청을_저장한다(reservedSession, studentMembership, memberPassProduct);
        대기_신청을_저장한다(waitingSession, studentMembership);
        제안_신청을_저장한다(offeredSession, studentMembership);
        예약_신청을_저장한다(canceledSession, studentMembership, memberPassProduct);
        예약_신청을_저장한다(otherClassTypeSession, studentMembership, pilatesPass);
        예약_신청을_저장한다(otherClassFormSession, studentMembership, memberPassProduct);
        대기_신청을_저장한다(otherMemberSession, otherStudentMembership);
        예약_신청을_저장한다(outOfRangeSession, studentMembership, memberPassProduct);
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
                StudentCalendarSummary.of(LocalDate.of(2026, 8, 15), true, false, false),
                StudentCalendarSummary.of(LocalDate.of(2026, 8, 16), true, false, false),
                StudentCalendarSummary.of(LocalDate.of(2026, 8, 17), true, false, false),
                StudentCalendarSummary.of(LocalDate.of(2026, 8, 18), false, true, true)
        );
        assertThat(queryCount).isEqualTo(5L);
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
                LocalDate.of(2026, 8, 16).atTime(11, 0)
        );
        ClassSession within = 수업을_저장한다(
                studio, instructorMembership, classType, "수강권 기간 안", ClassForm.GROUP,
                LocalDate.of(2026, 8, 18).atTime(11, 0)
        );
        ClassSession after = 수업을_저장한다(
                studio, instructorMembership, classType, "수강권 기간 후", ClassForm.GROUP,
                LocalDate.of(2026, 8, 19).atTime(11, 0)
        );
        예약_신청을_저장한다(before, studentMembership, memberPassProduct);
        예약_신청을_저장한다(within, studentMembership, memberPassProduct);
        예약_신청을_저장한다(after, studentMembership, memberPassProduct);
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
                StudentCalendarSummary.of(LocalDate.of(2026, 8, 18), false, true, false)
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
        assertThat(queryCount).isEqualTo(5L);
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
                .address(StudioFixture.기본_주소())
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
                .phoneNumber(member.getPhoneNumber())
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
            LocalDateTime startAt
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                classForm,
                60,
                12,
                startAt
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private void 예약_신청을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            MemberPassProduct memberPassProduct
    ) {
        예약_신청을_저장한다(
                classSession,
                membership,
                memberPassProduct,
                AttendanceResult.NOT_RECORDED
        );
    }

    private void 예약_신청을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            MemberPassProduct memberPassProduct,
            AttendanceResult attendanceResult
    ) {
        ClassSessionEnrollment enrollment = ClassSessionEnrollment.reserved(
                membership,
                classSession,
                memberPassProduct,
                NOW.minusDays(1)
        );
        if (attendanceResult == AttendanceResult.ATTENDED) {
            enrollment.markAttended(classSession.getEndAt());
        } else if (attendanceResult == AttendanceResult.ABSENT) {
            enrollment.markAbsent(classSession.getEndAt());
        }
        entityManager.persist(enrollment);
    }

    private void 대기_신청을_저장한다(ClassSession classSession, StudioMembership membership) {
        entityManager.persist(ClassSessionEnrollment.waiting(membership, classSession, NOW.minusDays(1)));
    }

    private void 제안_신청을_저장한다(ClassSession classSession, StudioMembership membership) {
        ClassSessionEnrollment enrollment = ClassSessionEnrollment.waiting(
                membership,
                classSession,
                NOW.minusDays(1)
        );
        enrollment.offer(NOW.minusMinutes(5), NOW.plusMinutes(5));
        entityManager.persist(enrollment);
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
