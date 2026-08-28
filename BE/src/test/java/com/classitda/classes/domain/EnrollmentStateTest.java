package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.classes.domain.enrollment.EnrollmentState;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EnrollmentStateTest {

    private static final LocalDateTime WAITED_AT =
            LocalDateTime.of(2026, 8, 20, 9, 0);
    private static final LocalDateTime OFFERED_AT = WAITED_AT.plusHours(1);
    private static final LocalDateTime OFFER_EXPIRES_AT = OFFERED_AT.plusMinutes(10);

    @Test
    void 같은_상태와_시각은_같은_값이다() {
        EnrollmentState first = EnrollmentState.waiting(WAITED_AT);
        EnrollmentState second = EnrollmentState.waiting(WAITED_AT);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    void 상태_전이는_기존_값을_바꾸지_않고_새_값을_반환한다() {
        EnrollmentState waiting = EnrollmentState.waiting(WAITED_AT);

        EnrollmentState offered = waiting.offer(OFFERED_AT, OFFER_EXPIRES_AT);

        assertThat(waiting.getStatus()).isEqualTo(EnrollmentStatus.WAITING);
        assertThat(waiting.getStatusChangedAt()).isEqualTo(WAITED_AT);
        assertThat(waiting.getOfferExpiresAt()).isNull();
        assertThat(offered.getStatus()).isEqualTo(EnrollmentStatus.OFFERED);
        assertThat(offered.getStatusChangedAt()).isEqualTo(OFFERED_AT);
        assertThat(offered.getOfferExpiresAt()).isEqualTo(OFFER_EXPIRES_AT);
    }
}
