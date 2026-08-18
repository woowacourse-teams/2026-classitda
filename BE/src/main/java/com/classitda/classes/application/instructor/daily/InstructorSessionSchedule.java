package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import java.util.List;
import java.util.Map;

record InstructorSessionSchedule(
        List<ClassSessionDailyProjection> classSessions,
        Map<Long, ReservationSummaryProjection> reservationSummaries,
        Map<Long, WaitingSummaryProjection> waitingSummaries,
        int reservationCloseMinutesBefore
) {

    static InstructorSessionSchedule empty() {
        return new InstructorSessionSchedule(List.of(), Map.of(), Map.of(), 0);
    }

    boolean isEmpty() {
        return classSessions.isEmpty();
    }

    ReservationSummaryProjection reservationSummary(Long classSessionId) {
        return reservationSummaries.get(classSessionId);
    }

    WaitingSummaryProjection waitingSummary(Long classSessionId) {
        return waitingSummaries.get(classSessionId);
    }
}
