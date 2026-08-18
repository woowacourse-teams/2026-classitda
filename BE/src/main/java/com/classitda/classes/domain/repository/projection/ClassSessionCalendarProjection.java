package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ClassSessionStatus;
import java.time.LocalDateTime;

public interface ClassSessionCalendarProjection {

    Long getInstructorMembershipId();

    LocalDateTime getStartAt();

    LocalDateTime getEndAt();

    ClassSessionStatus getSessionStatus();
}
