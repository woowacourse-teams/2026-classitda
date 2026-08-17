package com.classitda.classes.domain.repository.projection;

public interface WaitingSummaryProjection {

    Long getClassSessionId();

    long getWaitingCount();

    long getOwnWaitingCount();

    long getOwnOfferedCount();
}
