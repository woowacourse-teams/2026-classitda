package com.classitda.studio.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.classes.application.ClassTypeService;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.common.config.TimeConfig;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.member.domain.Member;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.passproduct.domain.MemberPassProductStatus;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Permission;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.StudioRolePermission;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.PermissionRepository;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.fixture.StudioMembershipFixture;
import com.classitda.studio.presentation.dto.StudioMembershipUpdateRequest;
import com.classitda.studio.presentation.dto.StudioMembershipResponse;
import com.classitda.support.MySqlDataJpaTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
        StudioMembershipService.class,
        ClassTypeService.class,
        StudioService.class,
        StudioPermissionService.class,
        StudioPolicyService.class,
        StudioMembershipTerminationService.class,
        TimeConfig.class})
@MySqlDataJpaTest
class StudioMembershipServiceTest {

    private final StudioMembershipService studioMembershipService;
    private final StudioService studioService;
    private final StudioRepository studioRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final EntityManager entityManager;

    @Autowired
    StudioMembershipServiceTest(
            StudioMembershipService studioMembershipService,
            StudioService studioService,
            StudioRepository studioRepository,
            StudioRoleRepository studioRoleRepository,
            StudioMembershipRepository studioMembershipRepository,
            StudioRolePermissionRepository studioRolePermissionRepository,
            PermissionRepository permissionRepository,
            MemberRepository memberRepository,
            AuthAccountRepository authAccountRepository,
            EntityManager entityManager
    ) {
        this.studioMembershipService = studioMembershipService;
        this.studioService = studioService;
        this.studioRepository = studioRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.studioMembershipRepository = studioMembershipRepository;
        this.studioRolePermissionRepository = studioRolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.memberRepository = memberRepository;
        this.authAccountRepository = authAccountRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 가입하지_않은_사람을_등록하면_회원을_만들지_않고_소속만_만든다() {
        // given
        Member owner = 회원을_저장한다("membership-owner", "01011110001");
        Studio studio = 시설을_만든다(owner);
        // when
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(memberRepository.findByPhoneNumber(StudioMembershipFixture.기본_전화번호)).isEmpty();
        StudioMembership studioMembership = 소속을_찾는다(studio, StudioMembershipFixture.기본_전화번호);
        assertThat(studioMembership.getName()).isEqualTo(StudioMembershipFixture.기본_이름);
        assertThat(studioMembership.getPhoneNumber()).isEqualTo(StudioMembershipFixture.기본_전화번호);
        assertThat(studioMembership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(studioMembership.isInstructor()).isFalse();
        assertThat(studioMembership.isRegistered()).isFalse();
    }

    @Test
    void 이미_가입한_사람을_등록하면_소속에_회원이_연결된다() {
        // given
        Member owner = 회원을_저장한다("registered-owner", "01011110002");
        Studio studio = 시설을_만든다(owner);
        Member registered = 가입한_회원을_저장한다("registered-member", StudioMembershipFixture.기본_전화번호);
        long memberCountBefore = memberRepository.count();

        // when
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();

        // then
        assertThat(memberRepository.count()).isEqualTo(memberCountBefore);
        StudioMembership studioMembership = 소속을_찾는다(studio, StudioMembershipFixture.기본_전화번호);
        assertThat(studioMembership.isRegistered()).isTrue();
        assertThat(studioMembership.getMember().getId()).isEqualTo(registered.getId());
    }

    @Test
    void 다른_시설이_먼저_등록한_번호여도_시설마다_이름을_따로_쓴다() {
        // given
        Member owner = 회원을_저장한다("shared-owner", "01011110003");
        Studio firstStudio = 시설을_만든다(owner);
        Studio secondStudio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), firstStudio.getId(),
                StudioMembershipFixture.소속_등록_요청("첫째시설이름", StudioMembershipFixture.기본_전화번호));
        entityManager.flush();
        long memberCountBefore = memberRepository.count();

        // when
        studioMembershipService.saveStudent(owner.getId(), secondStudio.getId(),
                StudioMembershipFixture.소속_등록_요청("둘째시설이름", StudioMembershipFixture.기본_전화번호));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(memberRepository.count()).isEqualTo(memberCountBefore);
        assertThat(소속을_찾는다(secondStudio, StudioMembershipFixture.기본_전화번호).getName())
                .isEqualTo("둘째시설이름");
        assertThat(소속을_찾는다(firstStudio, StudioMembershipFixture.기본_전화번호).getName())
                .isEqualTo("첫째시설이름");
    }

