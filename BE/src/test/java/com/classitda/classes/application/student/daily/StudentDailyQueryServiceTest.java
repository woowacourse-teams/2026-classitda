package com.classitda.classes.application.student.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.classitda.classes.application.student.BookingAvailability;
import com.classitda.classes.application.student.StudentBookingDecision;
import com.classitda.classes.application.student.StudentBookingDecisionPolicy;
import com.classitda.classes.application.student.StudentBookingRelation;
import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
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
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({
        StudentDailyQueryService.class,
        StudentSessionAccessReader.class,
        StudentOwnedPassesReader.class,
        StudentDailyScheduleReader.class,
        StudentDailySessionAssembler.class,
        StudentBookingDecisionPolicy.class,
        StudentDailyQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class StudentDailyQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDate QUERY_DATE = LocalDate.of(2026, 8, 17);

    private final StudentDailyQueryService studentDailyQueryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    StudentDailyQueryServiceTest(
            StudentDailyQueryService studentDailyQueryService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager,
            EntityManagerFactory entityManagerFactory
    ) {
        this.studentDailyQueryService = studentDailyQueryService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void 선택한_수강권으로_이용할_수_있는_일별_수업을_정렬하고_인원과_상태를_반환한다() {
        // given
        Member owner = 회원을_저장한다("daily-owner");
        Studio studio = 시설을_저장한다(owner, "회원용 일별 조회 시설");
        정책을_저장한다(studio, 30);
        StudioMembership memberMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership firstInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-first-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE,
                "시설 표시 첫 강사"
        );
        StudioMembership secondInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-second-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "필라테스");
        보유_수강권을_저장한다(
                memberMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(yoga)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusMonths(1),
                QUERY_DATE.plusMonths(1)
        );

        ClassSession laterSession = 수업_회차를_저장한다(
                studio, firstInstructor, yoga, "오후 요가", ClassForm.GROUP, 1,
                QUERY_DATE.atTime(14, 0)
        );
        ClassSession sameTimeFirst = 수업_회차를_저장한다(
                studio, firstInstructor, yoga, "정오 요가 A", ClassForm.GROUP, 3,
                QUERY_DATE.atTime(12, 0)
        );
        ClassSession sameTimeSecond = 수업_회차를_저장한다(
                studio, secondInstructor, yoga, "정오 요가 B", ClassForm.GROUP, 2,
                QUERY_DATE.atTime(12, 0)
        );
        수업_회차를_저장한다(
                studio, firstInstructor, pilates, "다른 종류", ClassForm.GROUP, 10,
                QUERY_DATE.atTime(13, 0)
        );
        ClassSession sameTypeOtherForm = 수업_회차를_저장한다(
                studio, firstInstructor, yoga, "다른 형태", ClassForm.INDIVIDUAL, 1,
                QUERY_DATE.atTime(15, 0)
        );
        수업_회차를_저장한다(
                studio, firstInstructor, yoga, "다른 날짜", ClassForm.GROUP, 10,
                QUERY_DATE.plusDays(1).atStartOfDay()
        );

        StudioMembership firstParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-first-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership secondParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-second-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership thirdParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-third-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership fourthParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-fourth-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership fifthParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-fifth-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        신청을_저장한다(sameTimeFirst, firstParticipant, EnrollmentStatus.RESERVED);
        신청을_저장한다(sameTimeFirst, secondParticipant, EnrollmentStatus.RESERVED);
        신청을_저장한다(sameTimeFirst, thirdParticipant, EnrollmentStatus.CANCELED);
        신청을_저장한다(sameTimeFirst, fourthParticipant, EnrollmentStatus.WAITING);
        신청을_저장한다(sameTimeFirst, fifthParticipant, EnrollmentStatus.OFFERED);
        신청을_저장한다(laterSession, firstParticipant, EnrollmentStatus.RESERVED);
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        List<StudentDailySessionView> responses = studentDailyQueryService.findAll(
                memberMembership.getMember().getId(),
                studio.getId(),
                QUERY_DATE
        );
        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(responses).containsExactly(
                new StudentDailySessionView(
                        sameTimeFirst.getId(),
                        null,
                        firstInstructor.getId(),
                        firstInstructor.getName(),
                        ClassForm.GROUP,
                        yoga.getId(),
                        yoga.getName(),
                        "정오 요가 A",
                        "정오 요가 A 안내",
                        3,
                        3,
                        0,
                        1,
                        QUERY_DATE.atTime(12, 0),
                        QUERY_DATE.atTime(13, 0),
                        decision(StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.WAITLISTABLE)
                ),
                new StudentDailySessionView(
                        sameTimeSecond.getId(),
                        null,
                        secondInstructor.getId(),
                        secondInstructor.getMember().getName(),
                        ClassForm.GROUP,
                        yoga.getId(),
                        yoga.getName(),
                        "정오 요가 B",
                        "정오 요가 B 안내",
                        2,
                        0,
                        2,
                        0,
                        QUERY_DATE.atTime(12, 0),
                        QUERY_DATE.atTime(13, 0),
                        decision(StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.RESERVABLE)
                ),
                new StudentDailySessionView(
                        laterSession.getId(),
                        null,
                        firstInstructor.getId(),
                        firstInstructor.getName(),
                        ClassForm.GROUP,
                        yoga.getId(),
                        yoga.getName(),
                        "오후 요가",
                        "오후 요가 안내",
                        1,
                        1,
                        0,
                        0,
                        QUERY_DATE.atTime(14, 0),
                        QUERY_DATE.atTime(15, 0),
                        decision(StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.WAITLISTABLE)
                )
        );
        assertThat(responses)
                .extracting(StudentDailySessionView::id)
                .doesNotContain(sameTypeOtherForm.getId());
        assertThat(queryCount).isEqualTo(6L);
    }

    @Test
    void 보유한_모든_수강권의_수업_종류를_합쳐서_조회한다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("all-pass-owner"), "전체 수강권 조회 시설");
        정책을_저장한다(studio, 30);
        StudioMembership memberMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("all-pass-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership instructor = 소속을_저장한다(
                studio,
                회원을_저장한다("all-pass-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType yoga = 수업_종류를_저장한다(studio, "전체 수강권 요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "전체 수강권 필라테스");
        ClassType barre = 수업_종류를_저장한다(studio, "전체 수강권 바레");
        보유_수강권을_저장한다(
                memberMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(yoga)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );
        보유_수강권을_저장한다(
                memberMembership,
                수강권을_저장한다(studio, ClassForm.INDIVIDUAL, List.of(pilates)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );
        ClassSession yogaSession = 수업_회차를_저장한다(
                studio, instructor, yoga, "요가 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(11, 0)
        );
        ClassSession pilatesSession = 수업_회차를_저장한다(
                studio, instructor, pilates, "필라테스 수업", ClassForm.INDIVIDUAL, 5,
                QUERY_DATE.atTime(12, 0)
        );
        수업_회차를_저장한다(
                studio, instructor, barre, "바레 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(13, 0)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudentDailySessionView> responses = studentDailyQueryService.findAll(
                memberMembership.getMember().getId(),
                studio.getId(),
                QUERY_DATE
        );

        // then
        assertThat(responses)
                .extracting(StudentDailySessionView::id)
                .containsExactly(yogaSession.getId(), pilatesSession.getId());
    }

    @Test
    void 과거_날짜에는_보유_수강권의_수업_중_예약과_출석과_결석_내역만_반환한다() {
        // given
        LocalDate pastDate = QUERY_DATE.minusDays(1);
        Studio studio = 시설을_저장한다(회원을_저장한다("past-daily-owner"), "과거 일별 조회 시설");
        정책을_저장한다(studio, 30);
        StudioMembership memberMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("past-daily-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership otherMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("past-daily-other-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership instructor = 소속을_저장한다(
                studio,
                회원을_저장한다("past-daily-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType yoga = 수업_종류를_저장한다(studio, "과거 요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "과거 필라테스");
        보유_수강권을_저장한다(
                memberMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(yoga)),
                MemberPassProductStatus.EXHAUSTED,
                0,
                pastDate.minusMonths(1),
                pastDate
        );

        ClassSession attended = 수업_회차를_저장한다(
                studio, instructor, yoga, "출석한 수업", ClassForm.GROUP, 5,
                pastDate.atTime(10, 0)
        );
        ClassSession unreserved = 수업_회차를_저장한다(
                studio, instructor, yoga, "예약하지 않은 수업", ClassForm.GROUP, 5,
                pastDate.atTime(11, 0)
        );
        ClassSession reservedOnly = 수업_회차를_저장한다(
                studio, instructor, yoga, "출석 처리되지 않은 수업", ClassForm.GROUP, 5,
                pastDate.atTime(12, 0)
        );
        ClassSession absent = 수업_회차를_저장한다(
                studio, instructor, yoga, "결석한 수업", ClassForm.GROUP, 5,
                pastDate.atTime(12, 30)
        );
        ClassSession otherMemberAttended = 수업_회차를_저장한다(
                studio, instructor, yoga, "다른 회원이 출석한 수업", ClassForm.GROUP, 5,
                pastDate.atTime(13, 0)
        );
        ClassSession otherClassType = 수업_회차를_저장한다(
                studio, instructor, pilates, "다른 종류 출석 수업", ClassForm.GROUP, 5,
                pastDate.atTime(14, 0)
        );
        신청을_저장한다(attended, memberMembership, EnrollmentStatus.RESERVED, AttendanceResult.ATTENDED);
        신청을_저장한다(reservedOnly, memberMembership, EnrollmentStatus.RESERVED);
        신청을_저장한다(absent, memberMembership, EnrollmentStatus.RESERVED, AttendanceResult.ABSENT);
        신청을_저장한다(otherMemberAttended, otherMembership, EnrollmentStatus.RESERVED, AttendanceResult.ATTENDED);
        신청을_저장한다(otherClassType, memberMembership, EnrollmentStatus.RESERVED, AttendanceResult.ATTENDED);
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudentDailySessionView> responses = studentDailyQueryService.findAll(
                memberMembership.getMember().getId(),
                studio.getId(),
                pastDate
        );

        // then
        assertThat(responses)
                .extracting(StudentDailySessionView::id, StudentDailySessionView::bookingDecision)
                .containsExactly(
                        tuple(attended.getId(), decision(StudentBookingRelation.RESERVED, AttendanceResult.ATTENDED, null)),
                        tuple(reservedOnly.getId(), decision(StudentBookingRelation.RESERVED, AttendanceResult.NOT_RECORDED, null)),
                        tuple(absent.getId(), decision(StudentBookingRelation.RESERVED, AttendanceResult.ABSENT, null))
                );
        assertThat(responses)
                .extracting(StudentDailySessionView::id)
                .doesNotContain(
                        unreserved.getId(),
                        otherMemberAttended.getId(),
                        otherClassType.getId()
                );
    }

    @Test
    void 회원용_예약_상태는_수업과_본인_상태와_마감_우선순위로_결정한다() {
        // given
        Member owner = 회원을_저장한다("status-owner");
        Studio studio = 시설을_저장한다(owner, "상태 조회 시설");
        정책을_저장한다(studio, 30);
        StudioMembership memberMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("status-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership instructor = 소속을_저장한다(
                studio,
                회원을_저장한다("status-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "상태 요가");
        보유_수강권을_저장한다(
                memberMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(classType)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );

        ClassSession canceled = 수업_회차를_저장한다(
                studio, instructor, classType, "취소 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(7, 0)
        );
        canceled.cancel(canceled.getStartAt().minusMinutes(1));
        ClassSession attendancePending = 수업_회차를_저장한다(
                studio, instructor, classType, "출석 처리 대기 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(8, 30)
        );
        ClassSession attended = 수업_회차를_저장한다(
                studio, instructor, classType, "출석 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(8, 40)
        );
        ClassSession absent = 수업_회차를_저장한다(
                studio, instructor, classType, "결석 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(8, 50)
        );
        ClassSession startedWithoutEnrollment = 수업_회차를_저장한다(
                studio, instructor, classType, "시작한 미신청 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(9, 0)
        );
        ClassSession reserved = 수업_회차를_저장한다(
                studio, instructor, classType, "예약 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 10)
        );
        ClassSession offered = 수업_회차를_저장한다(
                studio, instructor, classType, "제안 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 20)
        );
        ClassSession waiting = 수업_회차를_저장한다(
                studio, instructor, classType, "대기 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 25)
        );
        ClassSession closedAtBoundary = 수업_회차를_저장한다(
                studio, instructor, classType, "마감 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 30)
        );
        ClassSession availableBeforeClose = 수업_회차를_저장한다(
                studio, instructor, classType, "예약 가능 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(11, 0)
        );
        신청을_저장한다(canceled, memberMembership, EnrollmentStatus.RESERVED);
        ClassSessionEnrollment attendancePendingEnrollment = 신청을_저장한다(
                attendancePending, memberMembership, EnrollmentStatus.RESERVED
        );
        ClassSessionEnrollment attendedEnrollment = 신청을_저장한다(
                attended, memberMembership, EnrollmentStatus.RESERVED, AttendanceResult.ATTENDED
        );
        ClassSessionEnrollment absentEnrollment = 신청을_저장한다(
                absent, memberMembership, EnrollmentStatus.RESERVED, AttendanceResult.ABSENT
        );
        ClassSessionEnrollment reservedEnrollment = 신청을_저장한다(
                reserved, memberMembership, EnrollmentStatus.RESERVED
        );
        ClassSessionEnrollment offeredEnrollment = 신청을_저장한다(
                offered, memberMembership, EnrollmentStatus.OFFERED
        );
        ClassSessionEnrollment waitingEnrollment = 신청을_저장한다(
                waiting, memberMembership, EnrollmentStatus.WAITING
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudentDailySessionView> responses = studentDailyQueryService.findAll(
                memberMembership.getMember().getId(),
                studio.getId(),
                QUERY_DATE
        );

        // then
        assertThat(responses)
                .extracting(
                        StudentDailySessionView::id,
                        StudentDailySessionView::enrollmentId,
                        StudentDailySessionView::bookingDecision
                )
                .containsExactly(
                        tuple(attendancePending.getId(), attendancePendingEnrollment.getId(), decision(StudentBookingRelation.RESERVED, AttendanceResult.NOT_RECORDED, null)),
                        tuple(attended.getId(), attendedEnrollment.getId(), decision(StudentBookingRelation.RESERVED, AttendanceResult.ATTENDED, null)),
                        tuple(absent.getId(), absentEnrollment.getId(), decision(StudentBookingRelation.RESERVED, AttendanceResult.ABSENT, null)),
                        tuple(startedWithoutEnrollment.getId(), null, decision(StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED)),
                        tuple(reserved.getId(), reservedEnrollment.getId(), decision(StudentBookingRelation.RESERVED, AttendanceResult.NOT_RECORDED, null)),
                        tuple(offered.getId(), offeredEnrollment.getId(), decision(StudentBookingRelation.OFFERED, AttendanceResult.NOT_RECORDED, null)),
                        tuple(waiting.getId(), waitingEnrollment.getId(), decision(StudentBookingRelation.WAITING, AttendanceResult.NOT_RECORDED, null)),
                        tuple(closedAtBoundary.getId(), null, decision(StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED)),
                        tuple(availableBeforeClose.getId(), null, decision(StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.RESERVABLE))
                );
        assertThat(responses)
                .extracting(StudentDailySessionView::id)
                .doesNotContain(canceled.getId());
    }

    @Test
    void 시설_소속이_아니면_회원용_일별_목록을_조회할_수_없다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("list-stranger-owner"), "목록 비소속 시설");
        Member stranger = 회원을_저장한다("list-stranger");

        // when / then
        assertStudioError(
                () -> studentDailyQueryService.findAll(stranger.getId(), studio.getId(), QUERY_DATE),
                StudioErrorCode.NOT_MEMBERSHIP
        );
    }

    @Test
    void 비활성_소속은_회원용_일별_목록을_조회할_수_없다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("list-inactive-owner"), "목록 비활성 시설");
        Member inactiveMember = 회원을_저장한다("list-inactive-member");
        소속을_저장한다(studio, inactiveMember, SystemRole.STUDENT, MembershipStatus.INACTIVE);

        // when / then
        assertStudioError(
                () -> studentDailyQueryService.findAll(
                        inactiveMember.getId(),
                        studio.getId(),
                        QUERY_DATE
                ),
                StudioErrorCode.MEMBERSHIP_INACTIVE
        );
    }

    @ParameterizedTest
    @EnumSource(value = SystemRole.class, names = {"OWNER", "INSTRUCTOR"})
    void 대표나_강사는_학생용_일별_목록을_조회할_수_없다(SystemRole systemRole) {
        // given
        Studio studio = 시설을_저장한다(
                회원을_저장한다("student-list-role-owner-" + systemRole),
                "학생용 역할 제한 시설 " + systemRole
        );
        Member staff = 회원을_저장한다("student-list-role-staff-" + systemRole);
        소속을_저장한다(studio, staff, systemRole, MembershipStatus.ACTIVE);

        // when / then
        assertStudioError(
                () -> studentDailyQueryService.findAll(
                        staff.getId(),
                        studio.getId(),
                        QUERY_DATE
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @Test
    void 커스텀_직원_역할은_학생용_일별_목록을_조회할_수_없다() {
        // given
        Studio studio = 시설을_저장한다(
                회원을_저장한다("student-list-custom-role-owner"),
                "학생용 커스텀 역할 제한 시설"
        );
        StudioRole staffRole = StudioRole.builder()
                .studio(studio)
                .name("운영 관리자")
                .instructor(true)
                .build();
        entityManager.persist(staffRole);

        Member staff = 회원을_저장한다("student-list-custom-role-staff");
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(staff)
                .phoneNumber(staff.getPhoneNumber())
                .name(staff.getName())
                .studioRole(staffRole)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
        entityManager.flush();

        // when / then
        assertStudioError(
                () -> studentDailyQueryService.findAll(
                        staff.getId(),
                        studio.getId(),
                        QUERY_DATE
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @Test
    void 다른_회원의_보유_수강권으로는_수업을_조회하지_않는다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("other-pass-owner"), "다른 회원 수강권 시설");
        정책을_저장한다(studio, 30);
        StudioMembership memberMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("other-pass-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership otherMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("other-pass-owner-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership instructor = 소속을_저장한다(
                studio,
                회원을_저장한다("other-pass-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "다른 회원 수강권 종류");
        보유_수강권을_저장한다(
                otherMembership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(classType)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );
        수업_회차를_저장한다(
                studio, instructor, classType, "다른 회원만 수강 가능한 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(12, 0)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudentDailySessionView> responses = studentDailyQueryService.findAll(
                memberMembership.getMember().getId(),
                studio.getId(),
                QUERY_DATE
        );

        // then
        assertThat(responses).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("현재_상태와_잔여_횟수")
    void 이용_기간_안이면_현재_상태와_잔여_횟수와_관계없이_목록을_조회한다(
            MemberPassProductStatus status,
            int remainingCount
    ) {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("unusable-pass-owner-" + status), "사용 불가 수강권 시설");
        Member member = 회원을_저장한다("unusable-pass-member-" + status);
        StudioMembership membership = 소속을_저장한다(
                studio,
                member,
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "사용 불가 수업 종류");
        StudioMembership instructor = 소속을_저장한다(
                studio,
                회원을_저장한다("unusable-pass-instructor-" + status),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        보유_수강권을_저장한다(
                membership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(classType)),
                status,
                remainingCount,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );
        정책을_저장한다(studio, 30);
        수업_회차를_저장한다(
                studio, instructor, classType, "사용 불가 수강권 종류 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(12, 0)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudentDailySessionView> responses = studentDailyQueryService.findAll(
                member.getId(),
                studio.getId(),
                QUERY_DATE
        );

        // then
        assertThat(responses)
                .extracting(StudentDailySessionView::className)
                .containsExactly("사용 불가 수강권 종류 수업");
    }

    @Test
    void 조회일이_보유_수강권_이용_기간_밖이면_빈_목록을_반환한다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("period-pass-owner"), "기간 외 수강권 시설");
        Member member = 회원을_저장한다("period-pass-member");
        StudioMembership membership = 소속을_저장한다(
                studio,
                member,
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "기간 외 수업 종류");
        StudioMembership instructor = 소속을_저장한다(
                studio,
                회원을_저장한다("period-pass-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        보유_수강권을_저장한다(
                membership,
                수강권을_저장한다(studio, ClassForm.GROUP, List.of(classType)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusMonths(1),
                QUERY_DATE.minusDays(1)
        );
        정책을_저장한다(studio, 30);
        수업_회차를_저장한다(
                studio, instructor, classType, "기간 밖 수강권 종류 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(12, 0)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudentDailySessionView> responses = studentDailyQueryService.findAll(
                member.getId(),
                studio.getId(),
                QUERY_DATE
        );

        // then
        assertThat(responses).isEmpty();
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
            SystemRole systemRole,
            MembershipStatus status
    ) {
        return 소속을_저장한다(studio, member, systemRole, status, member.getName());
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            SystemRole systemRole,
            MembershipStatus status,
            String name
    ) {
        StudioRole role = 역할을_조회하거나_저장한다(studio, systemRole);

        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .phoneNumber(member.getPhoneNumber())
                .name(name)
                .studioRole(role)
                .status(status)
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

    private ClassSession 수업_회차를_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                "편한 복장과 개인 수건을 준비해 주세요.",
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0)
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private ClassSession 수업_회차를_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name,
            ClassForm classForm,
            int capacity,
            LocalDateTime startAt
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                classForm,
                60,
                capacity,
                startAt
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private void 정책을_저장한다(Studio studio, int reservationCloseMinutesBefore) {
        entityManager.persist(StudioPolicy.builder()
                .studio(studio)
                .reservationCloseMinutesBefore(reservationCloseMinutesBefore)
                .freeCancelMinutesBefore(60)
                .waitingOfferResponseMinutes(10)
                .build());
        entityManager.flush();
    }

    private PassProduct 수강권을_저장한다(
            Studio studio,
            ClassForm classForm,
            List<ClassType> classTypes
    ) {
        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name(classForm + " 회원용 수강권")
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
            MemberPassProductStatus status,
            int remainingCount,
            LocalDate startedAt,
            LocalDate expiresAt
    ) {
        MemberPassProduct memberPassProduct = MemberPassProduct.builder()
                .membership(membership)
                .passProduct(passProduct)
                .remainingCount(remainingCount)
                .remainingHoldDays(7)
                .status(status)
                .startedAt(startedAt)
                .expiresAt(expiresAt)
                .build();
        entityManager.persist(memberPassProduct);
        entityManager.flush();
        return memberPassProduct;
    }

    private ClassSessionEnrollment 신청을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            EnrollmentStatus status
    ) {
        return 신청을_저장한다(classSession, membership, status, AttendanceResult.NOT_RECORDED);
    }

    private ClassSessionEnrollment 신청을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            EnrollmentStatus status,
            AttendanceResult attendanceResult
    ) {
        ClassSessionEnrollment enrollment = switch (status) {
            case RESERVED -> ClassSessionEnrollment.reserved(
                    membership,
                    classSession,
                    수강권을_조회하거나_저장한다(membership, classSession),
                    NOW.minusDays(1)
            );
            case WAITING -> ClassSessionEnrollment.waiting(membership, classSession, NOW.minusDays(1));
            case OFFERED -> {
                ClassSessionEnrollment offered = ClassSessionEnrollment.waiting(
                        membership,
                        classSession,
                        NOW.minusDays(1)
                );
                offered.offer(NOW.minusMinutes(5), NOW.plusMinutes(5));
                yield offered;
            }
            case CANCELED -> {
                ClassSessionEnrollment canceled = ClassSessionEnrollment.waiting(
                        membership,
                        classSession,
                        NOW.minusDays(1)
                );
                canceled.cancelWaiting(NOW.minusHours(1));
                yield canceled;
            }
            case EXPIRED -> {
                ClassSessionEnrollment expired = ClassSessionEnrollment.waiting(
                        membership,
                        classSession,
                        NOW.minusDays(1)
                );
                expired.expire(NOW.minusHours(1));
                yield expired;
            }
        };
        if (attendanceResult == AttendanceResult.ATTENDED) {
            enrollment.markAttended(classSession.getEndAt());
        } else if (attendanceResult == AttendanceResult.ABSENT) {
            enrollment.markAbsent(classSession.getEndAt());
        }
        entityManager.persist(enrollment);
        return enrollment;
    }

    private MemberPassProduct 수강권을_조회하거나_저장한다(
            StudioMembership membership,
            ClassSession classSession
    ) {
        List<MemberPassProduct> ownedPasses = entityManager.createQuery("""
                        SELECT memberPassProduct
                        FROM MemberPassProduct memberPassProduct
                        WHERE memberPassProduct.membership.id = :membershipId
                        ORDER BY memberPassProduct.id
                        """, MemberPassProduct.class)
                .setParameter("membershipId", membership.getId())
                .setMaxResults(1)
                .getResultList();
        if (!ownedPasses.isEmpty()) {
            return ownedPasses.getFirst();
        }

        Long classTypeId = classSessionClassTypeRepository.findByClassSessionId(classSession.getId())
                .orElseThrow()
                .getClassTypeId();
        PassProduct passProduct = 수강권을_저장한다(
                membership.getStudio(),
                classSession.getClassForm(),
                List.of(classTypeRepository.getReferenceById(classTypeId))
        );
        return 보유_수강권을_저장한다(
                membership,
                passProduct,
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusYears(1),
                QUERY_DATE.plusYears(1)
        );
    }

    private static StudentBookingDecision decision(
            StudentBookingRelation bookingRelation,
            AttendanceResult attendanceResult,
            BookingAvailability availability
    ) {
        return new StudentBookingDecision(bookingRelation, attendanceResult, Optional.ofNullable(availability));
    }

    private void assertStudioError(Runnable action, StudioErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private void assertClassError(Runnable action, ClassErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private static Stream<Arguments> 현재_상태와_잔여_횟수() {
        return Stream.of(
                Arguments.of(MemberPassProductStatus.HOLD, 10),
                Arguments.of(MemberPassProductStatus.ACTIVE, 0),
                Arguments.of(MemberPassProductStatus.EXHAUSTED, 0)
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
