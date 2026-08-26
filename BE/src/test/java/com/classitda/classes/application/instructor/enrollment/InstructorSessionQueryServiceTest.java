package com.classitda.classes.application.instructor.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        InstructorSessionQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class InstructorSessionQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDateTime SESSION_START = LocalDateTime.of(2026, 8, 17, 12, 0);

    private long phoneSequence = 10_000_000L;

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
    void 대표는_수업_상세와_예약_회원을_예약_시각과_신청_ID_순서로_조회한다() {
        // given
        DetailContext context = 기본_환경("owner-detail");
        StudioRole studentRole = 역할을_저장한다(context.studio(), SystemRole.STUDENT);
        StudioMembership sameTimeFirst = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("김민지", "https://images.example.com/minji.png"),
                studentRole,
                "김민지"
        );
        StudioMembership sameTimeSecond = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("이서윤", null),
                studentRole,
                "이서윤"
        );
        StudioMembership earlier = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("박지수", "https://images.example.com/jisu.png"),
                studentRole,
                "박지수"
        );
        ClassSessionEnrollment sameTimeFirstEnrollment = 예약을_저장한다(
                context.classSession(), sameTimeFirst, NOW.minusDays(1));
        ClassSessionEnrollment sameTimeSecondEnrollment = 예약을_저장한다(
                context.classSession(), sameTimeSecond, NOW.minusDays(1));
        ClassSessionEnrollment earlierEnrollment = 예약을_저장한다(
                context.classSession(), earlier, NOW.minusDays(2));

        StudioMembership canceledMember = 학생_소속을_저장한다(context.studio(), studentRole, "취소 회원");
        ClassSessionEnrollment canceled = 예약을_저장한다(
                context.classSession(), canceledMember, NOW.minusDays(3));
        canceled.cancelReservation(NOW.minusHours(1));
        entityManager.persist(ClassSessionEnrollment.waiting(
                학생_소속을_저장한다(context.studio(), studentRole, "대기 회원"),
                context.classSession(),
                NOW.minusDays(1)
        ));
        ClassSessionEnrollment offered = ClassSessionEnrollment.waiting(
                학생_소속을_저장한다(context.studio(), studentRole, "제안 회원"),
                context.classSession(),
                NOW.minusDays(1)
        );
        offered.offer(NOW.minusMinutes(5), NOW.plusMinutes(5));
        entityManager.persist(offered);
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        InstructorSessionDetailView result = queryService.findDetail(
                context.owner().getId(),
                context.studio().getId(),
                context.classSession().getId()
        );
        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(result).isEqualTo(new InstructorSessionDetailView(
                context.classSession().getId(),
                context.instructorMembership().getId(),
                "이지은 강사",
                ClassForm.GROUP,
                context.classType().getId(),
                "리포머",
                "리포머 밸런스",
                "체어룸에서 진행",
                8,
                3,
                SESSION_START,
                SESSION_START.plusMinutes(60),
                InstructorSessionStatus.SCHEDULED_BOOKING_OPEN,
                true,
                List.of(
                        new InstructorSessionDetailView.ReservedMember(
                                earlierEnrollment.getId(),
                                earlier.getId(),
                                "박지수",
                                "https://images.example.com/jisu.png"
                        ),
                        new InstructorSessionDetailView.ReservedMember(
                                sameTimeFirstEnrollment.getId(),
                                sameTimeFirst.getId(),
                                "김민지",
                                "https://images.example.com/minji.png"
                        ),
                        new InstructorSessionDetailView.ReservedMember(
                                sameTimeSecondEnrollment.getId(),
                                sameTimeSecond.getId(),
                                "이서윤",
                                null
                        )
                )
        ));
        assertThat(queryCount).isEqualTo(6L);
    }

    @Test
    void 예약_회원이_없으면_예약_인원은_0이고_빈_목록을_반환한다() {
        // given
        DetailContext context = 기본_환경("empty-detail");
        entityManager.clear();

        // when
        InstructorSessionDetailView result = queryService.findDetail(
                context.owner().getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result.reservedCount()).isZero();
        assertThat(result.reservedMembers()).isEmpty();
    }

    @Test
    void 본인_수업_관리_권한이_있는_강사는_본인_수업을_조회한다() {
        // given
        DetailContext context = 기본_환경("own-detail");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.RESERVATION_READ);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member instructor = 회원을_저장한다("본인 수업 강사", null);
        StudioMembership instructorMembership = 소속을_저장한다(
                context.studio(), instructor, instructorRole, "본인 수업 강사");
        ClassSession ownSession = 수업을_저장한다(
                context.studio(), instructorMembership, context.classType(), "본인 수업");
        entityManager.clear();

        // when
        InstructorSessionDetailView result = queryService.findDetail(
                instructor.getId(),
                context.studio().getId(),
                ownSession.getId()
        );

        // then
        assertThat(result.mine()).isTrue();
    }

    @Test
    void 예약_조회_권한이_있는_강사는_다른_강사의_수업도_조회한다() {
        // given
        DetailContext context = 기본_환경("other-detail");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.RESERVATION_READ);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member instructor = 회원을_저장한다("타 수업 접근 강사", null);
        소속을_저장한다(context.studio(), instructor, instructorRole, "타 수업 접근 강사");
        entityManager.clear();

        // when
        InstructorSessionDetailView result = queryService.findDetail(
                instructor.getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result.mine()).isFalse();
    }

    @Test
    void 전체_수업_관리_권한이_있는_직원은_다른_강사의_수업을_조회한다() {
        // given
        DetailContext context = 기본_환경("all-detail");
        StudioRole managerRole = 사용자_역할을_저장한다(context.studio(), "수업 관리자");
        권한을_저장한다(managerRole, PermissionCode.RESERVATION_READ);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("수업 관리자", null);
        소속을_저장한다(context.studio(), manager, managerRole, "수업 관리자");
        entityManager.clear();

        // when
        InstructorSessionDetailView result = queryService.findDetail(
                manager.getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result.mine()).isFalse();
    }

    @Test
    void 예약_조회_권한이_없으면_수업_상세를_조회할_수_없다() {
        // given
        DetailContext context = 기본_환경("no-read-detail");
        StudioRole managerRole = 사용자_역할을_저장한다(context.studio(), "조회 권한 없는 관리자");
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("조회 권한 없는 관리자", null);
        소속을_저장한다(context.studio(), manager, managerRole, "조회 권한 없는 관리자");
        entityManager.clear();

        // when / then
        assertStudioError(
                () -> queryService.findDetail(
                        manager.getId(),
                        context.studio().getId(),
                        context.classSession().getId()
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @Test
    void 학생은_강사용_수업_상세를_조회할_수_없다() {
        // given
        DetailContext context = 기본_환경("student-detail");
        Member student = 회원을_저장한다("학생 접근자", null);
        소속을_저장한다(
                context.studio(),
                student,
                역할을_저장한다(context.studio(), SystemRole.STUDENT),
                "학생 접근자"
        );
        entityManager.clear();

        // when / then
        assertStudioError(
                () -> queryService.findDetail(
                        student.getId(),
                        context.studio().getId(),
                        context.classSession().getId()
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @Test
    void 다른_시설의_수업은_찾을_수_없다() {
        // given
        DetailContext context = 기본_환경("cross-detail");
        DetailContext other = 기본_환경("cross-detail-other");
        entityManager.clear();

        // when / then
        assertClassError(
                () -> queryService.findDetail(
                        context.owner().getId(),
                        context.studio().getId(),
                        other.classSession().getId()
                ),
                ClassErrorCode.CLASS_SESSION_NOT_FOUND
        );
    }

    @Test
    void 대표는_시설의_활성_학생과_예약_확정_여부를_ID_순서로_조회한다() {
        // given
        DetailContext context = 기본_환경("owner-candidates");
        StudioRole studentRole = 역할을_저장한다(context.studio(), SystemRole.STUDENT);
        StudioMembership reservedStudent = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("김민지", "https://images.example.com/minji.png"),
                studentRole,
                MembershipStatus.ACTIVE,
                "김민지"
        );
        StudioMembership activeStudent = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("최유진", null),
                studentRole,
                MembershipStatus.ACTIVE,
                "최유진"
        );
        StudioMembership waitingStudent = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("정하늘", null),
                studentRole,
                MembershipStatus.ACTIVE,
                "정하늘"
        );
        소속을_저장한다(
                context.studio(),
                회원을_저장한다("정지 회원", null),
                studentRole,
                MembershipStatus.INACTIVE,
                "정지 회원"
        );
        예약을_저장한다(
                context.classSession(),
                reservedStudent,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );
        ClassSessionEnrollment canceledEnrollment = 예약을_저장한다(
                context.classSession(),
                activeStudent,
                LocalDateTime.of(2026, 8, 1, 11, 0)
        );
        canceledEnrollment.cancelReservation(LocalDateTime.of(2026, 8, 1, 12, 0));
        entityManager.persist(ClassSessionEnrollment.waiting(
                waitingStudent,
                context.classSession(),
                LocalDateTime.of(2026, 8, 1, 13, 0)
        ));

        DetailContext otherContext = 기본_환경("other-studio-candidates");
        StudioRole otherStudentRole = 역할을_저장한다(otherContext.studio(), SystemRole.STUDENT);
        소속을_저장한다(
                otherContext.studio(),
                회원을_저장한다("다른 시설 회원", null),
                otherStudentRole,
                MembershipStatus.ACTIVE,
                "다른 시설 회원"
        );
        entityManager.clear();

        // when
        List<StudioStudentView> result = queryService.findAllStudioStudents(
                context.owner().getId(),
                context.studio().getId(),
                context.classSession().getId()
        );

        // then
        assertThat(result).containsExactly(
                new StudioStudentView(
                        reservedStudent.getId(),
                        "김민지",
                        "https://images.example.com/minji.png",
                        true
                ),
                new StudioStudentView(activeStudent.getId(), "최유진", null, false),
                new StudioStudentView(waitingStudent.getId(), "정하늘", null, false)
        );
    }

    @Test
    void 본인_수업만_관리하는_강사가_다른_강사의_수업_후보를_조회하면_찾을_수_없다() {
        // given
        DetailContext context = 기본_환경("other-session-candidates");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.RESERVATION_MANAGE);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member instructor = 회원을_저장한다("일반 강사", null);
        소속을_저장한다(context.studio(), instructor, instructorRole, "일반 강사");
        entityManager.clear();

        // when / then
        assertClassError(
                () -> queryService.findAllStudioStudents(
                        instructor.getId(),
                        context.studio().getId(),
                        context.classSession().getId()
                ),
                ClassErrorCode.CLASS_SESSION_NOT_FOUND
        );
    }

    @Test
    void 예약_관리_권한이_없는_강사는_후보_회원을_조회할_수_없다() {
        // given
        DetailContext context = 기본_환경("no-permission-candidates");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member instructor = 회원을_저장한다("권한 없는 강사", null);
        소속을_저장한다(context.studio(), instructor, instructorRole, "권한 없는 강사");
        entityManager.clear();

        // when / then
        assertStudioError(
                () -> queryService.findAllStudioStudents(
                        instructor.getId(),
                        context.studio().getId(),
                        context.classSession().getId()
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    private DetailContext 기본_환경(String suffix) {
        Member owner = 회원을_저장한다("대표-" + suffix, null);
        Studio studio = 시설을_저장한다(owner, "시설-" + suffix);
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                owner,
                역할을_저장한다(studio, SystemRole.OWNER),
                "이지은 강사"
        );
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "리포머")
        );
        정책을_저장한다(studio);
        ClassSession classSession = 수업을_저장한다(
                studio,
                instructorMembership,
                classType,
                "리포머 밸런스"
        );
        return new DetailContext(owner, studio, instructorMembership, classType, classSession);
    }

    private Member 회원을_저장한다(String name, String profileImageUrl) {
        Member member = Member.builder()
                .name(name)
                .phoneNumber("010%08d".formatted(phoneSequence++))
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

    private StudioRole 역할을_저장한다(Studio studio, SystemRole systemRole) {
        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private StudioRole 사용자_역할을_저장한다(Studio studio, String name) {
        StudioRole role = StudioRole.builder()
                .studio(studio)
                .name(name)
                .instructor(false)
                .build();
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            StudioRole role,
            String name
    ) {
        return 소속을_저장한다(studio, member, role, MembershipStatus.ACTIVE, name);
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            StudioRole role,
            MembershipStatus status,
            String name
    ) {
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .phoneNumber(member.getPhoneNumber())
                .studioRole(role)
                .name(name)
                .status(status)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    private StudioMembership 학생_소속을_저장한다(
            Studio studio,
            StudioRole studentRole,
            String name
    ) {
        return 소속을_저장한다(studio, 회원을_저장한다(name, null), studentRole, name);
    }

    private void 권한을_저장한다(StudioRole role, PermissionCode code) {
        Permission permission = permissionRepository.findByCodeIn(List.of(code)).getFirst();
        studioRolePermissionRepository.saveAndFlush(StudioRolePermission.builder()
                .studioRole(role)
                .permission(permission)
                .build());
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

    private ClassSession 수업을_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                "체어룸에서 진행",
                ClassForm.GROUP,
                60,
                8,
                SESSION_START
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private ClassSessionEnrollment 예약을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            LocalDateTime reservedAt
    ) {
        ClassSessionEnrollment enrollment = ClassSessionEnrollment.reservedWithoutPassProduct(
                membership,
                classSession,
                reservedAt
        );
        entityManager.persist(enrollment);
        entityManager.flush();
        return enrollment;
    }

    private void assertClassError(Runnable action, ClassErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private void assertStudioError(Runnable action, StudioErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private record DetailContext(
            Member owner,
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
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
