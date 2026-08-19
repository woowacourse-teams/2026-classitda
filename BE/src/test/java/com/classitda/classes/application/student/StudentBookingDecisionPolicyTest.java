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

class StudentBookingDecisionPolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);

    private final StudentBookingDecisionPolicy decisionPolicy = new StudentBookingDecisionPolicy();

    @ParameterizedTest(name = "{0} 예약 관계와 {1} 출결 결과와 {2} 예약 가능 여부를 결정한다")
    @MethodSource("bookingDecisionContexts")
    void 회원_수업_상태를_참여_관계와_예약_가능_여부로_결정한다(
            StudentBookingRelation expectedBookingRelation,
            StudentAttendanceResult expectedAttendanceResult,
            BookingAvailability expectedAvailability,
            StudentBookingContext context
    ) {
        StudentBookingDecision decision = decisionPolicy.decide(context);

        assertThat(decision.bookingRelation()).isEqualTo(expectedBookingRelation);
        assertThat(decision.attendanceResult()).isEqualTo(expectedAttendanceResult);
        assertThat(decision.availability()).isEqualTo(expectedAvailability);
    }

    private static Stream<Arguments> bookingDecisionContexts() {
        return Stream.of(
                Arguments.of(
                        StudentBookingRelation.NONE, StudentAttendanceResult.ABSENT, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                0, 1, 1, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, StudentAttendanceResult.ATTENDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                0, 1, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, StudentAttendanceResult.ATTENDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.minusHours(2),
                                1, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, StudentAttendanceResult.ATTENDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW,
                                1, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.RESERVED, StudentAttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                1, 0, 0, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.OFFERED, StudentAttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                0, 0, 0, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.WAITING, StudentAttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusHours(1),
                                0, 0, 0, 0, 1, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, StudentAttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        context(BookingWindow.CLOSED, NOW.plusMinutes(30),
                                0, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, StudentAttendanceResult.NOT_RECORDED, BookingAvailability.RESERVABLE,
                        context(BookingWindow.OPEN, NOW.plusHours(1),
                                0, 0, 0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, StudentAttendanceResult.NOT_RECORDED, BookingAvailability.WAITLISTABLE,
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
