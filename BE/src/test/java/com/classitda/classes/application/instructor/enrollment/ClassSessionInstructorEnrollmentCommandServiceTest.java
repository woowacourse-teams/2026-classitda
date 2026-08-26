package com.classitda.classes.application.instructor.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({
        ClassSessionInstructorEnrollmentCommandService.class,
        InstructorSessionAccessReader.class,
        ClassSessionInstructorEnrollmentCommandServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class ClassSessionInstructorEnrollmentCommandServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 12, 0);
    private static final LocalDateTime 시작_예정 = LocalDateTime.of(2026, 8, 17, 20, 0);
    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final Map<String, Long> 역할_캐시 = new HashMap<>();
    private final Map<String, Long> 소속_캐시 = new HashMap<>();

    private final ClassSessionInstructorEnrollmentCommandService commandService;
    private final ClassSessionEnrollmentRepository enrollmentRepository;
    private final PermissionRepository permissionRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final EntityManager entityManager;

    @Autowired
    ClassSessionInstructorEnrollmentCommandServiceTest(
            ClassSessionInstructorEnrollmentCommandService commandService,
            ClassSessionEnrollmentRepository enrollmentRepository,
            PermissionRepository permissionRepository,
            StudioRolePermissionRepository studioRolePermissionRepository,
            EntityManager entityManager
    ) {
        this.commandService = commandService;
        this.enrollmentRepository = enrollmentRepository;
        this.permissionRepository = permissionRepository;
        this.studioRolePermissionRepository = studioRolePermissionRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 대표가_회원을_수업_회차에_예약하면_예약_상태로_저장된다() {
        // given
        Member owner = 회원을_저장한다("owner-reserve");
        Studio studio = 시설을_저장한다(owner, "리포머 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-reserve", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());

        // then
        ClassSessionEnrollment enrollment = 신청을_다시_읽는다(classSession.getId(), student.getId());
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(NOW);
    }

    @Test
    void 수강권_없이도_예약이_저장된다() {
        // given
        Member owner = 회원을_저장한다("owner-no-pass");
        Studio studio = 시설을_저장한다(owner, "필라테스 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-no-pass", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());

        // then
        ClassSessionEnrollment enrollment = 신청을_다시_읽는다(classSession.getId(), student.getId());
        assertThat(enrollment.getMemberPassProduct()).isNull();
    }

    @Test
    void 전체_수업_관리_권한이_있는_소속도_회원을_예약할_수_있다() {
        // given
        Member owner = 회원을_저장한다("owner-permitted");
        Studio studio = 시설을_저장한다(owner, "권한 스튜디오");
        Member manager = 회원을_저장한다("manager-permitted");
        StudioRole managerRole = 역할을_저장한다(studio, SystemRole.INSTRUCTOR);
        소속을_저장한다(studio, manager, managerRole, MembershipStatus.ACTIVE);
        권한을_저장한다(managerRole, PermissionCode.RESERVATION_MANAGE);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        StudioMembership student = 학생_소속을_저장한다(studio, "student-permitted", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when
        commandService.save(manager.getId(), studio.getId(), classSession.getId(), student.getId());

        // then
        ClassSessionEnrollment enrollment = 신청을_다시_읽는다(classSession.getId(), student.getId());
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
    }

    @Test
    void 본인_수업_관리_권한이_있는_강사는_본인_수업에_회원을_예약할_수_있다() {
        // given
        Member owner = 회원을_저장한다("owner-own-reserve");
        Studio studio = 시설을_저장한다(owner, "본인 수업 예약 스튜디오");
        Member instructor = 회원을_저장한다("instructor-own-reserve");
        StudioRole instructorRole = 역할을_저장한다(studio, SystemRole.INSTRUCTOR);
        소속을_저장한다(studio, instructor, instructorRole, MembershipStatus.ACTIVE);
        권한을_저장한다(instructorRole, PermissionCode.RESERVATION_MANAGE);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        StudioMembership student = 학생_소속을_저장한다(
                studio,
                "student-own-reserve",
                MembershipStatus.ACTIVE
        );
        ClassSession classSession = 수업_회차를_저장한다(studio, instructor, 시작_예정, 12);

        // when
        commandService.save(instructor.getId(), studio.getId(), classSession.getId(), student.getId());

        // then
        ClassSessionEnrollment enrollment = 신청을_다시_읽는다(classSession.getId(), student.getId());
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
    }

    @Test
    void 본인_수업_관리_권한이_있는_강사는_다른_강사_수업에_회원을_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-other-reserve");
        Studio studio = 시설을_저장한다(owner, "다른 강사 수업 예약 스튜디오");
        Member instructor = 회원을_저장한다("instructor-other-reserve");
        StudioRole instructorRole = 역할을_저장한다(studio, SystemRole.INSTRUCTOR);
        소속을_저장한다(studio, instructor, instructorRole, MembershipStatus.ACTIVE);
        권한을_저장한다(instructorRole, PermissionCode.RESERVATION_MANAGE);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        StudioMembership student = 학생_소속을_저장한다(
                studio,
                "student-other-reserve",
                MembershipStatus.ACTIVE
        );
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                instructor.getId(), studio.getId(), classSession.getId(), student.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.CLASS_SESSION_NOT_FOUND.getMessage());
    }

    @Test
    void 예약_관리_권한만_있고_수업_관리_범위가_없으면_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-no-session-scope");
        Studio studio = 시설을_저장한다(owner, "수업 범위 없는 스튜디오");
        Member manager = 회원을_저장한다("manager-no-session-scope");
        StudioRole managerRole = 역할을_저장한다(studio, SystemRole.INSTRUCTOR);
        소속을_저장한다(studio, manager, managerRole, MembershipStatus.ACTIVE);
        권한을_저장한다(managerRole, PermissionCode.RESERVATION_MANAGE);
        StudioMembership student = 학생_소속을_저장한다(
                studio,
                "student-no-session-scope",
                MembershipStatus.ACTIVE
        );
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                manager.getId(), studio.getId(), classSession.getId(), student.getId()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.PERMISSION_DENIED.getMessage());
    }

    @Test
    void 예약_관리_권한이_없으면_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-denied");
        Studio studio = 시설을_저장한다(owner, "권한 없는 스튜디오");
        Member other = 회원을_저장한다("other-denied");
        StudioRole role = 역할을_저장한다(studio, SystemRole.STUDENT);
        소속을_저장한다(studio, other, role, MembershipStatus.ACTIVE);
        StudioMembership student = 학생_소속을_저장한다(studio, "student-denied", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                other.getId(), studio.getId(), classSession.getId(), student.getId()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.PERMISSION_DENIED.getMessage());
    }

    @Test
    void 비활성_소속_회원은_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-inactive");
        Studio studio = 시설을_저장한다(owner, "비활성 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-inactive", MembershipStatus.INACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), classSession.getId(), student.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.ENROLLMENT_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void 다른_시설의_회원은_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-cross");
        Studio studio = 시설을_저장한다(owner, "우리 스튜디오");
        Member otherOwner = 회원을_저장한다("owner-cross-other");
        Studio otherStudio = 시설을_저장한다(otherOwner, "남의 스튜디오");
        StudioMembership otherStudent = 학생_소속을_저장한다(otherStudio, "student-cross", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), classSession.getId(), otherStudent.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.ENROLLMENT_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void 강사_소속은_수업_회차에_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-instructor-target");
        Studio studio = 시설을_저장한다(owner, "강사 예약 차단 스튜디오");
        Member instructor = 회원을_저장한다("instructor-target");
        StudioMembership instructorMembership = 강사_소속을_저장한다(studio, instructor);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), classSession.getId(), instructorMembership.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.ENROLLMENT_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void 다른_시설의_수업_회차에는_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-session-cross");
        Studio studio = 시설을_저장한다(owner, "회차 스튜디오");
        강사_소속을_저장한다(studio, owner);
        Member otherOwner = 회원을_저장한다("owner-session-cross-other");
        Studio otherStudio = 시설을_저장한다(otherOwner, "남의 회차 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-session-cross", MembershipStatus.ACTIVE);
        ClassSession otherSession = 수업_회차를_저장한다(otherStudio, otherOwner, 시작_예정, 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), otherSession.getId(), student.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.CLASS_SESSION_NOT_FOUND.getMessage());
    }

    @Test
    void 이미_시작한_수업_회차에는_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-started");
        Studio studio = 시설을_저장한다(owner, "지난 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-started", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(
                studio, owner, LocalDateTime.of(2026, 8, 17, 10, 0), 12);

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), classSession.getId(), student.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.ENROLLMENT_SESSION_NOT_SCHEDULED.getMessage());
    }

    @Test
    void 취소된_수업_회차에는_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-canceled-session");
        Studio studio = 시설을_저장한다(owner, "취소 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-canceled-session", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);
        classSession.cancel(NOW);
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), classSession.getId(), student.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.ENROLLMENT_SESSION_NOT_SCHEDULED.getMessage());
    }

    @Test
    void 정원이_모두_차면_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-capacity");
        Studio studio = 시설을_저장한다(owner, "정원 스튜디오");
        StudioMembership first = 학생_소속을_저장한다(studio, "student-capacity-1", MembershipStatus.ACTIVE);
        StudioMembership second = 학생_소속을_저장한다(studio, "student-capacity-2", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 1);
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), first.getId());

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), classSession.getId(), second.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.ENROLLMENT_CAPACITY_EXCEEDED.getMessage());
    }

    @Test
    void 같은_회원을_같은_회차에_두_번_예약할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-duplicate");
        Studio studio = 시설을_저장한다(owner, "중복 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-duplicate", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());

        // when / then
        assertThatThrownBy(() -> commandService.save(
                owner.getId(), studio.getId(), classSession.getId(), student.getId()))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.ENROLLMENT_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 예약을_취소하면_취소_상태로_남는다() {
        // given
        Member owner = 회원을_저장한다("owner-cancel");
        Studio studio = 시설을_저장한다(owner, "취소 처리 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-cancel", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());
        Long enrollmentId = 신청을_다시_읽는다(classSession.getId(), student.getId()).getId();

        // when
        commandService.cancel(owner.getId(), studio.getId(), classSession.getId(), enrollmentId);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSessionEnrollment canceled = enrollmentRepository.findById(enrollmentId).orElseThrow();
        assertThat(canceled.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CANCELED);
    }

    @Test
    void 취소된_예약이_있으면_같은_회원을_다시_예약할_수_있다() {
        // given
        Member owner = 회원을_저장한다("owner-rebook");
        Studio studio = 시설을_저장한다(owner, "재예약 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-rebook", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());
        Long enrollmentId = 신청을_다시_읽는다(classSession.getId(), student.getId()).getId();
        commandService.cancel(owner.getId(), studio.getId(), classSession.getId(), enrollmentId);
        entityManager.flush();

        // when
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());

        // then
        assertThat(enrollmentRepository.countOccupied(classSession.getId())).isEqualTo(1L);
    }

    @Test
    void 다른_회차의_예약은_취소할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-cancel-cross");
        Studio studio = 시설을_저장한다(owner, "교차 취소 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-cancel-cross", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);
        ClassSession otherSession = 수업_회차를_저장한다(
                studio, owner, LocalDateTime.of(2026, 8, 18, 20, 0), 12);
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());
        Long enrollmentId = 신청을_다시_읽는다(classSession.getId(), student.getId()).getId();

        // when / then
        assertThatThrownBy(() -> commandService.cancel(
                owner.getId(), studio.getId(), otherSession.getId(), enrollmentId))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.CLASS_SESSION_ENROLLMENT_NOT_FOUND.getMessage());
    }

    @Test
    void 본인_수업_관리_권한이_있는_강사는_다른_강사_수업의_예약을_취소할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-other-cancel");
        Studio studio = 시설을_저장한다(owner, "다른 강사 예약 취소 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(
                studio,
                "student-other-cancel",
                MembershipStatus.ACTIVE
        );
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());
        Long enrollmentId = 신청을_다시_읽는다(classSession.getId(), student.getId()).getId();

        Member instructor = 회원을_저장한다("instructor-other-cancel");
        StudioRole instructorRole = 역할을_저장한다(studio, SystemRole.INSTRUCTOR);
        소속을_저장한다(studio, instructor, instructorRole, MembershipStatus.ACTIVE);
        권한을_저장한다(instructorRole, PermissionCode.RESERVATION_MANAGE);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);

        // when / then
        assertThatThrownBy(() -> commandService.cancel(
                instructor.getId(), studio.getId(), classSession.getId(), enrollmentId))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.CLASS_SESSION_NOT_FOUND.getMessage());
    }

    @Test
    void 이미_취소된_예약은_다시_취소할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-recancel");
        Studio studio = 시설을_저장한다(owner, "재취소 스튜디오");
        StudioMembership student = 학생_소속을_저장한다(studio, "student-recancel", MembershipStatus.ACTIVE);
        ClassSession classSession = 수업_회차를_저장한다(studio, owner, 시작_예정, 12);
        commandService.save(owner.getId(), studio.getId(), classSession.getId(), student.getId());
        Long enrollmentId = 신청을_다시_읽는다(classSession.getId(), student.getId()).getId();
        commandService.cancel(owner.getId(), studio.getId(), classSession.getId(), enrollmentId);
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> commandService.cancel(
                owner.getId(), studio.getId(), classSession.getId(), enrollmentId))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION.getMessage());
    }

    private Member 회원을_저장한다(String providerId) {
        Member member = StudioFixture.아이디가_다른_소유자(providerId);
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Studio 시설을_저장한다(Member owner, String name) {
        Studio studio = Studio.builder()
                .owner(owner)
                .name(name)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .address(StudioFixture.기본_주소())
                .build();
        entityManager.persist(studio);
        entityManager.flush();
        return studio;
    }

    private StudioMembership 학생_소속을_저장한다(Studio studio, String providerId, MembershipStatus status) {
        Member member = 회원을_저장한다(providerId);
        StudioRole role = 역할을_저장한다(studio, SystemRole.STUDENT);
        return 소속을_저장한다(studio, member, role, status);
    }

    private StudioRole 역할을_저장한다(Studio studio, SystemRole systemRole) {
        String key = studio.getId() + ":" + systemRole;
        Long cachedId = 역할_캐시.get(key);
        if (cachedId != null) {
            return entityManager.find(StudioRole.class, cachedId);
        }

        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        entityManager.flush();
        역할_캐시.put(key, role.getId());
        return role;
    }

    private StudioMembership 강사_소속을_저장한다(Studio studio, Member instructor) {
        return 소속을_저장한다(
                studio,
                instructor,
                역할을_저장한다(studio, SystemRole.INSTRUCTOR),
                MembershipStatus.ACTIVE
        );
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            StudioRole role,
            MembershipStatus status
    ) {
        String key = studio.getId() + ":" + member.getId();
        Long cachedId = 소속_캐시.get(key);
        if (cachedId != null) {
            return entityManager.find(StudioMembership.class, cachedId);
        }

        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .phoneNumber(member.getPhoneNumber())
                .name(member.getName())
                .studioRole(role)
                .status(status)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        소속_캐시.put(key, membership.getId());
        return membership;
    }

    private void 권한을_저장한다(StudioRole role, PermissionCode code) {
        Permission permission = permissionRepository.findByCodeIn(List.of(code)).getFirst();
        studioRolePermissionRepository.saveAndFlush(StudioRolePermission.builder()
                .studioRole(role)
                .permission(permission)
                .build());
    }

    private ClassSession 수업_회차를_저장한다(
            Studio studio,
            Member instructor,
            LocalDateTime startAt,
            int capacity
    ) {
        StudioMembership instructorMembership = 강사_소속을_저장한다(studio, instructor);
        ClassSession classSession = ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                "저녁 요가",
                "퇴근 후 진행하는 수업",
                ClassForm.GROUP,
                60,
                capacity,
                startAt
        );
        entityManager.persist(classSession);
        entityManager.flush();
        return classSession;
    }

    private ClassSessionEnrollment 신청을_다시_읽는다(Long classSessionId, Long membershipId) {
        entityManager.flush();
        entityManager.clear();
        return enrollmentRepository.findAll().stream()
                .filter(enrollment -> enrollment.getClassSession().getId().equals(classSessionId))
                .filter(enrollment -> enrollment.getMembership().getId().equals(membershipId))
                .filter(enrollment -> enrollment.getEnrollmentStatus() != EnrollmentStatus.CANCELED)
                .findFirst()
                .orElseThrow();
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW.atZone(SERVICE_ZONE_ID).toInstant(), SERVICE_ZONE_ID);
        }
    }
}
