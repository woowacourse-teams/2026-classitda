package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentBookingContext;
import com.classitda.classes.application.student.StudentBookingContext.ReservationCounts;
import com.classitda.classes.application.student.StudentBookingContext.WaitingCounts;
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
        ReservationCounts reservation = toReservationCounts(reservationSummary);
        WaitingCounts waiting = toWaitingCounts(waitingSummary);
        long remainingCapacity = Math.max(
                (long) classSession.getCapacity() - reservation.totalCount(), 0
        );

        StudentBookingContext bookingContext = new StudentBookingContext(
                classSession.getSessionStatus(),
                classSession.getStartAt(),
                reservation,
                waiting,
                reservationCloseMinutesBefore,
                remainingCapacity,
                now
        );
        StudentBookingStatus bookingStatus = bookingStatusResolver.resolve(bookingContext);

        return StudentDailySessionView.of(
                classSession,
                reservation.totalCount(),
                remainingCapacity,
                waiting.totalCount(),
                bookingStatus
        );
    }

    private ReservationCounts toReservationCounts(ReservationSummaryProjection summary) {
        if (summary == null) {
            return new ReservationCounts(0, 0, 0, 0);
        }
        return new ReservationCounts(
                summary.getReservedCount(),
                summary.getOwnReservedCount(),
                summary.getOwnAttendedCount(),
                summary.getOwnAbsentCount()
        );
    }

    private WaitingCounts toWaitingCounts(WaitingSummaryProjection summary) {
        if (summary == null) {
            return new WaitingCounts(0, 0, 0);
        }
        return new WaitingCounts(
                summary.getWaitingCount(),
                summary.getOwnOfferedCount(),
                summary.getOwnWaitingCount()
        );
    }
}
