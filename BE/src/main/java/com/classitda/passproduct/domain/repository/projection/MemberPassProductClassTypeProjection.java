package com.classitda.passproduct.domain.repository.projection;

import com.classitda.classes.domain.ClassForm;
import java.time.LocalDate;

public interface MemberPassProductClassTypeProjection {

    Long getMemberPassProductId();

    ClassForm getClassForm();

    Long getClassTypeId();

    LocalDate getStartedAt();

    LocalDate getExpiresAt();
}
