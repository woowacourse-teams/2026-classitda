package com.classitda.classes.application.instructor;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.classes.domain.ClassSessionStatus;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InstructorSessionStatusResolverTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);

    private final InstructorSessionStatusResolver resolver = new InstructorSessionStatusResolver();

    @ParameterizedTest
    @MethodSource("강사용_수업_상태")
    void 수업_진행과_예약_접수_상태를_하나의_상태로_판정한다(
            ClassSessionStatus sessionStatus,
            LocalDateTime startAt,
            LocalDateTime endAt,
            int reservationCloseMinutesBefore,
            InstructorSessionStatus expectedStatus
    ) {
        // when
        InstructorSessionStatus status = resolver.resolve(
                sessionStatus,
                startAt,
                endAt,
                reservationCloseMinutesBefore,
                NOW
        );

        // then
        assertThat(status).isEqualTo(expectedStatus);
    }

    private static Stream<Arguments> 강사용_수업_상태() {
        return Stream.of(
                Arguments.of(
                        ClassSessionStatus.CANCELED,
                        NOW.minusHours(2),
                        NOW.minusHours(1),
                        30,
                        InstructorSessionStatus.CANCELED
                ),
                Arguments.of(
                        ClassSessionStatus.OPENED,
                        NOW.minusHours(1),
                        NOW,
                        30,
                        InstructorSessionStatus.COMPLETED
                ),
                Arguments.of(
                        ClassSessionStatus.OPENED,
                        NOW,
                        NOW.plusHours(1),
                        30,
                        InstructorSessionStatus.IN_PROGRESS
                ),
                Arguments.of(
                        ClassSessionStatus.OPENED,
                        NOW.plusHours(1),
                        NOW.plusHours(2),
                        30,
                        InstructorSessionStatus.SCHEDULED_BOOKING_OPEN
                ),
                Arguments.of(
                        ClassSessionStatus.CLOSED,
                        NOW.plusHours(1),
                        NOW.plusHours(2),
                        30,
                        InstructorSessionStatus.SCHEDULED_BOOKING_CLOSED
                ),
                Arguments.of(
                        ClassSessionStatus.OPENED,
                        NOW.plusMinutes(30),
                        NOW.plusHours(1),
                        30,
                        InstructorSessionStatus.SCHEDULED_BOOKING_CLOSED
                ),
                Arguments.of(
                        ClassSessionStatus.OPENED,
                        NOW.plusMinutes(31),
                        NOW.plusHours(1),
                        30,
                        InstructorSessionStatus.SCHEDULED_BOOKING_OPEN
                )
        );
    }
}
