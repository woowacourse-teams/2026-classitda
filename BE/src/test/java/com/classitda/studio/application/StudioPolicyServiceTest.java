package com.classitda.studio.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.ClassTypeService;
import com.classitda.member.domain.Member;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.fixture.StudioPolicyFixture;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.support.MySqlDataJpaTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ClassTypeService.class, StudioPolicyService.class, StudioService.class, StudioPermissionService.class})
@MySqlDataJpaTest
class StudioPolicyServiceTest {

    private final StudioPolicyService studioPolicyService;
    private final StudioService studioService;
    private final EntityManager entityManager;

    @Autowired
    StudioPolicyServiceTest(
            StudioPolicyService studioPolicyService,
            StudioService studioService,
            EntityManager entityManager
    ) {
        this.studioPolicyService = studioPolicyService;
        this.studioService = studioService;
        this.entityManager = entityManager;
    }

    @Test
    void 시설을_만들면_기본_운영_정책이_함께_저장된다() {
        // given
        Member owner = 소유자를_저장한다();

        // when
        Long studioId = 시설을_만든다(owner);

        // then
        StudioPolicyResponse response = studioPolicyService.findByStudioId(studioId);
        assertThat(response.reservationCloseMinutesBefore()).isEqualTo(30);
        assertThat(response.freeCancelMinutesBefore()).isEqualTo(720);
        assertThat(response.waitingOfferResponseMinutes()).isEqualTo(60);
        assertThat(response.maxHoldDays()).isZero();
    }

    @Test
    void 운영_정책을_조회한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        StudioPolicyResponse response = studioPolicyService.findByStudioId(studioId);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.freeCancelMinutesBefore()).isEqualTo(720);
    }

    @Test
    void 없는_시설의_운영_정책은_조회할_수_없다() {
        // given / when / then
        assertThatThrownBy(() -> studioPolicyService.findByStudioId(999L))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void 대표_강사는_운영_정책을_수정할_수_있다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        StudioPolicyResponse response = studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180));

        // then
        assertThat(response.freeCancelMinutesBefore()).isEqualTo(180);
    }

    @Test
    void 최대_홀드_일수를_수정할_수_있다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        StudioPolicyResponse response = studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.최대_홀드_일수만_바꾸는_수정_요청(7));

        // then
        assertThat(response.maxHoldDays()).isEqualTo(7);
    }

    @Test
    void 보내지_않은_필드는_기존_값을_유지한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        StudioPolicyResponse response = studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180));

        // then
        assertThat(response.reservationCloseMinutesBefore()).isEqualTo(30);
        assertThat(response.waitingOfferResponseMinutes()).isEqualTo(60);
        assertThat(response.maxHoldDays()).isZero();
    }

    @Test
    void 아무_필드도_보내지_않으면_정책이_그대로_유지된다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        StudioPolicyResponse response = studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.아무것도_바꾸지_않는_수정_요청());

        // then
        assertThat(response.reservationCloseMinutesBefore()).isEqualTo(30);
        assertThat(response.freeCancelMinutesBefore()).isEqualTo(720);
        assertThat(response.waitingOfferResponseMinutes()).isEqualTo(60);
    }

    @Test
    void 소속이_아니면_운영_정책을_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Member other = StudioFixture.아이디가_다른_소유자("other");
        entityManager.persist(other);
        entityManager.flush();
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> studioPolicyService.update(
                other.getId(), studioId, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 없는_시설의_운영_정책은_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();

        // when / then
        assertThatThrownBy(() -> studioPolicyService.update(
                owner.getId(), 999L, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_FOUND.getMessage());
    }

    private Member 소유자를_저장한다() {
        Member owner = StudioFixture.기본_소유자();
        entityManager.persist(owner);
        entityManager.flush();
        return owner;
    }

    private Long 시설을_만든다(Member owner) {
        return studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
    }
}
