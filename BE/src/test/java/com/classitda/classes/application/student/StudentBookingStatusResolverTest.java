package com.classitda.classes.application.student;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.classes.application.student.StudentBookingContext.ReservationCounts;
import com.classitda.classes.application.student.StudentBookingContext.WaitingCounts;
import com.classitda.classes.domain.BookingWindow;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StudentBookingStatusResolverTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);

    private final StudentBookingStatusResolver resolver = new StudentBookingStatusResolver();

    @ParameterizedTest(name = "{0} 상태를 가장 높은 우선순위 규칙으로 결정한다")
    @MethodSource("bookingStatusContexts")
    void 예약_상태를_규칙_우선순위에_따라_결정한다(
            StudentBookingStatus expected,
            StudentBookingContext context
    ) {
        assertThat(resolver.resolve(context)).isEqualTo(expected);
    }

    private static Stream<Arguments> bookingStatusContexts() {
        return Stream.of(
                Arguments.of(
                        StudentBookingStatus.ABSENT,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                0, 1, 1, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.ATTENDED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                0, 1, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.ATTENDED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                1, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.ATTENDED,
                        context(BookingWindow.CLOSED, NOW,
                                1, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.RESERVED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                1, 0, 0, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.OFFERED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                0, 0, 0, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.WAITING,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                0, 0, 0, 0, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusMinutes(30),
                                0, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.AVAILABLE,
                        context(BookingWindow.OPEN, NOW.plusHours(1),
                                0, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.WAITING_AVAILABLE,
                        context(BookingWindow.OPEN, NOW.plusHours(1),
                                0, 0, 0, 0, 0, 0)
                )
        );
    }

    private static StudentBookingContext context(
            BookingWindow bookingWindow,
            LocalDateTime startAt,
            long ownReservedCount,
            long ownAttendedCount,
            long ownAbsentCount,
            long ownOfferedCount,
            long ownWaitingCount,
            long remainingCapacity
    ) {
        return new StudentBookingContext(
                bookingWindow,
                startAt,
                new ReservationCounts(0, ownReservedCount, ownAttendedCount, ownAbsentCount),
                new WaitingCounts(0, ownOfferedCount, ownWaitingCount),
                remainingCapacity,
                NOW
        );
    }
}
