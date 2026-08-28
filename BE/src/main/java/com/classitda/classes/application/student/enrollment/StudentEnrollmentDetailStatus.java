package com.classitda.classes.application.student.enrollment;

import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;

public enum StudentEnrollmentDetailStatus {
    RESERVED,
    WAITING,
    OFFERED,
    ATTENDED,
    ABSENT,
    RESERVATION_CANCELED,
    SESSION_CANCELED;

    static StudentEnrollmentDetailStatus resolve(
            EnrollmentStatus enrollmentStatus,
            AttendanceResult attendanceResult,
            boolean sessionCanceled
    ) {
        if (enrollmentStatus == EnrollmentStatus.CANCELED) {
            return RESERVATION_CANCELED;
        }
        if (enrollmentStatus == EnrollmentStatus.EXPIRED) {
            throw new IllegalArgumentException("조회할 수 없는 수업 신청 상태입니다.");
        }
        if (sessionCanceled) {
            return SESSION_CANCELED;
        }
        if (enrollmentStatus == EnrollmentStatus.WAITING) {
            return WAITING;
        }
        if (enrollmentStatus == EnrollmentStatus.OFFERED) {
            return OFFERED;
        }

        return switch (attendanceResult) {
            case NOT_RECORDED -> RESERVED;
            case ATTENDED -> ATTENDED;
            case ABSENT -> ABSENT;
        };
    }
}
