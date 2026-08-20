package com.classitda.classes.application.student.daily;

import com.classitda.classes.domain.repository.projection.StudentDailySessionProjection;
import java.util.List;

record StudentDailySchedule(
        List<StudentDailySessionProjection> classSessions,
        int reservationCloseMinutesBefore
) {

    static StudentDailySchedule empty() {
        return new StudentDailySchedule(List.of(), 0);
    }

    boolean isEmpty() {
        return classSessions.isEmpty();
    }
}
