package com.classitda.classes.application.instructor.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.InstructorSessionStatusResolver;
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
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Permission;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.StudioRolePermission;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.PermissionRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
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
        InstructorSessionQueryService.class,
        InstructorSessionAccessReader.class,
        InstructorSessionScheduleReader.class,
        InstructorSessionAssembler.class,
        InstructorSessionStatusResolver.class,
        InstructorSessionQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class InstructorSessionQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDate QUERY_DATE = LocalDate.of(2026, 8, 17);

    private final InstructorSessionQueryService queryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final PermissionRepository permissionRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    InstructorSessionQueryServiceTest(
            InstructorSessionQueryService queryService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            PermissionRepository permissionRepository,
            StudioRolePermissionRepository studioRolePermissionRepository,
            EntityManager entityManager,
            EntityManagerFactory entityManagerFactory
    ) {
        this.queryService = queryService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.permissionRepository = permissionRepository;
        this.studioRolePermissionRepository = studioRolePermissionRepository;
        this.entityManager = entityManager;
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void 대표는_시설의_일별_수업을_정렬하고_예약과_대기_인원을_함께_조회한다() {
        // given
        Member owner = 회원을_저장한다("instructor-daily-owner");
        Studio studio = 시설을_저장한다(owner, "강사용 일별 조회 시설");
        StudioMembership ownerMembership = 소속을_저장한다(
                studio,
                owner,
                SystemRole.OWNER,
                MembershipStatus.ACTIVE
        );
        StudioMembership firstInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("instructor-daily-first"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        StudioMembership secondInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("instructor-daily-second"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "기구 필라테스");
        정책을_저장한다(studio, 30);

        ClassSession firstAtSameTime = 수업을_저장한다(
                studio,
                ownerMembership,
                classType,
                "대표 수업",
                QUERY_DATE.atTime(12, 0),
                ClassSessionStatus.OPENED
        );
        ClassSession earlySession = 수업을_저장한다(
                studio,
                firstInstructor,
                classType,
                "오전 필라테스",
                QUERY_DATE.atTime(11, 0),
                ClassSessionStatus.OPENED
        );
        ClassSession secondAtSameTime = 수업을_저장한다(
                studio,
                secondInstructor,
                classType,
                "정오 필라테스",
                QUERY_DATE.atTime(12, 0),
                ClassSessionStatus.OPENED
        );
        수업을_저장한다(
                studio,
                firstInstructor,
                classType,
                "다음 날 수업",
                QUERY_DATE.plusDays(1).atStartOfDay(),
                ClassSessionStatus.OPENED
        );

        StudioMembership firstStudent = 소속을_저장한다(
                studio,
                회원을_저장한다("instructor-count-first-student"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership secondStudent = 소속을_저장한다(
                studio,
                회원을_저장한다("instructor-count-second-student"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership thirdStudent = 소속을_저장한다(
                studio,
                회원을_저장한다("instructor-count-third-student"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        예약을_저장한다(earlySession, firstStudent, ReservationStatus.RESERVED);
        예약을_저장한다(earlySession, secondStudent, ReservationStatus.ATTENDED);
        예약을_저장한다(earlySession, thirdStudent, ReservationStatus.CANCELED);
        대기를_저장한다(earlySession, firstStudent, 1, WaitingStatus.WAITING);
        대기를_저장한다(earlySession, secondStudent, 2, WaitingStatus.OFFERED);
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        List<InstructorSessionView> responses = queryService.findAll(
                owner.getId(),
                studio.getId(),
                QUERY_DATE,
                false
        );
        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(responses)
                .extracting(InstructorSessionView::id)
                .containsExactly(
                        earlySession.getId(),
                        firstAtSameTime.getId(),
                        secondAtSameTime.getId()
                );
        assertThat(responses.getFirst()).isEqualTo(new InstructorSessionView(
                earlySession.getId(),
                firstInstructor.getId(),
                firstInstructor.getMember().getName(),
                ClassForm.GROUP,
                classType.getId(),
                classType.getName(),
                "오전 필라테스",
                "오전 필라테스 안내",
                12,
                2,
                1,
                QUERY_DATE.atTime(11, 0),
                QUERY_DATE.atTime(12, 0),
                InstructorSessionStatus.SCHEDULED_OPEN
        ));
        assertThat(queryCount).isEqualTo(7L);
    }

    @Test
    void 일반_강사는_전체_조회를_요청해도_자신의_담당_수업만_조회한다() {
        // given
        Member owner = 회원을_저장한다("own-schedule-owner");
        Studio studio = 시설을_저장한다(owner, "일반 강사 일정 시설");
        StudioRole instructorRole = 역할을_저장한다(studio, SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member instructor = 회원을_저장한다("own-schedule-instructor");
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                instructor,
                instructorRole,
                MembershipStatus.ACTIVE
        );
        StudioMembership otherInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("own-schedule-other"),
                instructorRole,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "요가");
        정책을_저장한다(studio, 30);
        ClassSession ownSession = 수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "내 수업",
                QUERY_DATE.atTime(13, 0),
                ClassSessionStatus.OPENED
        );
        수업을_저장한다(
                studio,
                otherInstructor,
                classType,
                "다른 강사 수업",
                QUERY_DATE.atTime(14, 0),
                ClassSessionStatus.OPENED
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorSessionView> responses = queryService.findAll(
                instructor.getId(),
                studio.getId(),
                QUERY_DATE,
                false
        );

        // then
        assertThat(responses)
                .extracting(InstructorSessionView::id)
                .containsExactly(ownSession.getId());
    }

    @Test
    void 전체_관리_권한이_있는_직원은_강사_역할이_아니어도_시설_전체_수업을_조회한다() {
        // given
        Member owner = 회원을_저장한다("all-schedule-owner");
        Studio studio = 시설을_저장한다(owner, "전체 관리 일정 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(studio, "수업 관리자", false);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("all-schedule-manager");
        소속을_저장한다(studio, manager, managerRole, MembershipStatus.ACTIVE);
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("all-schedule-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "발레");
        정책을_저장한다(studio, 30);
        ClassSession classSession = 수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "전체 관리 대상 수업",
                QUERY_DATE.atTime(15, 0),
                ClassSessionStatus.OPENED
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorSessionView> responses = queryService.findAll(
                manager.getId(),
                studio.getId(),
                QUERY_DATE,
                false
        );

        // then
        assertThat(responses)
                .extracting(InstructorSessionView::id)
                .containsExactly(classSession.getId());
    }

    @Test
    void 전체_관리_권한자가_내_수업만_요청하면_자신의_담당_수업만_조회한다() {
        // given
        Member owner = 회원을_저장한다("all-mine-owner");
        Studio studio = 시설을_저장한다(owner, "전체 권한 내 일정 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(studio, "수업 총괄 강사", true);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("all-mine-manager");
        StudioMembership managerMembership = 소속을_저장한다(
                studio,
                manager,
                managerRole,
                MembershipStatus.ACTIVE
        );
        StudioMembership otherInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("all-mine-other"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "스피닝");
        정책을_저장한다(studio, 30);
        ClassSession ownSession = 수업을_저장한다(
                studio,
                managerMembership,
                classType,
                "총괄 강사 수업",
                QUERY_DATE.atTime(16, 0),
                ClassSessionStatus.OPENED
        );
        수업을_저장한다(
                studio,
                otherInstructor,
                classType,
                "다른 강사 수업",
                QUERY_DATE.atTime(17, 0),
                ClassSessionStatus.OPENED
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorSessionView> responses = queryService.findAll(
                manager.getId(),
                studio.getId(),
                QUERY_DATE,
                true
        );

        // then
        assertThat(responses)
                .extracting(InstructorSessionView::id)
                .containsExactly(ownSession.getId());
    }

    @Test
    void 학생은_강사용_일정을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("student-schedule-owner");
        Studio studio = 시설을_저장한다(owner, "학생 접근 제한 시설");
        Member student = 회원을_저장한다("student-schedule-member");
        소속을_저장한다(studio, student, SystemRole.STUDENT, MembershipStatus.ACTIVE);

        // when / then
        assertStudioError(
                () -> queryService.findAll(student.getId(), studio.getId(), QUERY_DATE, false),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @Test
    void 수업_관리_권한이_없는_직원은_강사용_일정을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("no-permission-owner");
        Studio studio = 시설을_저장한다(owner, "권한 없는 직원 시설");
        StudioRole staffRole = 사용자_역할을_저장한다(studio, "프런트 직원", false);
        Member staff = 회원을_저장한다("no-permission-staff");
        소속을_저장한다(studio, staff, staffRole, MembershipStatus.ACTIVE);

        // when / then
        assertStudioError(
                () -> queryService.findAll(staff.getId(), studio.getId(), QUERY_DATE, false),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @Test
    void 비활성_소속은_강사용_일정을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("inactive-schedule-owner");
        Studio studio = 시설을_저장한다(owner, "비활성 강사 시설");
        Member instructor = 회원을_저장한다("inactive-schedule-instructor");
        소속을_저장한다(
                studio,
                instructor,
                SystemRole.INSTRUCTOR,
                MembershipStatus.INACTIVE
        );

        // when / then
        assertStudioError(
                () -> queryService.findAll(instructor.getId(), studio.getId(), QUERY_DATE, false),
                StudioErrorCode.MEMBERSHIP_INACTIVE
        );
    }

    @Test
    void 일정이_없는_날은_시설_정책이_없어도_빈_목록을_반환한다() {
        // given
        Member owner = 회원을_저장한다("empty-schedule-owner");
        Studio studio = 시설을_저장한다(owner, "빈 일정 시설");
        소속을_저장한다(studio, owner, SystemRole.OWNER, MembershipStatus.ACTIVE);
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorSessionView> responses = queryService.findAll(
                owner.getId(),
                studio.getId(),
                QUERY_DATE,
                false
        );

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 조회일이_없거나_다음_날을_계산할_수_없으면_잘못된_입력으로_처리한다() {
        // given
        Member owner = 회원을_저장한다("invalid-date-owner");
        Studio studio = 시설을_저장한다(owner, "잘못된 날짜 시설");
        소속을_저장한다(studio, owner, SystemRole.OWNER, MembershipStatus.ACTIVE);
        entityManager.flush();
        entityManager.clear();

        // when / then
        assertCommonError(
                () -> queryService.findAll(owner.getId(), studio.getId(), null, false),
                CommonErrorCode.INVALID_INPUT
        );
        assertCommonError(
                () -> queryService.findAll(owner.getId(), studio.getId(), LocalDate.MAX, false),
                CommonErrorCode.INVALID_INPUT
        );
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
            SystemRole systemRole,
            MembershipStatus status
    ) {
        StudioRole role = 역할을_조회하거나_저장한다(studio, systemRole);
        return 소속을_저장한다(studio, member, role, status);
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            StudioRole role,
            MembershipStatus status
    ) {
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .name(member.getName())
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

        return 역할을_저장한다(studio, systemRole);
    }

    private StudioRole 역할을_저장한다(Studio studio, SystemRole systemRole) {
        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private StudioRole 사용자_역할을_저장한다(
            Studio studio,
            String name,
            boolean instructor
    ) {
        StudioRole role = StudioRole.builder()
                .studio(studio)
                .name(name)
                .instructor(instructor)
                .build();
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private void 권한을_저장한다(StudioRole role, PermissionCode code) {
        Permission permission = permissionRepository.findByCodeIn(List.of(code)).getFirst();
        studioRolePermissionRepository.saveAndFlush(StudioRolePermission.builder()
                .studioRole(role)
                .permission(permission)
                .build());
    }

    private ClassType 수업_종류를_저장한다(Studio studio, String name) {
        return classTypeRepository.saveAndFlush(ClassTypeFixture.이름이_다른_수업_종류(studio, name));
    }

    private ClassSession 수업을_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name,
            LocalDateTime startAt,
            ClassSessionStatus status
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                ClassForm.GROUP,
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

    private void 정책을_저장한다(Studio studio, int reservationCloseMinutesBefore) {
        entityManager.persist(StudioPolicy.builder()
                .studio(studio)
                .reservationCloseMinutesBefore(reservationCloseMinutesBefore)
                .freeCancelMinutesBefore(60)
                .waitingOfferResponseMinutes(10)
                .build());
        entityManager.flush();
    }

    private void 예약을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            ReservationStatus status
    ) {
        entityManager.persist(Reservation.builder()
                .membership(membership)
                .classSession(classSession)
                .status(status)
                .reservedAt(NOW.minusDays(1))
                .canceledAt(status == ReservationStatus.CANCELED ? NOW.minusHours(1) : null)
                .build());
    }

    private void 대기를_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            int sequence,
            WaitingStatus status
    ) {
        entityManager.persist(Waiting.builder()
                .membership(membership)
                .classSession(classSession)
                .sequence(sequence)
                .status(status)
                .offeredAt(status == WaitingStatus.OFFERED ? NOW.minusMinutes(5) : null)
                .build());
    }

    private void assertStudioError(Runnable action, StudioErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private void assertCommonError(Runnable action, CommonErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassitdaException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
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
