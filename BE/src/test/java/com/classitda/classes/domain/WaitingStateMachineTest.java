package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class WaitingStateMachineTest {

    private static final LocalDateTime OFFERED_AT = LocalDateTime.of(2026, 8, 25, 19, 0);
    private static final LocalDateTime OFFER_EXPIRES_AT = OFFERED_AT.plusMinutes(10);
    private static final LocalDateTime CANCELED_AT = OFFERED_AT.plusMinutes(5);
    private static final LocalDateTime EXPIRED_AT = OFFER_EXPIRES_AT;
    private static final LocalDateTime ACCEPTED_AT = OFFERED_AT.plusMinutes(5);

    private final WaitingStateMachine stateMachine = new WaitingStateMachine();

    @Test
    void 제안_발행_트리거를_대기_제안_전이로_연결한다() {
        // given
        Waiting waiting = 대기();
        WaitingTrigger trigger = new WaitingTrigger.OfferIssued(
                OFFERED_AT,
                OFFER_EXPIRES_AT
        );

        // when
        stateMachine.handle(waiting, trigger);

        // then
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.OFFERED);
        assertThat(waiting.getOfferedAt()).isEqualTo(OFFERED_AT);
        assertThat(waiting.getOfferExpiresAt()).isEqualTo(OFFER_EXPIRES_AT);
    }

    @Test
    void null_트리거는_처리할_수_없다() {
        // given
        Waiting waiting = 대기();

        // when / then
        assertThatThrownBy(() -> stateMachine.handle(waiting, null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_WAITING_TRANSITION));
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.WAITING);
        assertThat(waiting.getOfferedAt()).isNull();
        assertThat(waiting.getOfferExpiresAt()).isNull();
    }

    @Test
    void 제안_발행_트리거의_제안_시각은_필수다() {
        // when / then
        assertThatThrownBy(() ->
                new WaitingTrigger.OfferIssued(null, OFFER_EXPIRES_AT))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.WAITING_OFFERED_AT_REQUIRED));
    }

    @Test
    void 제안_발행_트리거의_만료_시각은_필수다() {
        // when / then
        assertThatThrownBy(() -> new WaitingTrigger.OfferIssued(OFFERED_AT, null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.WAITING_OFFER_EXPIRES_AT_REQUIRED));
    }

    @Test
    void 취소_요청_트리거를_대기_취소_전이로_연결한다() {
        // given
        Waiting waiting = 대기();
        WaitingTrigger trigger = new WaitingTrigger.CancelRequested(CANCELED_AT);

        // when
        stateMachine.handle(waiting, trigger);

        // then
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.CANCELED);
        assertThat(waiting.getEndedAt()).isEqualTo(CANCELED_AT);
    }

    @Test
    void 취소_요청_트리거의_발생_시각은_필수다() {
        // when / then
        assertThatThrownBy(() -> new WaitingTrigger.CancelRequested(null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.WAITING_CANCEL_OCCURRED_AT_REQUIRED));
    }

    @Test
    void 만료_도달_트리거를_대기_만료_전이로_연결한다() {
        // given
        Waiting waiting = 대기();
        WaitingTrigger trigger = new WaitingTrigger.ExpirationReached(EXPIRED_AT);

        // when
        stateMachine.handle(waiting, trigger);

        // then
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.EXPIRED);
        assertThat(waiting.getEndedAt()).isEqualTo(EXPIRED_AT);
    }

    @Test
    void 만료_도달_트리거의_발생_시각은_필수다() {
        // when / then
        assertThatThrownBy(() -> new WaitingTrigger.ExpirationReached(null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.WAITING_EXPIRATION_OCCURRED_AT_REQUIRED));
    }

    @Test
    void 제안_수락_트리거를_대기_수락_전이로_연결한다() {
        // given
        Waiting waiting = 제안_중인_대기();
        WaitingTrigger trigger = new WaitingTrigger.OfferAccepted(ACCEPTED_AT);

        // when
        stateMachine.handle(waiting, trigger);

        // then
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.ACCEPTED);
        assertThat(waiting.getEndedAt()).isEqualTo(ACCEPTED_AT);
    }

    @Test
    void 제안_수락_트리거의_발생_시각은_필수다() {
        // when / then
        assertThatThrownBy(() -> new WaitingTrigger.OfferAccepted(null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.WAITING_ACCEPTANCE_OCCURRED_AT_REQUIRED));
    }

    private Waiting 대기() {
        return Waiting.builder()
                .status(WaitingStatus.WAITING)
                .sequence(1)
                .build();
    }

    private Waiting 제안_중인_대기() {
        return Waiting.builder()
                .status(WaitingStatus.OFFERED)
                .sequence(1)
                .offeredAt(OFFERED_AT)
                .offerExpiresAt(OFFER_EXPIRES_AT)
                .build();
    }
}
