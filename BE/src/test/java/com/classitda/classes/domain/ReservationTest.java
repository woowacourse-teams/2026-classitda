package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReservationTest {

    private static final LocalDateTime RESERVED_AT = LocalDateTime.of(2026, 8, 18, 9, 0);
    private static final LocalDateTime CANCELED_AT = LocalDateTime.of(2026, 8, 19, 10, 0);

    @Test
    void 예약을_취소하면_상태와_취소_시각을_함께_변경한다() {
        // given
        Reservation reservation = 예약(ReservationStatus.RESERVED, null);

        // when
        reservation.cancel(CANCELED_AT);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(reservation.getCanceledAt()).isEqualTo(CANCELED_AT);
    }

    @ParameterizedTest
    @EnumSource(
            value = ReservationStatus.class,
            names = {"CANCELED", "ATTENDED", "ABSENT"}
    )
    void 예약_완료가_아닌_상태에서는_취소할_수_없다(
            ReservationStatus currentStatus
    ) {
        // given
        LocalDateTime originalCanceledAt = currentStatus == ReservationStatus.CANCELED
                ? CANCELED_AT.minusHours(1)
                : null;
        Reservation reservation = 예약(currentStatus, originalCanceledAt);

        // when / then
        assertThatThrownBy(() -> reservation.cancel(CANCELED_AT))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_RESERVATION_TRANSITION));
        assertThat(reservation.getStatus()).isEqualTo(currentStatus);
        assertThat(reservation.getCanceledAt()).isEqualTo(originalCanceledAt);
    }

    @Test
    void 예약_취소_시각은_필수다() {
        // given
        Reservation reservation = 예약(ReservationStatus.RESERVED, null);

        // when / then
        assertThatThrownBy(() -> reservation.cancel(null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_CANCEL_OCCURRED_AT_REQUIRED));
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getCanceledAt()).isNull();
    }

    private Reservation 예약(
            ReservationStatus status,
            LocalDateTime canceledAt
    ) {
        return Reservation.builder()
                .status(status)
                .reservedAt(RESERVED_AT)
                .canceledAt(canceledAt)
                .build();
    }
}
