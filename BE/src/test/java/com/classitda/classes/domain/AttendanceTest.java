package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AttendanceTest {

    private static final LocalDateTime ATTENDED_AT =
            LocalDateTime.of(2026, 8, 20, 20, 0);
    private static final LocalDateTime ABSENT_AT = ATTENDED_AT.plusMinutes(5);

    @Test
    void 초기_출결은_미기록_상태다() {
        Attendance attendance = Attendance.notRecorded();

        assertThat(attendance.getResult()).isEqualTo(AttendanceResult.NOT_RECORDED);
        assertThat(attendance.getRecordedAt()).isNull();
        assertThat(attendance.isRecorded()).isFalse();
    }

    @Test
    void 미기록_출결을_출석으로_기록한다() {
        Attendance attendance = Attendance.notRecorded();

        attendance.markAttended(ATTENDED_AT);

        assertThat(attendance.getResult()).isEqualTo(AttendanceResult.ATTENDED);
        assertThat(attendance.getRecordedAt()).isEqualTo(ATTENDED_AT);
        assertThat(attendance.isRecorded()).isTrue();
    }

    @Test
    void 출석을_결석으로_정정하면_기록_시각도_덮어쓴다() {
        Attendance attendance = Attendance.notRecorded();
        attendance.markAttended(ATTENDED_AT);

        attendance.markAbsent(ABSENT_AT);

        assertThat(attendance.getResult()).isEqualTo(AttendanceResult.ABSENT);
        assertThat(attendance.getRecordedAt()).isEqualTo(ABSENT_AT);
    }

    @Test
    void 기록된_출결을_다시_출석으로_바꿀_수_없다() {
        Attendance attendance = Attendance.notRecorded();
        attendance.markAttended(ATTENDED_AT);

        assertThatThrownBy(() -> attendance.markAttended(ATTENDED_AT.plusMinutes(1)))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_ATTENDANCE_TRANSITION));
        assertThat(attendance.getResult()).isEqualTo(AttendanceResult.ATTENDED);
        assertThat(attendance.getRecordedAt()).isEqualTo(ATTENDED_AT);
    }

    @Test
    void 결석은_다른_출결로_바꿀_수_없다() {
        Attendance attendance = Attendance.notRecorded();
        attendance.markAbsent(ABSENT_AT);

        assertThatThrownBy(() -> attendance.markAbsent(ABSENT_AT.plusMinutes(1)))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_ATTENDANCE_TRANSITION));
        assertThat(attendance.getResult()).isEqualTo(AttendanceResult.ABSENT);
        assertThat(attendance.getRecordedAt()).isEqualTo(ABSENT_AT);
    }

    @Test
    void 출결_처리_시각은_필수이며_실패하면_상태를_유지한다() {
        Attendance attendance = Attendance.notRecorded();

        assertThatThrownBy(() -> attendance.markAttended(null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.ATTENDANCE_OCCURRED_AT_REQUIRED));
        assertThat(attendance.getResult()).isEqualTo(AttendanceResult.NOT_RECORDED);
        assertThat(attendance.getRecordedAt()).isNull();
    }
}
