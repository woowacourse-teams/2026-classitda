package com.classitda.classes.application.instructor;

import com.classitda.classes.domain.ClassSessionStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class InstructorSessionStatusResolver {

    public InstructorSessionStatus resolve(
            ClassSessionStatus sessionStatus,
            LocalDateTime startAt,
            LocalDateTime endAt,
            int reservationCloseMinutesBefore,
            LocalDateTime now
    ) {
        if (sessionStatus == ClassSessionStatus.CANCELED) {
            return InstructorSessionStatus.CANCELED;
        }
        if (!now.isBefore(endAt)) {
            return InstructorSessionStatus.COMPLETED;
        }
        if (!now.isBefore(startAt)) {
            return InstructorSessionStatus.IN_PROGRESS;
        }
        if (sessionStatus == ClassSessionStatus.CLOSED
                || !now.isBefore(startAt.minusMinutes(reservationCloseMinutesBefore))) {
            return InstructorSessionStatus.SCHEDULED_CLOSED;
        }
        return InstructorSessionStatus.SCHEDULED_OPEN;
    }
}
