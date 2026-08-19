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

    private final StudentBookingDecisionPolicy decisionPolicy = new StudentBookingDecisionPolicy();
    private final StudentBookingStatusResolver resolver = new StudentBookingStatusResolver(decisionPolicy);

    @ParameterizedTest(name = "{0} 상태를 참여 관계와 예약 가능 여부로 결정한다")
    @MethodSource("bookingStatusContexts")
    void 회원_수업_상태를_참여_관계와_예약_가능_여부로_결정한다(
            StudentBookingStatus expectedStatus,
            StudentParticipation expectedParticipation,
            BookingAvailability expectedAvailability,
            StudentBookingContext context
    ) {
        StudentBookingDecision decision = decisionPolicy.decide(context);

        assertThat(decision.participation()).isEqualTo(expectedParticipation);
        assertThat(decision.availability()).isEqualTo(expectedAvailability);
        assertThat(resolver.resolve(context)).isEqualTo(expectedStatus);
    }

    private static Stream<Arguments> bookingStatusContexts() {
        return Stream.of(
                Arguments.of(
                        StudentBookingStatus.ABSENT, StudentParticipation.ABSENT, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                0, 1, 1, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.ATTENDED, StudentParticipation.ATTENDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                0, 1, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.ATTENDED, StudentParticipation.ATTENDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                1, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.ATTENDED, StudentParticipation.ATTENDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW,
                                1, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.RESERVED, StudentParticipation.RESERVED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                1, 0, 0, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.OFFERED, StudentParticipation.OFFERED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                0, 0, 0, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.WAITING, StudentParticipation.WAITING, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                0, 0, 0, 0, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.CLOSED, StudentParticipation.NONE, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusMinutes(30),
                                0, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.AVAILABLE, StudentParticipation.NONE, BookingAvailability.RESERVABLE,
                        context(BookingWindow.OPEN, NOW.plusHours(1),
                                0, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.WAITING_AVAILABLE, StudentParticipation.NONE, BookingAvailability.WAITLISTABLE,
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
