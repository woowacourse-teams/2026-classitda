package com.classitda.classes.application.student;

public record StudentBookingDecision(
        StudentParticipation participation,
        BookingAvailability availability
) {

    public StudentBookingStatus legacyStatus() {
        return switch (participation) {
            case RESERVED -> StudentBookingStatus.RESERVED;
            case WAITING -> StudentBookingStatus.WAITING;
            case OFFERED -> StudentBookingStatus.OFFERED;
            case ATTENDED -> StudentBookingStatus.ATTENDED;
            case ABSENT -> StudentBookingStatus.ABSENT;
            case NONE -> switch (availability) {
                case RESERVABLE -> StudentBookingStatus.AVAILABLE;
                case WAITLISTABLE -> StudentBookingStatus.WAITING_AVAILABLE;
                case CLOSED -> StudentBookingStatus.CLOSED;
            };
        };
    }
}
