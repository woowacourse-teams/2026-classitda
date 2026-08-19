package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class WaitingTest {

    private static final LocalDateTime OFFERED_AT = LocalDateTime.of(2026, 8, 25, 19, 0);
    private static final LocalDateTime OFFER_EXPIRES_AT = OFFERED_AT.plusMinutes(10);

    @Test
    void 대기_중인_회원에게_제안하면_상태와_제안_기한을_함께_변경한다() {
        // given
        Waiting waiting = 대기(WaitingStatus.WAITING, null, null);

        // when
        waiting.offer(OFFERED_AT, OFFER_EXPIRES_AT);

        // then
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.OFFERED);
        assertThat(waiting.getOfferedAt()).isEqualTo(OFFERED_AT);
        assertThat(waiting.getOfferExpiresAt()).isEqualTo(OFFER_EXPIRES_AT);
    }

    @ParameterizedTest
    @EnumSource(
            value = WaitingStatus.class,
            names = {"OFFERED", "ACCEPTED", "EXPIRED", "CANCELED"}
    )
    void 대기_중이_아닌_상태에는_제안할_수_없다(WaitingStatus currentStatus) {
        // given
        LocalDateTime originalOfferedAt = currentStatus == WaitingStatus.OFFERED
                ? OFFERED_AT.minusMinutes(1)
                : null;
        LocalDateTime originalOfferExpiresAt = currentStatus == WaitingStatus.OFFERED
                ? OFFER_EXPIRES_AT.minusMinutes(1)
                : null;
        Waiting waiting = 대기(
                currentStatus,
                originalOfferedAt,
                originalOfferExpiresAt
        );

        // when / then
        assertThatThrownBy(() -> waiting.offer(OFFERED_AT, OFFER_EXPIRES_AT))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_WAITING_TRANSITION));
        assertThat(waiting.getStatus()).isEqualTo(currentStatus);
        assertThat(waiting.getOfferedAt()).isEqualTo(originalOfferedAt);
        assertThat(waiting.getOfferExpiresAt()).isEqualTo(originalOfferExpiresAt);
    }

    @Test
    void 대기_제안_시각은_필수다() {
        // given
        Waiting waiting = 대기(WaitingStatus.WAITING, null, null);

        // when / then
        assertThatThrownBy(() -> waiting.offer(null, OFFER_EXPIRES_AT))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.WAITING_OFFERED_AT_REQUIRED));
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.WAITING);
        assertThat(waiting.getOfferedAt()).isNull();
        assertThat(waiting.getOfferExpiresAt()).isNull();
    }

    @Test
    void 대기_제안_만료_시각은_필수다() {
        // given
        Waiting waiting = 대기(WaitingStatus.WAITING, null, null);

        // when / then
        assertThatThrownBy(() -> waiting.offer(OFFERED_AT, null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.WAITING_OFFER_EXPIRES_AT_REQUIRED));
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.WAITING);
        assertThat(waiting.getOfferedAt()).isNull();
        assertThat(waiting.getOfferExpiresAt()).isNull();
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_제안_만료_시각")
    void 대기_제안_만료_시각은_제안_시각보다_뒤여야_한다(
            LocalDateTime invalidOfferExpiresAt
    ) {
        // given
        Waiting waiting = 대기(WaitingStatus.WAITING, null, null);

        // when / then
        assertThatThrownBy(() -> waiting.offer(OFFERED_AT, invalidOfferExpiresAt))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_WAITING_OFFER_DEADLINE));
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.WAITING);
        assertThat(waiting.getOfferedAt()).isNull();
        assertThat(waiting.getOfferExpiresAt()).isNull();
    }

    private Waiting 대기(
            WaitingStatus status,
            LocalDateTime offeredAt,
            LocalDateTime offerExpiresAt
    ) {
        return Waiting.builder()
                .status(status)
                .sequence(1)
                .offeredAt(offeredAt)
                .offerExpiresAt(offerExpiresAt)
                .build();
    }

    private static Stream<LocalDateTime> 유효하지_않은_제안_만료_시각() {
        return Stream.of(OFFERED_AT.minusNanos(1), OFFERED_AT);
    }
}
