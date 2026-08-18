package com.classitda.classes.application.student;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.classes.domain.ClassSessionStatus;
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
                        StudentBookingStatus.CANCELED,
                        context(ClassSessionStatus.CANCELED, NOW.minusHours(2), NOW.minusHours(1),
                                1, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.COMPLETED,
                        context(ClassSessionStatus.OPENED, NOW.minusHours(2), NOW,
                                1, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.RESERVED,
                        context(ClassSessionStatus.CLOSED, NOW.plusHours(1), NOW.plusHours(2),
                                1, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.OFFERED,
                        context(ClassSessionStatus.CLOSED, NOW.plusHours(1), NOW.plusHours(2),
                                0, 1, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.WAITING,
                        context(ClassSessionStatus.CLOSED, NOW.plusHours(1), NOW.plusHours(2),
                                0, 0, 1, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.CLOSED,
                        context(ClassSessionStatus.OPENED, NOW.plusMinutes(30), NOW.plusHours(1),
                                0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.AVAILABLE,
                        context(ClassSessionStatus.OPENED, NOW.plusHours(1), NOW.plusHours(2),
                                0, 0, 0, 1)
                ),
                Arguments.of(
                        StudentBookingStatus.WAITING_AVAILABLE,
                        context(ClassSessionStatus.OPENED, NOW.plusHours(1), NOW.plusHours(2),
                                0, 0, 0, 0)
                )
        );
    }

    private static StudentBookingContext context(
            ClassSessionStatus sessionStatus,
            LocalDateTime startAt,
            LocalDateTime endAt,
            long ownReservedCount,
            long ownOfferedCount,
            long ownWaitingCount,
            long remainingCapacity
    ) {
        return new StudentBookingContext(
                sessionStatus,
                startAt,
                endAt,
                ownReservedCount,
                ownOfferedCount,
                ownWaitingCount,
                30,
                remainingCapacity,
                NOW
        );
    }
}
