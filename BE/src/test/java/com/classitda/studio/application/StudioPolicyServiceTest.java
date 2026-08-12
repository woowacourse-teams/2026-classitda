package com.classitda.studio.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.member.domain.Member;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.fixture.StudioPolicyFixture;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.support.MySqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Import(MySqlTestContainerConfiguration.class)
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
class StudioPolicyServiceTest {

    @Autowired
    private StudioPolicyService studioPolicyService;

    @Autowired
    private StudioService studioService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 운영_정책을_등록하면_정책_정보를_반환한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        StudioPolicyResponse response = studioPolicyService.save(
                owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청());

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.reservationCloseMinutesBefore())
                .isEqualTo(StudioPolicyFixture.DEFAULT_RESERVATION_CLOSE_MINUTES);
        assertThat(response.freeCancelMinutesBefore())
                .isEqualTo(StudioPolicyFixture.DEFAULT_FREE_CANCEL_MINUTES);
        assertThat(response.waitingOfferResponseMinutes())
                .isEqualTo(StudioPolicyFixture.DEFAULT_WAITING_RESPONSE_MINUTES);
    }

    @Test
    void 한_시설에_운영_정책을_두_번_등록할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        studioPolicyService.save(owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청());
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> studioPolicyService.save(
                owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.POLICY_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 소속이_아니면_운영_정책을_등록할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Member other = StudioFixture.아이디가_다른_소유자("other");
        entityManager.persist(other);
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> studioPolicyService.save(
                other.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 없는_시설에는_운영_정책을_등록할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();

        // when / then
        assertThatThrownBy(() -> studioPolicyService.save(
                owner.getId(), 999L, StudioPolicyFixture.기본_정책_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void 운영_정책을_조회한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        StudioPolicyResponse created = studioPolicyService.save(
                owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청());
        entityManager.flush();

        // when
        StudioPolicyResponse response = studioPolicyService.findByStudioId(studioId);

        // then
        assertThat(response.id()).isEqualTo(created.id());
        assertThat(response.freeCancelMinutesBefore())
                .isEqualTo(StudioPolicyFixture.DEFAULT_FREE_CANCEL_MINUTES);
    }

    @Test
    void 정책이_없는_시설을_조회하면_예외가_발생한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> studioPolicyService.findByStudioId(studioId))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.POLICY_NOT_FOUND.getMessage());
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
        studioPolicyService.save(owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청());
        entityManager.flush();

        // when
        StudioPolicyResponse response = studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180));

        // then
        assertThat(response.freeCancelMinutesBefore()).isEqualTo(180);
    }

    @Test
    void 보내지_않은_필드는_기존_값을_유지한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        studioPolicyService.save(owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청());
        entityManager.flush();

        // when
        StudioPolicyResponse response = studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180));

        // then
        assertThat(response.reservationCloseMinutesBefore())
                .isEqualTo(StudioPolicyFixture.DEFAULT_RESERVATION_CLOSE_MINUTES);
        assertThat(response.waitingOfferResponseMinutes())
                .isEqualTo(StudioPolicyFixture.DEFAULT_WAITING_RESPONSE_MINUTES);
    }

    @Test
    void 아무_필드도_보내지_않으면_정책이_그대로_유지된다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        studioPolicyService.save(owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청());
        entityManager.flush();

        // when
        StudioPolicyResponse response = studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.아무것도_바꾸지_않는_수정_요청());

        // then
        assertThat(response.reservationCloseMinutesBefore())
                .isEqualTo(StudioPolicyFixture.DEFAULT_RESERVATION_CLOSE_MINUTES);
        assertThat(response.freeCancelMinutesBefore())
                .isEqualTo(StudioPolicyFixture.DEFAULT_FREE_CANCEL_MINUTES);
        assertThat(response.waitingOfferResponseMinutes())
                .isEqualTo(StudioPolicyFixture.DEFAULT_WAITING_RESPONSE_MINUTES);
    }

    @Test
    void 소속이_아니면_운영_정책을_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Member other = StudioFixture.아이디가_다른_소유자("other");
        entityManager.persist(other);
        Long studioId = 시설을_만든다(owner);
        studioPolicyService.save(owner.getId(), studioId, StudioPolicyFixture.기본_정책_생성_요청());
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> studioPolicyService.update(
                other.getId(), studioId, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 정책이_없으면_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> studioPolicyService.update(
                owner.getId(), studioId, StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180)))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.POLICY_NOT_FOUND.getMessage());
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
