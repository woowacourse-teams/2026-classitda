package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.session.ClassSessionClassType;
import com.classitda.classes.domain.template.ClassTemplate;
import com.classitda.classes.domain.template.ClassTemplateClassType;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transactionTemplate;

    @Autowired
    ClassTypeServiceTest(
            ClassTypeService classTypeService,
            StudioService studioService,
            StudioRepository studioRepository,
            ClassTypeRepository classTypeRepository,
            PermissionRepository permissionRepository,
            StudioRoleRepository studioRoleRepository,
            StudioRolePermissionRepository studioRolePermissionRepository,
            EntityManager entityManager,
            TransactionTemplate transactionTemplate
    ) {
        this.classTypeService = classTypeService;
        this.studioService = studioService;
        this.studioRepository = studioRepository;
        this.classTypeRepository = classTypeRepository;
        this.permissionRepository = permissionRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.studioRolePermissionRepository = studioRolePermissionRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
    }

    @Test
    void 대표_강사가_수업_종류를_등록하면_저장한다() {
        // given
        Member owner = 회원을_저장한다("class-type-owner");
        Studio studio = 시설을_만든다(owner);

        // when
        classTypeService.save(owner.getId(), studio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청());

        // then
        assertThat(classTypeRepository.findAllByStudioIdOrderByIdAsc(studio.getId()))
                .singleElement()
                .satisfies(saved -> {
                    assertThat(saved.getStudio().getId()).isEqualTo(studio.getId());
                    assertThat(saved.getName()).isEqualTo("일반 요가");
                });

        StudioRole ownerRole = 역할을_찾는다(studio, SystemRole.OWNER);
        assertThat(studioRolePermissionRepository.existsByStudioRoleIdAndPermissionCode(
                ownerRole.getId(), PermissionCode.CLASS_TYPE_MANAGE)).isTrue();
    }

    @Test
    void 기본_수업_종류를_저장하면_요가와_필라테스가_생성된다() {
        // given
        Member owner = 회원을_저장한다("default-class-type-owner");
        Studio studio = 시설을_만든다(owner);

        // when
        classTypeService.saveDefaultClassTypes(studio);
        entityManager.flush();

        // then
        assertThat(classTypeRepository.findAllByStudioIdOrderByIdAsc(studio.getId()))
                .extracting(ClassType::getName)
                .containsExactlyInAnyOrder("요가", "필라테스");
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
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.CLASS_TYPE_NAME_DUPLICATED.getMessage());
    }

    @Test
    void 다른_시설에는_같은_이름의_수업_종류를_등록할_수_있다() {
        // given
        Member owner = 회원을_저장한다("other-studio-owner");
        Studio firstStudio = 시설을_만든다(owner);
        Studio secondStudio = 시설을_만든다(owner);
        classTypeService.save(owner.getId(), firstStudio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청());

        // when
        classTypeService.save(
                owner.getId(), secondStudio.getId(), ClassTypeFixture.기본_수업_종류_생성_요청());

        // then
        assertThat(classTypeRepository.count()).isEqualTo(2);
        assertThat(classTypeRepository.findAllByStudioIdOrderByIdAsc(firstStudio.getId()))
                .extracting(ClassType::getName)
                .containsExactly("일반 요가");
        assertThat(classTypeRepository.findAllByStudioIdOrderByIdAsc(secondStudio.getId()))
                .extracting(ClassType::getName)
                .containsExactly("일반 요가");
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
    void 대표_강사가_수업_종류_이름을_수정하면_새_이름을_저장한다() {
        // given
        Member owner = 회원을_저장한다("update-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));
        ClassTypeUpdateRequest request = ClassTypeFixture.기본_수업_종류_수정_요청();

        // when
        classTypeService.update(owner.getId(), studio.getId(), classType.getId(), request);
        entityManager.clear();

        // then
        ClassType updated = classTypeRepository.findById(classType.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("리포머 요가");
    }

    @Test
    void 수업_종류를_현재_이름으로_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("same-name-update-owner");
        Studio studio = 시설을_만든다(owner);
        ClassType classType = classTypeRepository.saveAndFlush(ClassTypeFixture.기본_수업_종류(studio));

        // when
        classTypeService.update(
                owner.getId(), studio.getId(), classType.getId(),
                ClassTypeFixture.이름이_다른_수업_종류_수정_요청(classType.getName()));
        entityManager.clear();

        // then
        assertThat(classTypeRepository.findById(classType.getId()).orElseThrow().getName())
                .isEqualTo("일반 요가");
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
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.CLASS_TYPE_NAME_DUPLICATED));
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
        classTypeService.update(
                owner.getId(), studio.getId(), target.getId(), ClassTypeFixture.기본_수업_종류_수정_요청());
        entityManager.clear();

        // then
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
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
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
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
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

        classTypeService.save(
                owner.getId(), studio.getId(),
                ClassTypeFixture.이름이_다른_수업_종류_생성_요청("삭제할 요가"));

        // then
        assertThat(deleted).isTrue();
        assertThat(classTypeRepository.findAllByStudioIdOrderByIdAsc(studio.getId()))
                .extracting(ClassType::getName)
                .containsExactly("삭제할 요가");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 템플릿에서_사용_중인_수업_종류를_삭제하면_CLASS_TYPE_004가_발생하고_데이터가_유지된다() {
        // given
        DeleteConflictScenario scenario = 삭제_충돌_상황을_저장한다(
                "template-linked-delete-owner", true, false);

        try {
            // when / then
            사용_중_삭제_충돌을_검증한다(scenario);
            transactionTemplate.executeWithoutResult(status -> {
                assertThat(행_수("class_type", "id", scenario.classTypeId())).isOne();
                assertThat(행_수("class_template", "id", scenario.classTemplateId())).isOne();
                assertThat(행_수("class_template_class_type", "class_type_id", scenario.classTypeId())).isOne();
            });
        } finally {
            삭제_충돌_상황을_정리한다(scenario);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 수업_회차에서_사용_중인_수업_종류를_삭제하면_CLASS_TYPE_004가_발생하고_데이터가_유지된다() {
        // given
        DeleteConflictScenario scenario = 삭제_충돌_상황을_저장한다(
                "session-linked-delete-owner", false, true);

        try {
            // when / then
            사용_중_삭제_충돌을_검증한다(scenario);
            transactionTemplate.executeWithoutResult(status -> {
                assertThat(행_수("class_type", "id", scenario.classTypeId())).isOne();
                assertThat(행_수("class_session", "id", scenario.classSessionId())).isOne();
                assertThat(행_수("class_session_class_type", "class_type_id", scenario.classTypeId())).isOne();
            });
        } finally {
            삭제_충돌_상황을_정리한다(scenario);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 템플릿_연결만_제거해도_회차에서_사용_중이면_수업_종류를_삭제할_수_없다() {
        // given
        DeleteConflictScenario scenario = 삭제_충돌_상황을_저장한다(
                "partially-linked-delete-owner", true, true);

        try {
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.remove(entityManager.find(ClassTemplate.class, scenario.classTemplateId()));
                entityManager.flush();
            });

            // when / then
            사용_중_삭제_충돌을_검증한다(scenario);
            transactionTemplate.executeWithoutResult(status -> {
                assertThat(행_수("class_template", "id", scenario.classTemplateId())).isZero();
                assertThat(행_수("class_template_class_type", "class_type_id", scenario.classTypeId())).isZero();
                assertThat(행_수("class_type", "id", scenario.classTypeId())).isOne();
                assertThat(행_수("class_session", "id", scenario.classSessionId())).isOne();
                assertThat(행_수("class_session_class_type", "class_type_id", scenario.classTypeId())).isOne();
            });
        } finally {
            삭제_충돌_상황을_정리한다(scenario);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 템플릿과_회차가_삭제되어_모든_연결이_사라지면_수업_종류를_삭제할_수_있다() {
        // given
        DeleteConflictScenario scenario = 삭제_충돌_상황을_저장한다(
                "unlinked-delete-owner", true, true);
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.remove(entityManager.find(ClassTemplate.class, scenario.classTemplateId()));
            entityManager.remove(entityManager.find(ClassSession.class, scenario.classSessionId()));
            entityManager.flush();
        });

        try {
            // when
            classTypeService.delete(scenario.ownerId(), scenario.studioId(), scenario.classTypeId());

            // then
            transactionTemplate.executeWithoutResult(status -> {
                assertThat(행_수("class_template_class_type", "class_type_id", scenario.classTypeId())).isZero();
                assertThat(행_수("class_session_class_type", "class_type_id", scenario.classTypeId())).isZero();
                assertThat(행_수("class_type", "id", scenario.classTypeId())).isZero();
            });
        } finally {
            삭제_충돌_상황을_정리한다(scenario);
        }
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
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
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
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
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
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    private DeleteConflictScenario 삭제_충돌_상황을_저장한다(
            String ownerProviderId,
            boolean templateLinked,
            boolean sessionLinked
    ) {
        return transactionTemplate.execute(status -> {
            Member owner = StudioFixture.아이디가_다른_소유자(ownerProviderId);
            entityManager.persist(owner);
            Studio studio = Studio.builder()
                    .owner(owner)
                    .name("수업 종류 삭제 충돌 시설")
                    .openTime(LocalTime.of(9, 0))
                    .closeTime(LocalTime.of(22, 0))
                    .address(StudioFixture.기본_주소())
                    .build();
            entityManager.persist(studio);
            ClassType classType = ClassTypeFixture.이름이_다른_수업_종류(studio, "사용 중인 요가");
            entityManager.persist(classType);
            entityManager.flush();

            Long classTemplateId = null;
            if (templateLinked) {
                ClassTemplate template = ClassTemplate.builder()
                        .studioId(studio.getId())
                        .name("사용 중인 템플릿")
                        .description("삭제 충돌 검증")
                        .classForm(ClassForm.GROUP)
                        .durationMinutes(60)
                        .startTime(LocalTime.of(20, 0))
                        .recurringDays(Set.of(DayOfWeek.MONDAY))
                        .capacity(10)
                        .build();
                entityManager.persist(template);
                entityManager.flush();
                entityManager.persist(ClassTemplateClassType.builder()
                        .classTemplateId(template.getId())
                        .classTypeId(classType.getId())
                        .build());
                classTemplateId = template.getId();
            }

            Long instructorId = null;
            Long classSessionId = null;
            if (sessionLinked) {
                Member instructor = StudioFixture.아이디가_다른_소유자(ownerProviderId + "-instructor");
                entityManager.persist(instructor);
                StudioRole instructorRole = SystemRole.INSTRUCTOR.toStudioRole(studio);
                entityManager.persist(instructorRole);
                StudioMembership instructorMembership = StudioMembership.builder()
                        .studio(studio)
                        .member(instructor)
                        .name(instructor.getName())
                        .studioRole(instructorRole)
                        .status(MembershipStatus.ACTIVE)
                        .joinedAt(LocalDateTime.now())
                        .build();
                entityManager.persist(instructorMembership);
                ClassSession session = ClassSession.builder()
                        .studioId(studio.getId())
                        .instructorMembership(instructorMembership)
                        .name("사용 중인 회차")
                        .description("삭제 충돌 검증")
                        .classForm(ClassForm.GROUP)
                        .durationMinutes(60)
                        .capacity(10)
                        .startAt(LocalDateTime.of(2026, 8, 17, 20, 0))
                        .build();
                entityManager.persist(session);
                entityManager.flush();
                entityManager.persist(ClassSessionClassType.builder()
                        .classSessionId(session.getId())
                        .classTypeId(classType.getId())
                        .build());
                instructorId = instructor.getId();
                classSessionId = session.getId();
            }
            entityManager.flush();

            return new DeleteConflictScenario(
                    owner.getId(), instructorId, studio.getId(), classType.getId(), classTemplateId, classSessionId);
        });
    }

    private void 사용_중_삭제_충돌을_검증한다(DeleteConflictScenario scenario) {
        assertThatThrownBy(() -> classTypeService.delete(
                scenario.ownerId(), scenario.studioId(), scenario.classTypeId()))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_IN_USE));
    }

    private long 행_수(String tableName, String columnName, Long id) {
        return ((Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = :id")
                .setParameter("id", id)
                .getSingleResult()).longValue();
    }

    private void 삭제_충돌_상황을_정리한다(DeleteConflictScenario scenario) {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createNativeQuery("DELETE FROM class_template_class_type WHERE class_type_id = :classTypeId")
                    .setParameter("classTypeId", scenario.classTypeId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM class_session_class_type WHERE class_type_id = :classTypeId")
                    .setParameter("classTypeId", scenario.classTypeId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM class_template WHERE studio_id = :studioId")
                    .setParameter("studioId", scenario.studioId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM class_session WHERE studio_id = :studioId")
                    .setParameter("studioId", scenario.studioId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM class_type WHERE id = :classTypeId")
                    .setParameter("classTypeId", scenario.classTypeId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM studio_membership WHERE studio_id = :studioId")
                    .setParameter("studioId", scenario.studioId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM studio_role_permission WHERE studio_role_id IN "
                            + "(SELECT id FROM studio_role WHERE studio_id = :studioId)")
                    .setParameter("studioId", scenario.studioId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM studio_role WHERE studio_id = :studioId")
                    .setParameter("studioId", scenario.studioId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM studio WHERE id = :studioId")
                    .setParameter("studioId", scenario.studioId())
                    .executeUpdate();
            if (scenario.instructorId() != null) {
                entityManager.createNativeQuery("DELETE FROM member WHERE id = :memberId")
                        .setParameter("memberId", scenario.instructorId())
                        .executeUpdate();
            }
            entityManager.createNativeQuery("DELETE FROM member WHERE id = :memberId")
                    .setParameter("memberId", scenario.ownerId())
                    .executeUpdate();
        });
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
        classTypeRepository.deleteAll(classTypeRepository.findAllByStudioIdOrderByIdAsc(studioId));
        classTypeRepository.flush();
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
                .name(member.getName())
                .studioRole(role)
                .status(status)
                .joinedAt(LocalDateTime.now())
                .build());
        entityManager.flush();
        return member;
    }

    private record DeleteConflictScenario(
            Long ownerId,
            Long instructorId,
            Long studioId,
            Long classTypeId,
            Long classTemplateId,
            Long classSessionId
    ) {
    }
}
