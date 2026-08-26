package com.classitda.studio.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.ClassTypeService;
import com.classitda.member.domain.Member;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ClassTypeService.class, StudioPermissionService.class, StudioService.class})
@MySqlRepositoryTest
class StudioPermissionServiceTest {

    private final StudioPermissionService permissionChecker;
    private final StudioService studioService;
    private final StudioRepository studioRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final EntityManager entityManager;

    @Autowired
    StudioPermissionServiceTest(
            StudioPermissionService permissionChecker,
            StudioService studioService,
            StudioRepository studioRepository,
            StudioRoleRepository studioRoleRepository,
            StudioRolePermissionRepository studioRolePermissionRepository,
            EntityManager entityManager
    ) {
        this.permissionChecker = permissionChecker;
        this.studioService = studioService;
        this.studioRepository = studioRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.studioRolePermissionRepository = studioRolePermissionRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 대표_강사는_모든_권한을_통과한다() {
        // given
        Member owner = 회원을_저장한다("owner");
        Studio studio = 시설을_만든다(owner);

        // when / then
        for (PermissionCode code : PermissionCode.values()) {
            assertThatCode(() -> permissionChecker.validate(studio, owner.getId(), code))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void 일반_강사는_수업_관리_권한을_가진다() {
        // given
        Member owner = 회원을_저장한다("owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(studio, "instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);

        // when / then
        assertThatCode(() -> permissionChecker.validate(
                studio, instructor.getId(), PermissionCode.CLASS_SESSION_MANAGE_OWN))
                .doesNotThrowAnyException();
    }

    @Test
    void 활성_상태의_시설_직원은_직원_검증을_통과한다() {
        // given
        Member owner = 회원을_저장한다("staff-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(
                studio,
                "active-staff",
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );

        // when / then
        assertThatCode(() -> permissionChecker.validateStaff(studio, instructor.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void 학생은_직원_검증에서_PERMISSION_001을_던진다() {
        // given
        Member owner = 회원을_저장한다("student-staff-owner");
        Studio studio = 시설을_만든다(owner);
        Member student = 소속을_만든다(
                studio,
                "student-staff-member",
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );

        // when / then
        assertThatThrownBy(() -> permissionChecker.validateStaff(studio, student.getId()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.PERMISSION_DENIED.getMessage());
    }

    @Test
    void 일반_강사는_룸_관리_권한이_없다() {
        // given
        Member owner = 회원을_저장한다("owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(studio, "instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);

        // when / then
        assertThatThrownBy(() -> permissionChecker.validate(
                studio, instructor.getId(), PermissionCode.ROOM_MANAGE))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.PERMISSION_DENIED.getMessage());
    }

    @Test
    void 회원은_어떤_권한도_가지지_않는다() {
        // given
        Member owner = 회원을_저장한다("owner");
        Studio studio = 시설을_만든다(owner);
        Member student = 소속을_만든다(studio, "student", SystemRole.STUDENT, MembershipStatus.ACTIVE);

        // when / then
        for (PermissionCode code : PermissionCode.values()) {
            assertThatThrownBy(() -> permissionChecker.validate(studio, student.getId(), code))
                    .isInstanceOf(StudioException.class)
                    .hasMessage(StudioErrorCode.PERMISSION_DENIED.getMessage());
        }
    }

    @Test
    void 소속이_아니면_MEMBERSHIP_001을_던진다() {
        // given
        Member owner = 회원을_저장한다("owner");
        Studio studio = 시설을_만든다(owner);
        Member stranger = 회원을_저장한다("stranger");

        // when / then
        assertThatThrownBy(() -> permissionChecker.validate(
                studio, stranger.getId(), PermissionCode.ROOM_MANAGE))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 정지된_소속은_MEMBERSHIP_002를_던진다() {
        // given
        Member owner = 회원을_저장한다("owner");
        Studio studio = 시설을_만든다(owner);
        Member suspended = 소속을_만든다(studio, "suspended", SystemRole.INSTRUCTOR, MembershipStatus.INACTIVE);

        // when / then
        assertThatThrownBy(() -> permissionChecker.validate(
                studio, suspended.getId(), PermissionCode.CLASS_SESSION_MANAGE_OWN))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.MEMBERSHIP_INACTIVE.getMessage());
    }

    @Test
    void 시설을_만들면_시스템_역할에_기본_권한만_매핑된다() {
        // given
        Member owner = 회원을_저장한다("owner");
        Studio studio = 시설을_만든다(owner);
        StudioRole instructorRole = 역할을_찾는다(studio, SystemRole.INSTRUCTOR);

        // when / then
        for (PermissionCode code : PermissionCode.values()) {
            boolean granted = studioRolePermissionRepository
                    .existsByStudioRoleIdAndPermissionCode(instructorRole.getId(), code);
            assertThat(granted)
                    .as("일반 강사 역할의 %s 권한", code)
                    .isEqualTo(SystemRole.INSTRUCTOR.getDefaultPermissions().contains(code));
        }
    }

    private Member 회원을_저장한다(String providerId) {
        Member member = StudioFixture.아이디가_다른_소유자(providerId);
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Studio 시설을_만든다(Member owner) {
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        entityManager.flush();
        return studioRepository.findById(studioId).orElseThrow();
    }

    private StudioRole 역할을_찾는다(Studio studio, SystemRole systemRole) {
        return studioRoleRepository.findAll().stream()
                .filter(it -> it.getStudio().getId().equals(studio.getId()) && it.getSystemRole() == systemRole)
                .findFirst()
                .orElseThrow();
    }

    private Member 소속을_만든다(Studio studio, String providerId, SystemRole systemRole, MembershipStatus status) {
        Member member = 회원을_저장한다(providerId);
        StudioRole role = 역할을_찾는다(studio, systemRole);
        entityManager.persist(StudioMembership.builder()
                .studio(studio)
                .member(member)
                .name(member.getName())
                .studioRole(role)
                .status(status)
                .joinedAt(LocalDateTime.now())
                .build());
        entityManager.flush();
        return member;
    }
}
