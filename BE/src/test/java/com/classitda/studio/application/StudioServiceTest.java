package com.classitda.studio.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.ClassTypeService;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.member.domain.Member;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.support.ImageTestConfiguration;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ClassTypeService.class, StudioService.class, StudioPermissionService.class})
@MySqlRepositoryTest
class StudioServiceTest {

    private final StudioService studioService;
    private final StudioRepository studioRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;

    @Autowired
    StudioServiceTest(
            StudioService studioService,
            StudioRepository studioRepository,
            StudioRoleRepository studioRoleRepository,
            StudioMembershipRepository studioMembershipRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager
    ) {
        this.studioService = studioService;
        this.studioRepository = studioRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.studioMembershipRepository = studioMembershipRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 시설을_생성하면_시설_정보를_반환한다() {
        // given
        Member owner = 소유자를_저장한다();
        StudioCreateRequest request = StudioFixture.기본_시설_생성_요청();

        // when
        StudioResponse response = studioService.save(owner.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.address().zonecode()).isEqualTo(request.address().zonecode());
        assertThat(response.address().roadAddress()).isEqualTo(request.address().roadAddress());
        assertThat(response.address().detailAddress()).isEqualTo(request.address().detailAddress());
        assertThat(response.openTime()).isEqualTo(request.openTime());
    }

    @Test
    void 시설을_생성하면_시설이_저장된다() {
        // given
        Member owner = 소유자를_저장한다();

        // when
        StudioResponse response = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // then
        assertThat(studioRepository.findById(response.id())).isPresent();
    }

    @Test
    void 시설을_생성하면_시스템_기본_역할_세_개가_함께_저장된다() {
        // given
        Member owner = 소유자를_저장한다();

        // when
        StudioResponse response = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // then
        List<String> roleNames = studioRoleRepository.findAllByStudioId(response.id()).stream()
                .map(StudioRole::getName)
                .toList();
        assertThat(roleNames).containsExactlyInAnyOrder(
                SystemRole.OWNER.getRoleName(),
                SystemRole.INSTRUCTOR.getRoleName(),
                SystemRole.STUDENT.getRoleName()
        );
    }

    @Test
    void 시설을_생성하면_생성자가_대표_강사_멤버십으로_저장된다() {
        // given
        Member owner = 소유자를_저장한다();

        // when
        StudioResponse response = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // then
        Optional<StudioMembership> membership =
                studioMembershipRepository.findByStudioIdAndMemberId(response.id(), owner.getId());
        assertThat(membership).isPresent();
        assertThat(membership.get().getStudioRole().getName()).isEqualTo(SystemRole.OWNER.getRoleName());
        assertThat(membership.get().isInstructor()).isTrue();
        assertThat(membership.get().getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    void 시설을_생성하면_기본_수업_종류_두_개가_저장된다() {
        // given
        Member owner = 소유자를_저장한다();

        // when
        StudioResponse response = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // then
        assertThat(classTypeRepository.findAllByStudioIdOrderByIdAsc(response.id()))
                .extracting(ClassType::getName)
                .containsExactlyInAnyOrder("요가", "필라테스");
    }

    @Test
    void 시설을_조회하면_시설_정보를_반환한다() {
        // given
        Member owner = 소유자를_저장한다();
        StudioResponse created = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // when
        StudioResponse response = studioService.findById(created.id());

        // then
        assertThat(response.id()).isEqualTo(created.id());
        assertThat(response.name()).isEqualTo(created.name());
    }

    @Test
    void 없는_시설을_조회하면_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> studioService.findById(999L))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void 대표_강사는_시설_정보를_수정할_수_있다() {
        // given
        Member owner = 소유자를_저장한다();
        StudioResponse created = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // when
        StudioResponse response = studioService.update(
                owner.getId(), created.id(), StudioFixture.이름만_바꾸는_수정_요청("바뀐 스튜디오"));

        // then
        assertThat(response.name()).isEqualTo("바뀐 스튜디오");
    }

    @Test
    void 수정_요청에_없는_필드는_기존_값을_유지한다() {
        // given
        Member owner = 소유자를_저장한다();
        StudioResponse created = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // when
        StudioResponse response = studioService.update(
                owner.getId(), created.id(), StudioFixture.이름만_바꾸는_수정_요청("바뀐 스튜디오"));

        // then
        assertThat(response.address()).isEqualTo(created.address());
        assertThat(response.openTime()).isEqualTo(created.openTime());
    }

    @Test
    void 소속이_아니면_시설을_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Member other = StudioFixture.아이디가_다른_소유자("other");
        entityManager.persist(other);
        StudioResponse created = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> studioService.update(
                other.getId(), created.id(), StudioFixture.이름만_바꾸는_수정_요청("남의 스튜디오")))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 없는_시설을_수정하면_예외가_발생한다() {
        // given
        Member owner = 소유자를_저장한다();

        // when / then
        assertThatThrownBy(() -> studioService.update(
                owner.getId(), 999L, StudioFixture.이름만_바꾸는_수정_요청("바뀐 스튜디오")))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void 내가_속한_시설을_아이디_오름차순으로_조회한다() {
        // given
        Member owner = 소유자를_저장한다();
        StudioResponse first = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        StudioResponse second = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        Member other = StudioFixture.아이디가_다른_소유자("other-studio-owner");
        entityManager.persist(other);
        entityManager.flush();
        studioService.save(other.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();
        entityManager.clear();

        // when
        List<StudioResponse> responses = studioService.findAllByMemberId(owner.getId());

        // then
        assertThat(responses)
                .extracting(StudioResponse::id)
                .containsExactly(first.id(), second.id());
    }

    @Test
    void 속한_시설이_없으면_빈_목록을_반환한다() {
        // given
        Member member = StudioFixture.아이디가_다른_소유자("no-studio-member");
        entityManager.persist(member);
        entityManager.flush();

        // when
        List<StudioResponse> responses = studioService.findAllByMemberId(member.getId());

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 시설을_등록할_때_대표_이미지도_함께_저장한다() {
        // given
        Member owner = 소유자를_저장한다();
        String objectKey = "studio-images/first.jpg";

        // when
        StudioResponse response = studioService.save(
                owner.getId(), StudioFixture.이미지가_있는_시설_생성_요청(objectKey));

        // then
        assertThat(response.image())
                .isEqualTo(ImageTestConfiguration.BASE_URL + "/" + objectKey);
        assertThat(studioService.findById(response.id()).image())
                .isEqualTo(ImageTestConfiguration.BASE_URL + "/" + objectKey);
    }

    @Test
    void 이미지_없이도_시설을_등록할_수_있다() {
        // given
        Member owner = 소유자를_저장한다();

        // when
        StudioResponse response = studioService.save(
                owner.getId(), StudioFixture.기본_시설_생성_요청());

        // then
        assertThat(response.image()).isNull();
    }

    @Test
    void 대표_이미지를_공개_URL로_반환한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        String objectKey = "studio-images/a.jpg";

        // when
        studioService.update(owner.getId(), studioId, StudioFixture.이미지만_바꾸는_수정_요청(objectKey));

        // then
        assertThat(studioService.findById(studioId).image())
                .isEqualTo(ImageTestConfiguration.BASE_URL + "/" + objectKey);
    }

    @Test
    void 대표_이미지는_새_이미지로_교체된다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        studioService.update(owner.getId(), studioId,
                StudioFixture.이미지만_바꾸는_수정_요청("studio-images/a.jpg"));

        // when
        studioService.update(owner.getId(), studioId,
                StudioFixture.이미지만_바꾸는_수정_요청("studio-images/b.jpg"));

        // then
        assertThat(studioService.findById(studioId).image())
                .isEqualTo(ImageTestConfiguration.BASE_URL + "/studio-images/b.jpg");
    }

    @Test
    void 이미지를_보내지_않으면_기존_이미지를_유지한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        studioService.update(owner.getId(), studioId,
                StudioFixture.이미지만_바꾸는_수정_요청("studio-images/a.jpg"));

        // when
        studioService.update(owner.getId(), studioId, StudioFixture.이름만_바꾸는_수정_요청("바뀐 이름"));

        // then
        assertThat(studioService.findById(studioId).image())
                .isEqualTo(ImageTestConfiguration.BASE_URL + "/studio-images/a.jpg");
    }

    @Test
    void 업로드_네임스페이스_밖의_키는_저장할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        String outsideKey = "studios/1/images/a.jpg";

        // when / then
        assertThatThrownBy(() -> studioService.update(
                owner.getId(), studioId, StudioFixture.이미지만_바꾸는_수정_요청(outsideKey)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.INVALID_IMAGE_OBJECT_KEY.getMessage());
    }

    @Test
    void 상위_경로를_노리는_키는_저장할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        String traversalKey = "studio-images/../notice-images/a.jpg";

        // when / then
        assertThatThrownBy(() -> studioService.update(
                owner.getId(), studioId, StudioFixture.이미지만_바꾸는_수정_요청(traversalKey)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.INVALID_IMAGE_OBJECT_KEY.getMessage());
    }

    @Test
    void 다른_시설이_쓰는_이미지로는_시설을_등록할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        String shared = "studio-images/shared-on-create.jpg";
        studioService.save(owner.getId(), StudioFixture.이미지가_있는_시설_생성_요청(shared));

        // when / then
        assertThatThrownBy(() -> studioService.save(
                owner.getId(), StudioFixture.이미지가_있는_시설_생성_요청(shared)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.IMAGE_ALREADY_USED.getMessage());
    }

    @Test
    void 같은_이미지를_두_시설에_붙일_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long firstId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        Long secondId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        String shared = "studio-images/shared.jpg";
        studioService.update(owner.getId(), firstId, StudioFixture.이미지만_바꾸는_수정_요청(shared));

        // when / then
        assertThatThrownBy(() -> studioService.update(
                owner.getId(), secondId, StudioFixture.이미지만_바꾸는_수정_요청(shared)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.IMAGE_ALREADY_USED.getMessage());
    }

    private Member 소유자를_저장한다() {
        Member owner = StudioFixture.기본_소유자();
        entityManager.persist(owner);
        entityManager.flush();
        return owner;
    }
}
