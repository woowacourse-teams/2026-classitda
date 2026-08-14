package com.classitda.passproduct.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.classitda.classes.application.ClassTypeService;
import com.classitda.passproduct.domain.ClassKind;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.member.domain.Member;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.repository.PassProductClassTypeRepository;
import com.classitda.passproduct.domain.repository.PassProductRepository;
import com.classitda.passproduct.exception.PassProductErrorCode;
import com.classitda.passproduct.exception.PassProductException;
import com.classitda.passproduct.fixture.PassProductFixture;
import com.classitda.passproduct.presentation.dto.PassProductResponse;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.application.StudioService;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioRepository;
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

@Import({PassProductService.class, ClassTypeService.class, StudioService.class, StudioPermissionService.class})
@MySqlRepositoryTest
class PassProductServiceTest {

    private final PassProductService passProductService;
    private final StudioService studioService;
    private final StudioRepository studioRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final ClassTypeRepository classTypeRepository;
    private final PassProductRepository passProductRepository;
    private final PassProductClassTypeRepository passProductClassTypeRepository;
    private final EntityManager entityManager;

    @Autowired
    PassProductServiceTest(
            PassProductService passProductService,
            StudioService studioService,
            StudioRepository studioRepository,
            StudioRoleRepository studioRoleRepository,
            ClassTypeRepository classTypeRepository,
            PassProductRepository passProductRepository,
            PassProductClassTypeRepository passProductClassTypeRepository,
            EntityManager entityManager
    ) {
        this.passProductService = passProductService;
        this.studioService = studioService;
        this.studioRepository = studioRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.classTypeRepository = classTypeRepository;
        this.passProductRepository = passProductRepository;
        this.passProductClassTypeRepository = passProductClassTypeRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 대표_강사가_수강권을_등록하면_저장하고_응답한다() {
        // given
        Member owner = 회원을_저장한다("pass-product-owner");
        Studio studio = 시설을_만든다(owner);

        // when
        PassProductResponse response = passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(
                        시설의_수업_종류(studio).stream().map(ClassType::getId).toList()));

        // then
        PassProduct saved = passProductRepository.findById(response.id()).orElseThrow();
        assertThat(response.name()).isEqualTo(PassProductFixture.기본_이름);
        assertThat(response.classKind()).isEqualTo(ClassKind.GROUP);
        assertThat(response.active()).isTrue();
        assertThat(saved.getStudio().getId()).isEqualTo(studio.getId());
        assertThat(saved.getTotalCount()).isEqualTo(PassProductFixture.기본_횟수);
        assertThat(saved.getValidPeriodUnit()).isEqualTo(PassProductPeriodUnit.MONTH);
    }

