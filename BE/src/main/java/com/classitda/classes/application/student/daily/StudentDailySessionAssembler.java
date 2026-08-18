package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentBookingContext;
import com.classitda.classes.application.student.StudentBookingStatus;
import com.classitda.classes.application.student.StudentBookingStatusResolver;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentDailySessionAssembler {

    private final StudentBookingStatusResolver bookingStatusResolver;

    StudentDailySessionView assemble(
            ClassSessionDailyProjection classSession,
            ReservationSummaryProjection reservationSummary,
            WaitingSummaryProjection waitingSummary,
            int reservationCloseMinutesBefore,
            LocalDateTime now
    ) {
        long reservedCount = reservationSummary == null ? 0 : reservationSummary.getReservedCount();
        long waitingCount = waitingSummary == null ? 0 : waitingSummary.getWaitingCount();
        long remainingCapacity = Math.max((long) classSession.getCapacity() - reservedCount, 0);

        StudentBookingContext bookingContext = new StudentBookingContext(
                classSession.getSessionStatus(),
                classSession.getStartAt(),
                classSession.getEndAt(),
                reservationSummary == null ? 0 : reservationSummary.getOwnReservedCount(),
                reservationSummary == null ? 0 : reservationSummary.getOwnAttendedCount(),
                reservationSummary == null ? 0 : reservationSummary.getOwnNoShowCount(),
                waitingSummary == null ? 0 : waitingSummary.getOwnOfferedCount(),
                waitingSummary == null ? 0 : waitingSummary.getOwnWaitingCount(),
                reservationCloseMinutesBefore,
                remainingCapacity,
                now
        );
        StudentBookingStatus bookingStatus = bookingStatusResolver.resolve(bookingContext);

        return StudentDailySessionView.of(
                classSession,
                reservedCount,
                remainingCapacity,
                waitingCount,
                bookingStatus
        );
    }
}