    @Test
    void 같은_시설에_같은_번호를_다시_등록하면_MEMBERSHIP_004가_발생한다() {
        // given
        Member owner = 회원을_저장한다("duplicate-owner", "01011110004");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.MEMBERSHIP_ALREADY_EXISTS));
    }

    @Test
    void 강사로_등록하면_강사_역할이_붙는다() {
        // given
        Member owner = 회원을_저장한다("instructor-role-owner", "01011110005");
        Studio studio = 시설을_만든다(owner);

        // when
        studioMembershipService.saveInstructor(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        entityManager.clear();

        // then
        StudioMembership studioMembership = 소속을_찾는다(studio, StudioMembershipFixture.기본_전화번호);
        assertThat(studioMembership.isInstructor()).isTrue();
        assertThat(studioMembership.getStudioRole().getSystemRole()).isEqualTo(SystemRole.INSTRUCTOR);
    }

    @Test
    void 초대_권한만_있으면_강사를_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("invite-only-owner", "01011110008");
        Studio studio = 시설을_만든다(owner);
        Member inviter = 소속을_만든다(studio, "inviter", "01011110009", SystemRole.INSTRUCTOR);
        권한을_부여한다(studio, SystemRole.INSTRUCTOR, PermissionCode.MEMBER_INVITE);
        // when / then
        assertThatThrownBy(() -> studioMembershipService.saveInstructor(
                inviter.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThat(memberRepository.findByPhoneNumber(StudioMembershipFixture.기본_전화번호)).isEmpty();
    }

    @Test
    void 초대_권한만_있어도_회원은_등록할_수_있다() {
        // given
        Member owner = 회원을_저장한다("invite-student-owner", "01011110010");
        Studio studio = 시설을_만든다(owner);
        Member inviter = 소속을_만든다(studio, "student-inviter", "01011110011", SystemRole.INSTRUCTOR);
        권한을_부여한다(studio, SystemRole.INSTRUCTOR, PermissionCode.MEMBER_INVITE);
        // when
        studioMembershipService.saveStudent(
                inviter.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();

        // then
        assertThat(소속을_찾는다(studio, StudioMembershipFixture.기본_전화번호).isInstructor()).isFalse();
    }

    @Test
    void 권한이_없으면_소속을_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("denied-owner", "01011110012");
        Studio studio = 시설을_만든다(owner);
        Member instructor = 소속을_만든다(studio, "denied-instructor", "01011110013", SystemRole.INSTRUCTOR);
        // when / then
        assertThatThrownBy(() -> studioMembershipService.saveStudent(
                instructor.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
    }

    @Test
    void 없는_시설에는_소속을_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("missing-studio-owner", "01011110014");

        // when / then
        assertThatThrownBy(() -> studioMembershipService.saveStudent(
                owner.getId(), 999L, StudioMembershipFixture.기본_소속_등록_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_FOUND));
    }

    @Test
    void 회원_목록을_아이디_오름차순으로_조회하고_다른_시설은_제외한다() {
        // given
        Member owner = 회원을_저장한다("list-owner", "01011110015");
        Studio studio = 시설을_만든다(owner);
        Studio otherStudio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("첫째", "01022220001"));
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("둘째", "01022220002"));
        studioMembershipService.saveStudent(owner.getId(), otherStudio.getId(),
                StudioMembershipFixture.소속_등록_요청("다른시설", "01022220003"));
        entityManager.flush();
        entityManager.clear();

        // when
        CursorResponse<StudioMembershipResponse> response = studioMembershipService.findStudentsWithCursor(
                owner.getId(), studio.getId(), null, 10);

        // then
        assertThat(response.items())
                .extracting(StudioMembershipResponse::name)
                .containsExactly("첫째", "둘째");
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void 회원_목록이_커서로_끊기면_다음_커서를_반환한다() {
        // given
        Member owner = 회원을_저장한다("cursor-owner", "01011110016");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("첫째", "01033330001"));
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("둘째", "01033330002"));
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("셋째", "01033330003"));
        entityManager.flush();
        entityManager.clear();

        // when
        CursorResponse<StudioMembershipResponse> firstPage = studioMembershipService.findStudentsWithCursor(
                owner.getId(), studio.getId(), null, 2);
        CursorResponse<StudioMembershipResponse> secondPage = studioMembershipService.findStudentsWithCursor(
                owner.getId(), studio.getId(), firstPage.nextCursor(), 2);

        // then
        assertThat(firstPage.items()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isNotNull();
        assertThat(firstPage.items())
                .extracting(StudioMembershipResponse::name)
                .containsExactly("첫째", "둘째");
        assertThat(secondPage.items())
                .extracting(StudioMembershipResponse::name)
                .containsExactly("셋째");
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    void 소속_목록은_앱_가입_여부를_함께_반환한다() {
        // given
        Member owner = 회원을_저장한다("registered-list-owner", "01011110017");
        Studio studio = 시설을_만든다(owner);
        가입한_회원을_저장한다("app-user", "01044440001");
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("앱사용", "01044440001"));
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("미가입", "01044440002"));
        entityManager.flush();
        entityManager.clear();

        // when
        CursorResponse<StudioMembershipResponse> response = studioMembershipService.findStudentsWithCursor(
                owner.getId(), studio.getId(), null, 10);

        // then
        assertThat(response.items())
                .extracting(StudioMembershipResponse::name, StudioMembershipResponse::registered)
                .contains(tuple("앱사용", true), tuple("미가입", false));
    }

    @Test
    void 회원_목록과_강사_목록이_서로_섞이지_않는다() {
        // given
        Member owner = 회원을_저장한다("split-list-owner", "01011110023");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("수강생", "01055550001"));
        studioMembershipService.saveInstructor(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("강사", "01055550002"));
        entityManager.flush();
        entityManager.clear();

        // when
        CursorResponse<StudioMembershipResponse> students = studioMembershipService.findStudentsWithCursor(
                owner.getId(), studio.getId(), null, 10);
        CursorResponse<StudioMembershipResponse> instructors = studioMembershipService.findInstructorsWithCursor(
                owner.getId(), studio.getId(), null, 10);

        // then
        assertThat(students.items())
                .extracting(StudioMembershipResponse::name)
                .containsExactly("수강생");
        assertThat(instructors.items())
                .extracting(StudioMembershipResponse::name)
                .containsExactly(owner.getName(), "강사");
        assertThat(instructors.items())
                .allSatisfy(response -> assertThat(response.studioRole().instructor()).isTrue());
    }

    @Test
    void 소속_단건을_조회한다() {
        // given
        Member owner = 회원을_저장한다("find-one-owner", "01011110018");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        entityManager.clear();
        Long membershipId = 소속_아이디를_찾는다(studio, StudioMembershipFixture.기본_전화번호);

        // when
        StudioMembershipResponse response = studioMembershipService.findById(
                owner.getId(), studio.getId(), membershipId);

        // then
        assertThat(response.id()).isEqualTo(membershipId);
        assertThat(response.name()).isEqualTo(StudioMembershipFixture.기본_이름);
        assertThat(response.registered()).isFalse();
    }

    @Test
    void 다른_시설의_소속을_조회하면_MEMBERSHIP_005가_발생한다() {
        // given
        Member owner = 회원을_저장한다("cross-find-owner", "01011110019");
        Studio studio = 시설을_만든다(owner);
        Studio otherStudio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), otherStudio.getId(),
                StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(otherStudio, StudioMembershipFixture.기본_전화번호);

        // when / then
        assertThatThrownBy(() -> studioMembershipService.findById(owner.getId(), studio.getId(), membershipId))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.MEMBERSHIP_NOT_FOUND));
    }

    private Long 소속_아이디를_찾는다(Studio studio, String phoneNumber) {
        return 소속을_찾는다(studio, phoneNumber).getId();
    }

    private StudioMembership 소속을_찾는다(Studio studio, String phoneNumber) {
        return studioMembershipRepository
                .findByStudioIdAndPhoneNumber(studio.getId(), phoneNumber)
                .orElseThrow();
    }

    @Test
    void 가입하지_않은_회원은_이름과_번호를_모두_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("update-owner", "01011120001");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(studio, StudioMembershipFixture.기본_전화번호);

        // when
        studioMembershipService.update(owner.getId(), studio.getId(), membershipId,
                StudioMembershipUpdateRequest.of("바뀐이름", "01099998888"));
        entityManager.flush();
        entityManager.clear();

        // then
        StudioMembership updated = studioMembershipRepository.findById(membershipId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("바뀐이름");
        assertThat(updated.getPhoneNumber()).isEqualTo("01099998888");
    }

    @Test
    void 가입한_회원의_번호를_바꾸면_MEMBERSHIP_007이_발생한다() {
        // given
        Member owner = 회원을_저장한다("locked-owner", "01011120002");
        Studio studio = 시설을_만든다(owner);
        가입한_회원을_저장한다("locked-member", StudioMembershipFixture.기본_전화번호);
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(studio, StudioMembershipFixture.기본_전화번호);

        // when / then
        assertThatThrownBy(() -> studioMembershipService.update(owner.getId(), studio.getId(), membershipId,
                StudioMembershipUpdateRequest.of("바뀐이름", "01099998888")))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(StudioErrorCode.MEMBERSHIP_PHONE_NUMBER_NOT_EDITABLE));
    }

    @Test
    void 가입한_회원도_이름은_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("name-owner", "01011120003");
        Studio studio = 시설을_만든다(owner);
        가입한_회원을_저장한다("name-member", StudioMembershipFixture.기본_전화번호);
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(studio, StudioMembershipFixture.기본_전화번호);

        // when
        studioMembershipService.update(owner.getId(), studio.getId(), membershipId,
                StudioMembershipUpdateRequest.of("바뀐이름", StudioMembershipFixture.기본_전화번호));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(studioMembershipRepository.findById(membershipId).orElseThrow().getName())
                .isEqualTo("바뀐이름");
    }

    @Test
    void 번호를_고치면_그_번호로_가입한_회원과_연결된다() {
        // given
        Member owner = 회원을_저장한다("link-owner", "01011130001");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("오타회원", "01000000001"));
        entityManager.flush();
        Member registered = 가입한_회원을_저장한다("실제회원", "01012349999");
        Long membershipId = 소속_아이디를_찾는다(studio, "01000000001");

        // when
        studioMembershipService.update(owner.getId(), studio.getId(), membershipId,
                StudioMembershipUpdateRequest.of("실제회원", "01012349999"));
        entityManager.flush();
        entityManager.clear();

        // then
        StudioMembership updated = studioMembershipRepository.findById(membershipId).orElseThrow();
        assertThat(updated.isRegistered()).isTrue();
        assertThat(updated.getMember().getId()).isEqualTo(registered.getId());
    }

    @Test
    void 가입하지_않은_번호로_고치면_연결되지_않는다() {
        // given
        Member owner = 회원을_저장한다("nolink-owner", "01011130002");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("오타회원", "01000000002"));
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(studio, "01000000002");

        // when
        studioMembershipService.update(owner.getId(), studio.getId(), membershipId,
                StudioMembershipUpdateRequest.of("고친이름", "01000000003"));
        entityManager.flush();
        entityManager.clear();

        // then
        StudioMembership updated = studioMembershipRepository.findById(membershipId).orElseThrow();
        assertThat(updated.isRegistered()).isFalse();
        assertThat(updated.getPhoneNumber()).isEqualTo("01000000003");
    }

    @Test
    void 이력이_없는_소속을_삭제하면_기록이_남지_않는다() {
        // given
        Member owner = 회원을_저장한다("delete-owner", "01011120004");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(studio, StudioMembershipFixture.기본_전화번호);

        // when
        studioMembershipService.delete(owner.getId(), studio.getId(), membershipId);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(studioMembershipRepository.findById(membershipId)).isEmpty();
    }

    @Test
    void 삭제한_번호로_다시_등록할_수_있다() {
        // given
        Member owner = 회원을_저장한다("recreate-owner", "01011120005");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청());
        entityManager.flush();
        studioMembershipService.delete(
                owner.getId(), studio.getId(), 소속_아이디를_찾는다(studio, StudioMembershipFixture.기본_전화번호));
        entityManager.flush();

        // when
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("다시등록", StudioMembershipFixture.기본_전화번호));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(소속을_찾는다(studio, StudioMembershipFixture.기본_전화번호).getName()).isEqualTo("다시등록");
    }

    @Test
    void 삭제한_강사를_회원으로_다시_등록하면_회원_역할로_되살아난다() {
        // given
        Member owner = 회원을_저장한다("role-owner", "01011140001");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveInstructor(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("강사였던사람", "01011140002"));
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(studio, "01011140002");
        이력을_남긴다(studio, membershipId);
        studioMembershipService.delete(owner.getId(), studio.getId(), membershipId);
        entityManager.flush();

        // when
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("이제회원", "01011140002"));
        entityManager.flush();
        entityManager.clear();

        // then
        StudioMembership revived = studioMembershipRepository.findById(membershipId).orElseThrow();
        assertThat(revived.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(revived.isInstructor()).isFalse();
        assertThat(revived.getStudioRole().getSystemRole()).isEqualTo(SystemRole.STUDENT);
    }

    @Test
    void 삭제한_회원을_강사로_다시_등록하면_강사_역할로_되살아난다() {
        // given
        Member owner = 회원을_저장한다("role-owner2", "01011140003");
        Studio studio = 시설을_만든다(owner);
        studioMembershipService.saveStudent(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("회원이던사람", "01011140004"));
        entityManager.flush();
        Long membershipId = 소속_아이디를_찾는다(studio, "01011140004");
        이력을_남긴다(studio, membershipId);
        studioMembershipService.delete(owner.getId(), studio.getId(), membershipId);
        entityManager.flush();

        // when
        studioMembershipService.saveInstructor(owner.getId(), studio.getId(),
                StudioMembershipFixture.소속_등록_요청("이제강사", "01011140004"));
        entityManager.flush();
        entityManager.clear();

        // then
        StudioMembership revived = studioMembershipRepository.findById(membershipId).orElseThrow();
        assertThat(revived.getStudioRole().getSystemRole()).isEqualTo(SystemRole.INSTRUCTOR);
    }

    @Test
    void 대표_강사는_삭제할_수_없다() {
        // given
        Member owner = 회원을_저장한다("owner-delete", "01011120006");
        Studio studio = 시설을_만든다(owner);
        Long ownerMembershipId = 소속을_찾는다(studio, owner.getPhoneNumber()).getId();

        // when / then
        assertThatThrownBy(() -> studioMembershipService.delete(
                owner.getId(), studio.getId(), ownerMembershipId))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(StudioErrorCode.MEMBERSHIP_OWNER_NOT_DELETABLE));
    }

    @Test
    void 없는_소속을_삭제하면_MEMBERSHIP_005가_발생한다() {
        // given
        Member owner = 회원을_저장한다("missing-delete-owner", "01011120007");
        Studio studio = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> studioMembershipService.delete(owner.getId(), studio.getId(), 999L))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.MEMBERSHIP_NOT_FOUND));
    }

    @Test
    void 탈퇴를_요청한_회원의_번호로는_등록할_수_없다() {
        // given
        Member owner = 회원을_저장한다("withdrawing-owner", "01011120008");
        Studio studio = 시설을_만든다(owner);
        Member withdrawing = 가입한_회원을_저장한다("withdrawing", StudioMembershipFixture.기본_전화번호);
        withdrawing.withdraw(LocalDateTime.now());
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> studioMembershipService.saveStudent(
                owner.getId(), studio.getId(), StudioMembershipFixture.기본_소속_등록_요청()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(StudioErrorCode.MEMBERSHIP_WITHDRAWAL_PENDING));
    }

    private Member 회원을_저장한다(String name, String phoneNumber) {
        Member member = Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .build();
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Member 가입한_회원을_저장한다(String name, String phoneNumber) {
        Member member = 회원을_저장한다(name, phoneNumber);
        authAccountRepository.saveAndFlush(AuthAccount.builder()
                .memberId(member.getId())
                .provider(OauthProvider.GOOGLE)
                .providerSubject("subject-" + member.getId())
                .providerEmail(name + "@example.com")
                .build());
        return member;
    }

    private Studio 시설을_만든다(Member owner) {
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        entityManager.flush();
        return studioRepository.findById(studioId).orElseThrow();
    }

    private StudioRole 역할을_찾는다(Studio studio, SystemRole systemRole) {
        return studioRoleRepository.findAllByStudioId(studio.getId()).stream()
                .filter(studioRole -> studioRole.getSystemRole() == systemRole)
                .findFirst()
                .orElseThrow();
    }

    private void 권한을_부여한다(Studio studio, SystemRole systemRole, PermissionCode permissionCode) {
        StudioRole studioRole = 역할을_찾는다(studio, systemRole);
        Permission permission = permissionRepository.findByCodeIn(List.of(permissionCode)).stream()
                .findFirst()
                .orElseThrow();
        studioRolePermissionRepository.saveAndFlush(StudioRolePermission.builder()
                .studioRole(studioRole)
                .permission(permission)
                .build());
    }

    private Member 소속을_만든다(Studio studio, String name, String phoneNumber, SystemRole systemRole) {
        Member member = 회원을_저장한다(name, phoneNumber);
        entityManager.persist(StudioMembership.builder()
                .studio(studio)
                .member(member)
                .phoneNumber(member.getPhoneNumber())
                .studioRole(역할을_찾는다(studio, systemRole))
                .name(member.getName())
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build());
        entityManager.flush();
        return member;
    }
    private void 이력을_남긴다(Studio studio, Long membershipId) {
        ClassType classType = ClassType.builder()
                .studio(studio)
                .name("이력용 수업 종류")
                .build();
        entityManager.persist(classType);
        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name("10회권")
                .classForm(ClassForm.GROUP)
                .classTypes(List.of(classType))
                .totalCount(10)
                .validPeriodAmount(3)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(7)
                .build();
        entityManager.persist(passProduct);
        entityManager.persist(MemberPassProduct.builder()
                .membership(studioMembershipRepository.findById(membershipId).orElseThrow())
                .passProduct(passProduct)
                .remainingCount(10)
                .remainingHoldDays(7)
                .status(MemberPassProductStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(3))
                .build());
        entityManager.flush();
    }
}
