package com.classitda.classes.application.student;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.classes.domain.AttendanceResult;
import com.classitda.classes.domain.BookingWindow;
import com.classitda.classes.domain.EnrollmentStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StudentBookingDecisionPolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);

    private final StudentBookingDecisionPolicy decisionPolicy = new StudentBookingDecisionPolicy();

    @ParameterizedTest(name = "{0} 예약 관계와 {1} 출결 결과와 {2} 예약 가능 여부를 결정한다")
    @MethodSource("bookingDecisionContexts")
    void 회원_수업_상태를_예약_관계와_출결_결과와_예약_가능_여부로_결정한다(
            StudentBookingRelation expectedBookingRelation,
            AttendanceResult expectedAttendanceResult,
            BookingAvailability expectedAvailability,
            StudentSessionFacts facts
    ) {
        StudentBookingDecision decision = decisionPolicy.decide(facts);

        assertThat(decision.bookingRelation()).isEqualTo(expectedBookingRelation);
        assertThat(decision.attendanceResult()).isEqualTo(expectedAttendanceResult);
        assertThat(decision.availability()).isEqualTo(expectedAvailability);
    }

    private static Stream<Arguments> bookingDecisionContexts() {
        return Stream.of(
                Arguments.of(
                        StudentBookingRelation.NONE, AttendanceResult.ABSENT, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW.minusHours(2), EnrollmentStatus.RESERVED, AttendanceResult.ABSENT, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, AttendanceResult.ATTENDED, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW.minusHours(2), EnrollmentStatus.RESERVED, AttendanceResult.ATTENDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW.minusHours(2), EnrollmentStatus.RESERVED, AttendanceResult.NOT_RECORDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW, EnrollmentStatus.RESERVED, AttendanceResult.NOT_RECORDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.RESERVED, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW.plusHours(1), EnrollmentStatus.RESERVED, AttendanceResult.NOT_RECORDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.OFFERED, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW.plusHours(1), EnrollmentStatus.OFFERED, AttendanceResult.NOT_RECORDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.WAITING, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW.plusHours(1), EnrollmentStatus.WAITING, AttendanceResult.NOT_RECORDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.CLOSED,
                        facts(BookingWindow.CLOSED, NOW.plusMinutes(30), null, AttendanceResult.NOT_RECORDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.RESERVABLE,
                        facts(BookingWindow.OPEN, NOW.plusHours(1), null, AttendanceResult.NOT_RECORDED, 1)
                ),
                Arguments.of(
                        StudentBookingRelation.NONE, AttendanceResult.NOT_RECORDED, BookingAvailability.WAITLISTABLE,
                        facts(BookingWindow.OPEN, NOW.plusHours(1), null, AttendanceResult.NOT_RECORDED, 0)
                )
        );
    }

    private static StudentSessionFacts facts(
            BookingWindow bookingWindow,
            LocalDateTime startAt,
            EnrollmentStatus ownEnrollmentStatus,
            AttendanceResult attendanceResult,
            long remainingCapacity
    ) {
        return new StudentSessionFacts(
                bookingWindow,
                startAt,
                Optional.ofNullable(ownEnrollmentStatus),
                attendanceResult,
                remainingCapacity,
                NOW
        );
    }
}
