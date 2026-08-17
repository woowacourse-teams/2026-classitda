package com.classitda.classes.application.student;

import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import com.classitda.classes.presentation.dto.MemberClassSessionBookingStatus;
import com.classitda.classes.presentation.dto.MemberClassSessionResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentSessionAssembler {

    private final StudentBookingStatusResolver bookingStatusResolver;

    public MemberClassSessionResponse assemble(
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
                waitingSummary == null ? 0 : waitingSummary.getOwnOfferedCount(),
                waitingSummary == null ? 0 : waitingSummary.getOwnWaitingCount(),
                reservationCloseMinutesBefore,
                remainingCapacity,
                now
        );
        MemberClassSessionBookingStatus bookingStatus = bookingStatusResolver.resolve(bookingContext);

        return MemberClassSessionResponse.of(
                classSession,
                reservedCount,
                remainingCapacity,
                waitingCount,
                bookingStatus
        );
    }
}
