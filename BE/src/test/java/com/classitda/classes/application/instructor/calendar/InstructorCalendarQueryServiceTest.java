package com.classitda.classes.application.instructor.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
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
        InstructorCalendarQueryService.class,
        InstructorSessionAccessReader.class,
        InstructorCalendarQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class InstructorCalendarQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDate RANGE_FROM = LocalDate.of(2026, 8, 15);
    private static final LocalDate RANGE_TO = LocalDate.of(2026, 8, 19);

    private final InstructorCalendarQueryService queryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final PermissionRepository permissionRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    InstructorCalendarQueryServiceTest(
            InstructorCalendarQueryService queryService,
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
    void 대표는_예정과_완료_수업_존재_여부를_날짜별로_조회하고_진행과_취소만_있는_날짜는_생략한다() {
        // given
        Member owner = 회원을_저장한다("calendar-summary-owner");
        Studio studio = 시설을_저장한다(owner, "달력 요약 시설");
        StudioMembership ownerMembership = 소속을_저장한다(
                studio,
                owner,
                SystemRole.OWNER,
                MembershipStatus.ACTIVE
        );
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-summary-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "달력 필라테스");
        ClassSession completedSession = 수업을_저장한다(
                studio,
                ownerMembership,
                classType,
                "완료 수업",
                LocalDate.of(2026, 8, 16).atTime(9, 0)
        );
        수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "진행 수업",
                LocalDate.of(2026, 8, 17).atTime(9, 30)
        );
        ClassSession canceledSession = 수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "취소 수업",
                LocalDate.of(2026, 8, 17).atTime(20, 0)
        );
        canceledSession.cancel(canceledSession.getStartAt().minusMinutes(1));
        수업을_저장한다(
                studio,
                ownerMembership,
                classType,
                "예약 가능 예정 수업",
                LocalDate.of(2026, 8, 18).atTime(11, 0)
        );
        수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "예약 마감 예정 수업",
                LocalDate.of(2026, 8, 18).atTime(12, 0)
        );
        수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "기간 밖 수업",
                LocalDate.of(2026, 8, 20).atTime(10, 0)
        );

        StudioMembership studentMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-summary-student"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        예약을_저장한다(completedSession, studentMembership);
        대기를_저장한다(completedSession, studentMembership);
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        List<InstructorCalendarSummary> summaries = queryService.findAll(
                owner.getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        );
        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(summaries).containsExactly(
                new InstructorCalendarSummary(
                        LocalDate.of(2026, 8, 16),
                        false,
                        true,
                        false,
                        true
                ),
                new InstructorCalendarSummary(
                        LocalDate.of(2026, 8, 18),
                        true,
                        false,
                        true,
                        false
                )
        );
        assertThat(queryCount).isEqualTo(4L);
    }

    @Test
    void 일반_강사는_시설_전체와_본인_수업_존재_여부를_함께_조회한다() {
        // given
        Member owner = 회원을_저장한다("calendar-own-owner");
        Studio studio = 시설을_저장한다(owner, "강사 본인 달력 시설");
        StudioRole instructorRole = 역할을_저장한다(studio, SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member instructor = 회원을_저장한다("calendar-own-instructor");
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                instructor,
                instructorRole,
                MembershipStatus.ACTIVE
        );
        StudioMembership otherInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-own-other"),
                instructorRole,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "강사 달력 요가");
        수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "내 달력 수업",
                LocalDate.of(2026, 8, 18).atTime(11, 0)
        );
        수업을_저장한다(
                studio,
                otherInstructor,
                classType,
                "다른 강사 달력 수업",
                LocalDate.of(2026, 8, 18).atTime(12, 0)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorCalendarSummary> summaries = queryService.findAll(
                instructor.getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        );

        // then
        assertThat(summaries).containsExactly(new InstructorCalendarSummary(
                LocalDate.of(2026, 8, 18),
                true,
                false,
                true,
                false
        ));
    }

    @Test
    void 전체_관리_권한자는_시설_전체_수업_존재_여부를_조회한다() {
        // given
        Member owner = 회원을_저장한다("calendar-all-owner");
        Studio studio = 시설을_저장한다(owner, "전체 관리 달력 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(studio, "달력 관리자", false);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("calendar-all-manager");
        소속을_저장한다(studio, manager, managerRole, MembershipStatus.ACTIVE);
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-all-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "전체 관리 발레");
        수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "전체 관리 대상 수업",
                LocalDate.of(2026, 8, 18).atTime(13, 0)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorCalendarSummary> summaries = queryService.findAll(
                manager.getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        );

        // then
        assertThat(summaries).containsExactly(new InstructorCalendarSummary(
                LocalDate.of(2026, 8, 18),
                true,
                false,
                false,
                false
        ));
    }

    @Test
    void 전체_관리_권한자도_시설_전체와_본인_수업_존재_여부를_함께_조회한다() {
        // given
        Member owner = 회원을_저장한다("calendar-all-mine-owner");
        Studio studio = 시설을_저장한다(owner, "전체 권한 본인 달력 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(studio, "달력 총괄 강사", true);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("calendar-all-mine-manager");
        StudioMembership managerMembership = 소속을_저장한다(
                studio,
                manager,
                managerRole,
                MembershipStatus.ACTIVE
        );
        StudioMembership otherInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("calendar-all-mine-other"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "전체 권한 스피닝");
        수업을_저장한다(
                studio,
                managerMembership,
                classType,
                "내 전체 권한 수업",
                LocalDate.of(2026, 8, 18).atTime(14, 0)
        );
        수업을_저장한다(
                studio,
                otherInstructor,
                classType,
                "다른 강사 전체 권한 수업",
                LocalDate.of(2026, 8, 18).atTime(15, 0)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorCalendarSummary> summaries = queryService.findAll(
                manager.getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        );

        // then
        assertThat(summaries).containsExactly(new InstructorCalendarSummary(
                LocalDate.of(2026, 8, 18),
                true,
                false,
                true,
                false
        ));
    }

    @Test
    void 학생은_강사용_달력을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("calendar-student-owner");
        Studio studio = 시설을_저장한다(owner, "학생 달력 차단 시설");
        Member student = 회원을_저장한다("calendar-student-member");
        소속을_저장한다(studio, student, SystemRole.STUDENT, MembershipStatus.ACTIVE);
        entityManager.flush();
        entityManager.clear();

        // when / then
        assertThatThrownBy(() -> queryService.findAll(
                student.getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        )).isInstanceOfSatisfying(StudioException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
    }

    @Test
    void 수업이_없는_기간은_시설_정책이_없어도_빈_목록을_반환한다() {
        // given
        Member owner = 회원을_저장한다("calendar-empty-owner");
        Studio studio = 시설을_저장한다(owner, "빈 달력 시설");
        소속을_저장한다(studio, owner, SystemRole.OWNER, MembershipStatus.ACTIVE);
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorCalendarSummary> summaries = queryService.findAll(
                owner.getId(),
                studio.getId(),
                RANGE_FROM,
                RANGE_TO
        );

        // then
        assertThat(summaries).isEmpty();
    }

    @Test
    void 양_끝을_포함한_42일_범위를_조회할_수_있다() {
        // given
        Member owner = 회원을_저장한다("calendar-max-range-owner");
        Studio studio = 시설을_저장한다(owner, "최대 달력 기간 시설");
        소속을_저장한다(studio, owner, SystemRole.OWNER, MembershipStatus.ACTIVE);
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = from.plusDays(41);
        entityManager.flush();
        entityManager.clear();

        // when
        List<InstructorCalendarSummary> summaries = queryService.findAll(
                owner.getId(),
                studio.getId(),
                from,
                to
        );

        // then
        assertThat(summaries).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("잘못된_조회_기간")
    void 필수값이_없거나_역전되거나_42일을_초과한_기간은_조회할_수_없다(
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
            LocalDateTime startAt
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                ClassForm.GROUP,
                60,
                12,
                startAt
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private void 예약을_저장한다(
            ClassSession classSession,
            StudioMembership membership
    ) {
        entityManager.persist(Reservation.builder()
                .membership(membership)
                .classSession(classSession)
                .status(ReservationStatus.RESERVED)
                .reservedAt(NOW.minusDays(1))
                .build());
    }

    private void 대기를_저장한다(
            ClassSession classSession,
            StudioMembership membership
    ) {
        entityManager.persist(Waiting.builder()
                .membership(membership)
                .classSession(classSession)
                .sequence(1)
                .status(WaitingStatus.WAITING)
                .build());
    }

    private static Stream<Arguments> 잘못된_조회_기간() {
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
