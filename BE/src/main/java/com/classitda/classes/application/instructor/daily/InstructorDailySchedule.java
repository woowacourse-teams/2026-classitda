package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.domain.repository.projection.InstructorDailySessionProjection;
import java.util.List;

record InstructorDailySchedule(
        List<InstructorDailySessionProjection> classSessions,
        int reservationCloseMinutesBefore
) {

    static InstructorDailySchedule empty() {
        return new InstructorDailySchedule(List.of(), 0);
    }

    boolean isEmpty() {
        return classSessions.isEmpty();
    }
}
