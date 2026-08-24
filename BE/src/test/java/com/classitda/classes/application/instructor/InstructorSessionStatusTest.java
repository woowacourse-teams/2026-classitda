package com.classitda.classes.application.instructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.classitda.classes.domain.session.BookingWindow;
import com.classitda.classes.domain.session.SessionPhase;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InstructorSessionStatusTest {

    @ParameterizedTest
    @MethodSource("강사용_수업_상태")
    void 수업_단계와_예약창을_강사용_화면_상태로_변환한다(
            SessionPhase phase,
            BookingWindow bookingWindow,
            InstructorSessionStatus expectedStatus
    ) {
        // when
        InstructorSessionStatus status = InstructorSessionStatus.from(phase, bookingWindow);

        // then
        assertThat(status).isEqualTo(expectedStatus);
    }

    private static Stream<Arguments> 강사용_수업_상태() {
        return Stream.of(
                arguments(SessionPhase.SCHEDULED, BookingWindow.OPEN, InstructorSessionStatus.SCHEDULED_BOOKING_OPEN),
                arguments(SessionPhase.SCHEDULED, BookingWindow.CLOSED, InstructorSessionStatus.SCHEDULED_BOOKING_CLOSED),
                arguments(SessionPhase.IN_PROGRESS, BookingWindow.OPEN, InstructorSessionStatus.IN_PROGRESS),
                arguments(SessionPhase.IN_PROGRESS, BookingWindow.CLOSED, InstructorSessionStatus.IN_PROGRESS),
                arguments(SessionPhase.COMPLETED, BookingWindow.OPEN, InstructorSessionStatus.COMPLETED),
                arguments(SessionPhase.COMPLETED, BookingWindow.CLOSED, InstructorSessionStatus.COMPLETED),
                arguments(SessionPhase.CANCELED, BookingWindow.OPEN, InstructorSessionStatus.CANCELED),
                arguments(SessionPhase.CANCELED, BookingWindow.CLOSED, InstructorSessionStatus.CANCELED)
        );
    }
}
