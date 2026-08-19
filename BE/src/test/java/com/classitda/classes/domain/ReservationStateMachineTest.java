package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ReservationStateMachineTest {

    private static final LocalDateTime RESERVED_AT = LocalDateTime.of(2026, 8, 18, 9, 0);
    private static final LocalDateTime CANCELED_AT = LocalDateTime.of(2026, 8, 19, 10, 0);

    private final ReservationStateMachine stateMachine = new ReservationStateMachine();

    @Test
    void 예약_취소_트리거를_취소_전이로_연결한다() {
        // given
        Reservation reservation = 예약();
        ReservationTrigger trigger = new ReservationTrigger.CancelRequested(CANCELED_AT);

        // when
        stateMachine.handle(reservation, trigger);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(reservation.getCanceledAt()).isEqualTo(CANCELED_AT);
    }

    @Test
    void null_트리거는_처리할_수_없다() {
        // given
        Reservation reservation = 예약();

        // when / then
        assertThatThrownBy(() -> stateMachine.handle(reservation, null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_RESERVATION_TRANSITION));
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getCanceledAt()).isNull();
    }

    @Test
    void 예약_취소_트리거의_발생_시각은_필수다() {
        // when / then
        assertThatThrownBy(() -> new ReservationTrigger.CancelRequested(null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_CANCEL_OCCURRED_AT_REQUIRED));
    }

    private Reservation 예약() {
        return Reservation.builder()
                .status(ReservationStatus.RESERVED)
                .reservedAt(RESERVED_AT)
                .build();
    }
}
