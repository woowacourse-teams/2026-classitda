package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassTypeErrorCode;
import com.classitda.classes.exception.ClassTypeException;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.classes.presentation.dto.ClassTypeUpdateRequest;
import com.classitda.member.domain.Member;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.application.StudioService;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Permission;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.StudioRolePermission;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.PermissionRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ClassTypeService.class, StudioService.class, StudioPermissionService.class})
@MySqlRepositoryTest
class ClassTypeServiceTest {

    private final ClassTypeService classTypeService;
    private final StudioService studioService;
    private final StudioRepository studioRepository;
    private final ClassTypeRepository classTypeRepository;
    private final PermissionRepository permissionRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final EntityManager entityManager;

    @Autowired
    ClassTypeServiceTest(
            ClassTypeService classTypeService,
            StudioService studioService,
            StudioRepository studioRepository,
            ClassTypeRepository classTypeRepository,
            PermissionRepository permissionRepository,
            StudioRoleRepository studioRoleRepository,
            StudioRolePermissionRepository studioRolePermissionRepository,
            EntityManager entityManager
    ) {
        this.classTypeService = classTypeService;
        this.studioService = studioService;
        this.studioRepository = studioRepository;
        this.classTypeRepository = classTypeRepository;
        this.permissionRepository = permissionRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.studioRolePermissionRepository = studioRolePermissionRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 대표_강사가_수업_종류를_등록하면_저장하고_응답한다() {
        // given
        Member owner = 회원을_저장한다("class-type-owner");
        Studio studio = 시설을_만든다(owner);

        // when
        ClassTypeResponse response = classTypeService.save(
                owner.getId(), studio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청());

        // then
        ClassType saved = classTypeRepository.findById(response.id()).orElseThrow();
        assertThat(response.name()).isEqualTo("일반 요가");
        assertThat(saved.getStudio().getId()).isEqualTo(studio.getId());
        assertThat(saved.getName()).isEqualTo(response.name());

        StudioRole ownerRole = 역할을_찾는다(studio, SystemRole.OWNER);
        assertThat(studioRolePermissionRepository.existsByStudioRoleIdAndPermissionCode(
                ownerRole.getId(), PermissionCode.CLASS_TYPE_MANAGE)).isTrue();
    }

    @Test
    void 없는_시설에는_수업_종류를_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("missing-studio-owner");

        // when / then
        assertThatThrownBy(() -> classTypeService.save(
                owner.getId(), 999L, ClassTypeFixture.기본_수업_종류_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void 일반_강사는_수업_종류를_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("permission-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(studio, "regular-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);

        // when / then
        assertThatThrownBy(() -> classTypeService.save(
                instructor.getId(), studio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.PERMISSION_DENIED.getMessage());
    }

    @Test
    void 소속이_아니면_수업_종류를_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("membership-owner");
        Studio studio = 시설을_만든다(owner);
        Member stranger = 회원을_저장한다("class-type-stranger");

        // when / then
        assertThatThrownBy(() -> classTypeService.save(
                stranger.getId(), studio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 비활성_소속은_수업_종류를_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("inactive-owner");
        Studio studio = 시설을_만든다(owner);
        Member inactive = 소속을_만든다(studio, "inactive-instructor", SystemRole.INSTRUCTOR, MembershipStatus.INACTIVE);

        // when / then
        assertThatThrownBy(() -> classTypeService.save(
                inactive.getId(), studio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.MEMBERSHIP_INACTIVE.getMessage());
    }

    @Test
    void 같은_시설에_같은_이름의_수업_종류를_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("duplicate-owner");
        Studio studio = 시설을_만든다(owner);
        classTypeService.save(owner.getId(), studio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청());

        // when / then
        assertThatThrownBy(() -> classTypeService.save(
                owner.getId(), studio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청()))
                .isInstanceOf(ClassTypeException.class)
                .hasMessage(ClassTypeErrorCode.CLASS_TYPE_NAME_DUPLICATED.getMessage());
    }

    @Test
    void 다른_시설에는_같은_이름의_수업_종류를_등록할_수_있다() {
        // given
        Member owner = 회원을_저장한다("other-studio-owner");
        Studio firstStudio = 시설을_만든다(owner);
        Studio secondStudio = 시설을_만든다(owner);
        classTypeService.save(owner.getId(), firstStudio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청());

        // when
        ClassTypeResponse response = classTypeService.save(
                owner.getId(), secondStudio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청());

        // then
        assertThat(response.id()).isNotNull();
        assertThat(classTypeRepository.count()).isEqualTo(2);
    }

    @Test
    void 시설의_모든_수업_종류를_아이디_오름차순으로_조회하고_다른_시설은_제외한다() {
        // given
        Member owner = 회원을_저장한다("class-type-list-owner");
        Studio studio = 시설을_만든다(owner);
        Studio otherStudio = 시설을_만든다(owner);

        ClassType first = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "일반 요가"));
        ClassType second = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "리포머 요가"));
        ClassType third = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "플라잉 요가"));

        classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(otherStudio, "다른 시설 요가"));

        // when
        List<ClassTypeResponse> responses = classTypeService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(responses)
                .extracting(ClassTypeResponse::id, ClassTypeResponse::name)
                .containsExactly(
                        tuple(first.getId(), first.getName()),
                        tuple(second.getId(), second.getName()),
                        tuple(third.getId(), third.getName())
                );
    }

    @Test
    void 수업_종류가_없는_기존_시설을_조회하면_빈_목록을_반환한다() {
        // given
        Member owner = 회원을_저장한다("empty-class-type-owner");
        Studio studio = 시설을_만든다(owner);

        // when
        List<ClassTypeResponse> responses = classTypeService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 없는_시설의_수업_종류를_조회하면_STUDIO_002_예외가_발생한다() {
        // given
        Long missingStudioId = 999L;

        // when / then
        assertThatThrownBy(() -> classTypeService.findAll(999L, missingStudioId))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_FOUND));
    }

    @Test
    void 수업_종류_관리_권한을_받은_활성_일반_강사는_목록을_조회할_수_있다() {
        // given
        Member owner = 회원을_저장한다("granted-list-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(
                studio, "granted-list-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);
        수업_종류_관리_권한을_부여한다(studio, SystemRole.INSTRUCTOR);
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));

        // when
        List<ClassTypeResponse> responses = classTypeService.findAll(instructor.getId(), studio.getId());

        // then
        assertThat(responses)
                .extracting(ClassTypeResponse::id, ClassTypeResponse::name)
                .containsExactly(tuple(classType.getId(), classType.getName()));
    }

    @Test
    void 소속이_아닌_회원은_수업_종류_목록을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("list-membership-owner");
        Studio studio = 시설을_만든다(owner);
        Member stranger = 회원을_저장한다("list-stranger");

        // when / then
        assertThatThrownBy(() -> classTypeService.findAll(stranger.getId(), studio.getId()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_MEMBERSHIP));
    }

    @Test
    void 비활성_소속은_수업_종류_목록을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("inactive-list-owner");
        Studio studio = 시설을_만든다(owner);
        Member inactive = 소속을_만든다(
                studio, "inactive-list-instructor", SystemRole.INSTRUCTOR, MembershipStatus.INACTIVE);

        // when / then
        assertThatThrownBy(() -> classTypeService.findAll(inactive.getId(), studio.getId()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.MEMBERSHIP_INACTIVE));
    }

    @Test
    void 관리_권한이_없는_활성_일반_강사는_수업_종류_목록을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("denied-list-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(
                studio, "denied-list-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);

        // when / then
        assertThatThrownBy(() -> classTypeService.findAll(instructor.getId(), studio.getId()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
    }

    @Test
    void 대표_강사가_수업_종류_이름을_수정하면_같은_아이디와_새_이름을_응답하고_저장한다() {
        // given
        Member owner = 회원을_저장한다("update-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));
        ClassTypeUpdateRequest request = ClassTypeFixture.기본_수업_종류_수정_요청();

        // when
        ClassTypeResponse response = classTypeService.update(
                owner.getId(), studio.getId(), classType.getId(), request);
        entityManager.clear();

        // then
        ClassType updated = classTypeRepository.findById(classType.getId()).orElseThrow();
        assertThat(response.id()).isEqualTo(classType.getId());
        assertThat(response.name()).isEqualTo("리포머 요가");
        assertThat(updated.getName()).isEqualTo("리포머 요가");
    }

    @Test
    void 수업_종류를_현재_이름으로_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("same-name-update-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));

        // when
        ClassTypeResponse response = classTypeService.update(
                owner.getId(), studio.getId(), classType.getId(),
                ClassTypeFixture.이름이_다른_수업_종류_수정_요청(classType.getName()));

        // then
        assertThat(response.id()).isEqualTo(classType.getId());
        assertThat(response.name()).isEqualTo("일반 요가");
    }

    @Test
    void 같은_시설의_다른_수업_종류_이름으로_수정하면_CLASS_TYPE_002가_발생한다() {
        // given
        Member owner = 회원을_저장한다("duplicate-update-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType target = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));
        ClassType duplicate = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "리포머 요가"));

        // when / then
        assertThatThrownBy(() -> classTypeService.update(
                owner.getId(), studio.getId(), target.getId(),
                ClassTypeFixture.이름이_다른_수업_종류_수정_요청(duplicate.getName())))
                .isInstanceOfSatisfying(ClassTypeException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassTypeErrorCode.CLASS_TYPE_NAME_DUPLICATED));
    }

    @Test
    void 다른_시설에서만_사용하는_이름으로_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("other-studio-update-owner");
        Studio studio = 시설을_만든다(owner);
        Studio otherStudio = 시설을_만든다(owner);
        ClassType target = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));
        classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(otherStudio, "리포머 요가"));

        // when
        ClassTypeResponse response = classTypeService.update(
                owner.getId(), studio.getId(), target.getId(), ClassTypeFixture.기본_수업_종류_수정_요청());

        // then
        assertThat(response.name()).isEqualTo("리포머 요가");
        assertThat(classTypeRepository.findById(target.getId()).orElseThrow().getName())
                .isEqualTo("리포머 요가");
    }

    @Test
    void 없는_시설의_수업_종류를_수정하면_STUDIO_002가_먼저_발생하고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("missing-studio-update-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));

        // when / then
        assertThatThrownBy(() -> classTypeService.update(
                owner.getId(), 999L, classType.getId(), ClassTypeFixture.기본_수업_종류_수정_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_FOUND));
        assertThat(classTypeRepository.findById(classType.getId()).orElseThrow().getName())
                .isEqualTo("일반 요가");
    }

    @Test
    void 일반_강사가_없는_수업_종류를_수정하면_PERMISSION_001이_먼저_발생하고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("update-permission-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(
                studio, "update-regular-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));

        // when / then
        assertThatThrownBy(() -> classTypeService.update(
                instructor.getId(), studio.getId(), 999L, ClassTypeFixture.기본_수업_종류_수정_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThat(classTypeRepository.findById(classType.getId()).orElseThrow().getName())
                .isEqualTo("일반 요가");
    }

    @Test
    void 소속이_아닌_회원이_없는_수업_종류를_수정하면_MEMBERSHIP_001이_먼저_발생하고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("update-membership-owner");
        Studio studio = 시설을_만든다(owner);
        Member stranger = 회원을_저장한다("update-stranger");
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));

        // when / then
        assertThatThrownBy(() -> classTypeService.update(
                stranger.getId(), studio.getId(), 999L, ClassTypeFixture.기본_수업_종류_수정_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_MEMBERSHIP));
        assertThat(classTypeRepository.findById(classType.getId()).orElseThrow().getName())
                .isEqualTo("일반 요가");
    }

    @Test
    void 기존_시설에_없는_수업_종류를_수정하면_CLASS_TYPE_003이_발생한다() {
        // given
        Member owner = 회원을_저장한다("missing-class-type-update-owner");
        Studio studio = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> classTypeService.update(
                owner.getId(), studio.getId(), 999L, ClassTypeFixture.기본_수업_종류_수정_요청()))
                .isInstanceOfSatisfying(ClassTypeException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassTypeErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    @Test
    void 다른_시설의_수업_종류를_수정하면_CLASS_TYPE_003이_발생하고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("cross-studio-update-owner");
        Studio requestedStudio = 시설을_만든다(owner);
        Studio owningStudio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(owningStudio, "다른 시설 요가"));

        // when / then
        assertThatThrownBy(() -> classTypeService.update(
                owner.getId(), requestedStudio.getId(), classType.getId(),
                ClassTypeFixture.기본_수업_종류_수정_요청()))
                .isInstanceOfSatisfying(ClassTypeException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassTypeErrorCode.CLASS_TYPE_NOT_FOUND));
        assertThat(classTypeRepository.findById(classType.getId()).orElseThrow().getName())
                .isEqualTo("다른 시설 요가");
    }

    @Test
    void 대표_강사가_수업_종류를_삭제하면_행이_사라지고_같은_이름을_다시_등록할_수_있다() {
        // given
        Member owner = 회원을_저장한다("delete-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "삭제할 요가"));

        // when
        classTypeService.delete(owner.getId(), studio.getId(), classType.getId());
        entityManager.flush();
        entityManager.clear();
        boolean deleted = classTypeRepository.findById(classType.getId()).isEmpty();

        ClassTypeResponse recreated = classTypeService.save(
                owner.getId(), studio.getId(),
                ClassTypeFixture.이름이_다른_수업_종류_생성_요청("삭제할 요가"));

        // then
        assertThat(deleted).isTrue();
        assertThat(recreated.name()).isEqualTo("삭제할 요가");
    }

    @Test
    void 일반_강사는_수업_종류를_삭제할_수_없고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("delete-permission-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(
                studio, "delete-regular-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "권한 확인 요가"));

        // when / then
        assertThatThrownBy(() -> classTypeService.delete(
                instructor.getId(), studio.getId(), classType.getId()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThat(classTypeRepository.findById(classType.getId())).isPresent();
    }

    @Test
    void 없는_시설의_수업_종류를_삭제하면_STUDIO_002가_발생하고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("delete-missing-studio-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "시설 확인 요가"));

        // when / then
        assertThatThrownBy(() -> classTypeService.delete(owner.getId(), 999L, classType.getId()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_FOUND));
        assertThat(classTypeRepository.findById(classType.getId())).isPresent();
    }

    @Test
    void 기존_시설에_없는_수업_종류를_삭제하면_CLASS_TYPE_003이_발생한다() {
        // given
        Member owner = 회원을_저장한다("delete-missing-class-type-owner");
        Studio studio = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> classTypeService.delete(owner.getId(), studio.getId(), 999L))
                .isInstanceOfSatisfying(ClassTypeException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassTypeErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    @Test
    void 다른_시설의_수업_종류를_삭제하면_CLASS_TYPE_003이_발생하고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("delete-cross-studio-owner");
        Studio requestedStudio = 시설을_만든다(owner);
        Studio owningStudio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(owningStudio, "다른 시설 요가"));

        // when / then
        assertThatThrownBy(() -> classTypeService.delete(
                owner.getId(), requestedStudio.getId(), classType.getId()))
                .isInstanceOfSatisfying(ClassTypeException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassTypeErrorCode.CLASS_TYPE_NOT_FOUND));
        assertThat(classTypeRepository.findById(classType.getId())).isPresent();
    }

    @Test
    void 이미_삭제한_수업_종류를_다시_삭제하면_CLASS_TYPE_003이_발생한다() {
        // given
        Member owner = 회원을_저장한다("delete-repeated-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "반복 삭제 요가"));
        Long classTypeId = classType.getId();
        classTypeService.delete(owner.getId(), studio.getId(), classTypeId);
        entityManager.flush();
        entityManager.clear();

        // when / then
        assertThatThrownBy(() -> classTypeService.delete(owner.getId(), studio.getId(), classTypeId))
                .isInstanceOfSatisfying(ClassTypeException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassTypeErrorCode.CLASS_TYPE_NOT_FOUND));
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
        return studioRoleRepository.findAllByStudioId(studio.getId()).stream()
                .filter(role -> role.getSystemRole() == systemRole)
                .findFirst()
                .orElseThrow();
    }

    private void 수업_종류_관리_권한을_부여한다(Studio studio, SystemRole systemRole) {
        StudioRole role = 역할을_찾는다(studio, systemRole);
        Permission permission = permissionRepository.findByCodeIn(List.of(PermissionCode.CLASS_TYPE_MANAGE))
                .stream()
                .findFirst()
                .orElseThrow();
        studioRolePermissionRepository.saveAndFlush(StudioRolePermission.builder()
                .studioRole(role)
                .permission(permission)
                .build());
    }

    private Member 소속을_만든다(
            Studio studio,
            String providerId,
            SystemRole systemRole,
            MembershipStatus status
    ) {
        Member member = 회원을_저장한다(providerId);
        StudioRole role = 역할을_찾는다(studio, systemRole);
        entityManager.persist(StudioMembership.builder()
                .studio(studio)
                .member(member)
                .studioRole(role)
                .status(status)
                .joinedAt(LocalDateTime.now())
                .build());
        entityManager.flush();
        return member;
    }
}
