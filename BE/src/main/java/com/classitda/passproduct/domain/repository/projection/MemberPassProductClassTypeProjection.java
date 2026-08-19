package com.classitda.passproduct.domain.repository.projection;

import java.time.LocalDate;

public interface MemberPassProductClassTypeProjection {

    Long getMemberPassProductId();

    Long getClassTypeId();

    LocalDate getStartedAt();

    LocalDate getExpiresAt();
}
