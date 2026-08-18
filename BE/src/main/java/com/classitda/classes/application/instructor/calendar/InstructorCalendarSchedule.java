package com.classitda.classes.application.instructor.calendar;

import com.classitda.classes.domain.repository.projection.ClassSessionCalendarProjection;
import java.util.List;

record InstructorCalendarSchedule(
        List<ClassSessionCalendarProjection> classSessions,
        Long requesterMembershipId,
        int reservationCloseMinutesBefore
) {

    static InstructorCalendarSchedule of(
            List<ClassSessionCalendarProjection> classSessions,
            Long requesterMembershipId,
            int reservationCloseMinutesBefore
    ) {
        return new InstructorCalendarSchedule(
                classSessions,
                requesterMembershipId,
                reservationCloseMinutesBefore
        );
    }

    static InstructorCalendarSchedule empty(Long requesterMembershipId) {
        return of(List.of(), requesterMembershipId, 0);
    }

    boolean isEmpty() {
        return classSessions.isEmpty();
    }
}
