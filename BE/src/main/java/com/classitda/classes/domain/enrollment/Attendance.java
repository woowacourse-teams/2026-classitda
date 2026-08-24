package com.classitda.classes.domain.enrollment;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Attendance {

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_result", nullable = false, length = 20)
    private AttendanceResult result;

    @Column(name = "attendance_recorded_at")
    private LocalDateTime recordedAt;

    private Attendance(AttendanceResult result, LocalDateTime recordedAt) {
        this.result = result;
        this.recordedAt = recordedAt;
    }

    public static Attendance notRecorded() {
        return new Attendance(AttendanceResult.NOT_RECORDED, null);
    }

    public void markAttended(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        if (result != AttendanceResult.NOT_RECORDED) {
            throw new ClassException(ClassErrorCode.INVALID_ATTENDANCE_TRANSITION);
        }

        result = AttendanceResult.ATTENDED;
        recordedAt = occurredAt;
    }

    public void markAbsent(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        if (result != AttendanceResult.NOT_RECORDED
                && result != AttendanceResult.ATTENDED) {
            throw new ClassException(ClassErrorCode.INVALID_ATTENDANCE_TRANSITION);
        }

        result = AttendanceResult.ABSENT;
        recordedAt = occurredAt;
    }

    public boolean isRecorded() {
        return result != AttendanceResult.NOT_RECORDED;
    }

    private void requireOccurredAt(LocalDateTime occurredAt) {
        if (occurredAt == null) {
            throw new ClassException(ClassErrorCode.ATTENDANCE_OCCURRED_AT_REQUIRED);
        }
    }
}
