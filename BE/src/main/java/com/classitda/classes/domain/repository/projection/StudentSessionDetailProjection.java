package com.classitda.classes.domain.repository.projection;

public interface StudentSessionDetailProjection extends StudentDailySessionProjection {

    String getInstructorProfileImageUrl();

    String getStudioName();
}