    @Test
    void 수업_종류를_지정하지_않으면_수강권을_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("no-class-type-owner");
        Studio studio = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of())))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.CLASS_TYPE_REQUIRED));
        assertThat(passProductRepository.count()).isZero();
    }

    @Test
    void 수업_종류를_지정하면_연결_행을_만들고_함께_응답한다() {
        // given
        Member owner = 회원을_저장한다("with-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        List<ClassType> classTypes = 시설의_수업_종류(studio);

        // when
        PassProductResponse response = passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(classTypes.getFirst().getId())));

        // then
        assertThat(response.classTypes())
                .extracting(ClassTypeResponse::id, ClassTypeResponse::name)
                .containsExactly(tuple(classTypes.getFirst().getId(), classTypes.getFirst().getName()));
        assertThat(passProductClassTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void 같은_수업_종류를_중복해서_보내도_연결_행은_하나만_만든다() {
        // given
        Member owner = 회원을_저장한다("duplicate-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        Long classTypeId = 시설의_수업_종류(studio).getFirst().getId();

        // when
        PassProductResponse response = passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(classTypeId, classTypeId)));

        // then
        assertThat(response.classTypes()).hasSize(1);
        assertThat(passProductClassTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void 없는_수업_종류를_지정하면_CLASS_TYPE_003이_발생한다() {
        // given
        Member owner = 회원을_저장한다("missing-class-type-owner");
        Studio studio = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(999L))))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
        assertThat(passProductRepository.count()).isZero();
    }

    @Test
    void 다른_시설의_수업_종류를_지정하면_CLASS_TYPE_003이_발생한다() {
        // given
        Member owner = 회원을_저장한다("cross-studio-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        Studio otherStudio = 시설을_만든다(owner);
        Long otherClassTypeId = 시설의_수업_종류(otherStudio).getFirst().getId();

        // when / then
        assertThatThrownBy(() -> passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(otherClassTypeId))))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
        assertThat(passProductRepository.count()).isZero();
    }

    @Test
    void 없는_시설에는_수강권을_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("missing-studio-pass-owner");

        // when / then
        assertThatThrownBy(() -> passProductService.save(
                owner.getId(), 999L, PassProductFixture.기본_수강권_생성_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_FOUND));
    }

    @Test
    void 일반_강사는_수강권을_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("pass-permission-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(studio, "pass-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);

        // when / then
        assertThatThrownBy(() -> passProductService.save(
                instructor.getId(), studio.getId(), PassProductFixture.기본_수강권_생성_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
    }

    @Test
    void 소속이_아니면_수강권을_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("pass-membership-owner");
        Studio studio = 시설을_만든다(owner);
        Member stranger = 회원을_저장한다("pass-stranger");

        // when / then
        assertThatThrownBy(() -> passProductService.save(
                stranger.getId(), studio.getId(), PassProductFixture.기본_수강권_생성_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_MEMBERSHIP));
    }

    @Test
    void 비활성_소속은_수강권을_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("pass-inactive-owner");
        Studio studio = 시설을_만든다(owner);
        Member inactive = 소속을_만든다(
                studio, "pass-inactive-instructor", SystemRole.INSTRUCTOR, MembershipStatus.INACTIVE);

        // when / then
        assertThatThrownBy(() -> passProductService.save(
                inactive.getId(), studio.getId(), PassProductFixture.기본_수강권_생성_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.MEMBERSHIP_INACTIVE));
    }

    @Test
    void 시설의_수강권을_아이디_오름차순으로_조회하고_다른_시설은_제외한다() {
        // given
        Member owner = 회원을_저장한다("pass-list-owner");
        Studio studio = 시설을_만든다(owner);
        Studio otherStudio = 시설을_만든다(owner);
        PassProduct first = passProductRepository.saveAndFlush(
                PassProductFixture.이름과_수업_종류를_지정한_수강권(studio, "첫 번째 수강권", 시설의_수업_종류(studio)));
        PassProduct second = passProductRepository.saveAndFlush(
                PassProductFixture.이름과_수업_종류를_지정한_수강권(studio, "두 번째 수강권", 시설의_수업_종류(studio)));
        passProductRepository.saveAndFlush(
                PassProductFixture.이름과_수업_종류를_지정한_수강권(otherStudio, "다른 시설 수강권", 시설의_수업_종류(otherStudio)));

        // when
        List<PassProductResponse> responses = passProductService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(responses)
                .extracting(PassProductResponse::id, PassProductResponse::name)
                .containsExactly(
                        tuple(first.getId(), first.getName()),
                        tuple(second.getId(), second.getName())
                );
    }

    @Test
    void 판매를_중지한_수강권도_목록에_포함한다() {
        // given
        Member owner = 회원을_저장한다("inactive-pass-list-owner");
        Studio studio = 시설을_만든다(owner);
        PassProduct passProduct = passProductRepository.saveAndFlush(PassProductFixture.수업_종류를_지정한_수강권(studio, 시설의_수업_종류(studio)));
        passProductService.update(
                owner.getId(), studio.getId(), passProduct.getId(),
                PassProductFixture.수강권_수정_요청(
                        "중지된 수강권", ClassKind.GROUP,
                        시설의_수업_종류(studio).stream().map(ClassType::getId).toList(),
                        20, 3, PassProductPeriodUnit.MONTH, 0, false));
        entityManager.flush();
        entityManager.clear();

        // when
        List<PassProductResponse> responses = passProductService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(responses)
                .extracting(PassProductResponse::name, PassProductResponse::active)
                .containsExactly(tuple("중지된 수강권", false));
    }

    @Test
    void 목록_조회는_각_수강권의_수업_종류를_함께_반환한다() {
        // given
        Member owner = 회원을_저장한다("pass-list-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        List<ClassType> classTypes = 시설의_수업_종류(studio);
        passProductService.save(owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(classTypes.getFirst().getId())));
        passProductService.save(owner.getId(), studio.getId(),
                PassProductFixture.수강권_생성_요청(
                        "두 종류 수강권", ClassKind.GROUP,
                        classTypes.stream().map(ClassType::getId).toList(),
                        10, 1, PassProductPeriodUnit.MONTH, 0));
        entityManager.flush();
        entityManager.clear();

        // when
        List<PassProductResponse> responses = passProductService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.getFirst().classTypes())
                .extracting(ClassTypeResponse::name)
                .containsExactly(classTypes.getFirst().getName());
        assertThat(responses.getLast().classTypes())
                .extracting(ClassTypeResponse::name)
                .containsExactlyElementsOf(classTypes.stream().map(ClassType::getName).toList());
    }

    @Test
    void 수업_종류가_여럿인_수강권도_목록에_한_번만_나오고_아이디_오름차순으로_묶인다() {
        // given
        Member owner = 회원을_저장한다("multi-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        List<ClassType> classTypes = 시설의_수업_종류(studio);
        List<Long> classTypeIds = classTypes.stream()
                .map(ClassType::getId)
                .toList();
        passProductService.save(owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(classTypeIds));
        entityManager.flush();
        entityManager.clear();

        // when
        List<PassProductResponse> responses = passProductService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(classTypeIds).hasSizeGreaterThan(1);
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().classTypes())
                .extracting(ClassTypeResponse::id)
                .containsExactlyElementsOf(classTypeIds);
    }

    @Test
    void 수강권이_없는_시설을_조회하면_빈_목록을_반환한다() {
        // given
        Member owner = 회원을_저장한다("empty-pass-owner");
        Studio studio = 시설을_만든다(owner);

        // when
        List<PassProductResponse> responses = passProductService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 관리_권한이_없는_일반_강사는_수강권_목록을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("denied-pass-list-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(
                studio, "denied-pass-list-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);

        // when / then
        assertThatThrownBy(() -> passProductService.findAll(instructor.getId(), studio.getId()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
    }

    @Test
    void 대표_강사가_수강권을_수정하면_모든_필드가_교체된다() {
        // given
        Member owner = 회원을_저장한다("pass-update-owner");
        Studio studio = 시설을_만든다(owner);
        PassProduct passProduct = passProductRepository.saveAndFlush(PassProductFixture.수업_종류를_지정한_수강권(studio, 시설의_수업_종류(studio)));

        // when
        PassProductResponse response = passProductService.update(
                owner.getId(), studio.getId(), passProduct.getId(),
                PassProductFixture.수강권_수정_요청(
                        "6개월 그룹 30회권", ClassKind.GROUP,
                        시설의_수업_종류(studio).stream().map(ClassType::getId).toList(),
                        30, 6, PassProductPeriodUnit.MONTH, 14, true));
        entityManager.flush();
        entityManager.clear();

        // then
        PassProduct updated = passProductRepository.findById(passProduct.getId()).orElseThrow();
        assertThat(response.id()).isEqualTo(passProduct.getId());
        assertThat(response.name()).isEqualTo("6개월 그룹 30회권");
        assertThat(updated.getTotalCount()).isEqualTo(30);
        assertThat(updated.getValidPeriodAmount()).isEqualTo(6);
        assertThat(updated.getTotalHoldDays()).isEqualTo(14);
    }

    @Test
    void 수강권을_수정하면_수업_종류_연결이_통째로_교체된다() {
        // given
        Member owner = 회원을_저장한다("pass-update-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        List<ClassType> classTypes = 시설의_수업_종류(studio);
        PassProductResponse created = passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(classTypes.getFirst().getId())));
        entityManager.flush();

        // when
        PassProductResponse response = passProductService.update(
                owner.getId(), studio.getId(), created.id(),
                PassProductFixture.수강권_수정_요청(
                        "교체된 수강권", ClassKind.GROUP, List.of(classTypes.getLast().getId()),
                        20, 3, PassProductPeriodUnit.MONTH, 0, true));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(response.classTypes())
                .extracting(ClassTypeResponse::id)
                .containsExactly(classTypes.getLast().getId());
        assertThat(passProductClassTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void 수강권_수정으로_수업_종류를_비울_수_없다() {
        // given
        Member owner = 회원을_저장한다("pass-clear-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        List<ClassType> classTypes = 시설의_수업_종류(studio);
        PassProductResponse created = passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(classTypes.getFirst().getId())));
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> passProductService.update(
                owner.getId(), studio.getId(), created.id(),
                PassProductFixture.수강권_수정_요청(
                        "전체 사용 수강권", ClassKind.GROUP, List.of(), 20, 3, PassProductPeriodUnit.MONTH, 0, true)))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.CLASS_TYPE_REQUIRED));
        entityManager.clear();
        assertThat(passProductClassTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void 같은_수업_종류로_다시_수정해도_연결이_유지된다() {
        // given
        Member owner = 회원을_저장한다("pass-same-class-type-owner");
        Studio studio = 시설을_만든다(owner);
        Long classTypeId = 시설의_수업_종류(studio).getFirst().getId();
        PassProductResponse created = passProductService.save(
                owner.getId(), studio.getId(),
                PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of(classTypeId)));
        entityManager.flush();

        // when
        PassProductResponse response = passProductService.update(
                owner.getId(), studio.getId(), created.id(),
                PassProductFixture.수강권_수정_요청(
                        "같은 수업 종류 수강권", ClassKind.GROUP, List.of(classTypeId),
                        20, 3, PassProductPeriodUnit.MONTH, 0, true));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(response.classTypes())
                .extracting(ClassTypeResponse::id)
                .containsExactly(classTypeId);
        assertThat(passProductClassTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void 기존_시설에_없는_수강권을_수정하면_PASS_PRODUCT_008이_발생한다() {
        // given
        Member owner = 회원을_저장한다("missing-pass-update-owner");
        Studio studio = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> passProductService.update(
                owner.getId(), studio.getId(), 999L, PassProductFixture.기본_수강권_수정_요청()))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.PASS_PRODUCT_NOT_FOUND));
    }

    @Test
    void 다른_시설의_수강권을_수정하면_PASS_PRODUCT_008이_발생하고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("cross-studio-pass-update-owner");
        Studio requestedStudio = 시설을_만든다(owner);
        Studio owningStudio = 시설을_만든다(owner);
        PassProduct passProduct = passProductRepository.saveAndFlush(
                PassProductFixture.이름과_수업_종류를_지정한_수강권(owningStudio, "다른 시설 수강권", 시설의_수업_종류(owningStudio)));

        // when / then
        assertThatThrownBy(() -> passProductService.update(
                owner.getId(), requestedStudio.getId(), passProduct.getId(),
                PassProductFixture.기본_수강권_수정_요청()))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.PASS_PRODUCT_NOT_FOUND));
        assertThat(passProductRepository.findById(passProduct.getId()).orElseThrow().getName())
                .isEqualTo("다른 시설 수강권");
    }

    @Test
    void 일반_강사는_수강권을_수정할_수_없고_행이_유지된다() {
        // given
        Member owner = 회원을_저장한다("pass-update-permission-owner");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(
                studio, "pass-update-instructor", SystemRole.INSTRUCTOR, MembershipStatus.ACTIVE);
        PassProduct passProduct = passProductRepository.saveAndFlush(PassProductFixture.수업_종류를_지정한_수강권(studio, 시설의_수업_종류(studio)));

        // when / then
        assertThatThrownBy(() -> passProductService.update(
                instructor.getId(), studio.getId(), passProduct.getId(),
                PassProductFixture.기본_수강권_수정_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThat(passProductRepository.findById(passProduct.getId()).orElseThrow().getName())
                .isEqualTo(PassProductFixture.기본_이름);
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

    private List<ClassType> 시설의_수업_종류(Studio studio) {
        return classTypeRepository.findAllByStudioIdOrderByIdAsc(studio.getId());
    }

    private StudioRole 역할을_찾는다(Studio studio, SystemRole systemRole) {
        return studioRoleRepository.findAllByStudioId(studio.getId()).stream()
                .filter(role -> role.getSystemRole() == systemRole)
                .findFirst()
                .orElseThrow();
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
