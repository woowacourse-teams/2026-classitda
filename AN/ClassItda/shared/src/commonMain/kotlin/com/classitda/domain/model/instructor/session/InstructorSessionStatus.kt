package com.classitda.domain.model.instructor.session

enum class InstructorClassForm {
    INDIVIDUAL,
    GROUP,
    UNKNOWN,
}

enum class InstructorSessionStatus {
    SCHEDULED_BOOKING_OPEN,
    SCHEDULED_BOOKING_CLOSED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED,
    UNKNOWN,
}
