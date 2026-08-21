package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ReservationFixture;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.SystemRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void 회원으로_예약을_만들면_상태가_예약됨이_된다() {
        // given
        StudioMembership membership = 소속();

        // when
        Reservation reservation = Reservation.builder()
                .membership(membership)
                .classSession(수업())
                .reservedAt(ReservationFixture.기준_시각)
                .build();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.isGuestReservation()).isFalse();
        assertThat(reservation.getCanceledAt()).isNull();
    }

    @Test
    void 비회원으로_예약을_만들_수_있다() {
        // given
        ClassGuest classGuest = ReservationFixture.기본_비회원(시설());

        // when
        Reservation reservation = Reservation.builder()
                .classGuest(classGuest)
                .classSession(수업())
                .reservedAt(ReservationFixture.기준_시각)
                .build();

        // then
        assertThat(reservation.isGuestReservation()).isTrue();
        assertThat(reservation.getMembership()).isNull();
    }

    @Test
    void 회원과_비회원을_모두_비우면_예약할_수_없다() {
        // when / then
        assertThatThrownBy(() -> Reservation.builder()
                .classSession(수업())
                .reservedAt(ReservationFixture.기준_시각)
                .build())
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_ATTENDEE_REQUIRED));
    }

    @Test
    void 회원과_비회원을_동시에_지정하면_예약할_수_없다() {
        // given
        ClassGuest classGuest = ReservationFixture.기본_비회원(시설());

        // when / then
        assertThatThrownBy(() -> Reservation.builder()
                .membership(소속())
                .classGuest(classGuest)
                .classSession(수업())
                .reservedAt(ReservationFixture.기준_시각)
                .build())
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_ATTENDEE_AMBIGUOUS));
    }

    @Test
    void 수업_없이_예약할_수_없다() {
        // when / then
        assertThatThrownBy(() -> Reservation.builder()
                .membership(소속())
                .reservedAt(ReservationFixture.기준_시각)
                .build())
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_SESSION_REQUIRED));
    }

    @Test
    void 예약을_취소하면_상태와_취소_시각이_기록된다() {
        // given
        Reservation reservation = 회원_예약();
        LocalDateTime canceledAt = ReservationFixture.기준_시각.plusHours(1);

        // when
        reservation.cancel(canceledAt);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(reservation.isCanceled()).isTrue();
        assertThat(reservation.getCanceledAt()).isEqualTo(canceledAt);
    }

    @Test
    void 이미_취소된_예약은_다시_취소할_수_없다() {
        // given
        Reservation reservation = 회원_예약();
        reservation.cancel(ReservationFixture.기준_시각);

        // when / then
        assertThatThrownBy(() -> reservation.cancel(ReservationFixture.기준_시각.plusHours(1)))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_ALREADY_CANCELED));
    }

    private Reservation 회원_예약() {
        return Reservation.builder()
                .membership(소속())
                .classSession(수업())
                .reservedAt(ReservationFixture.기준_시각)
                .build();
    }

    private ClassSession 수업() {
        return ReservationFixture.기본_수업(1L, 소속());
    }

    private StudioMembership 소속() {
        return ReservationFixture.기본_소속(시설(), SystemRole.STUDENT);
    }

    private Studio 시설() {
        return ReservationFixture.기본_시설();
    }
}
