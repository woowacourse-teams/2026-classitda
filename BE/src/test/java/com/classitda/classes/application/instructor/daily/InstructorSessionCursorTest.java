package com.classitda.classes.application.instructor.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import org.junit.jupiter.api.Test;

class InstructorSessionCursorTest {

    @Test
    void Base64_형식이지만_날짜가_잘못된_커서는_COMMON_001로_거부한다() {
        assertThatThrownBy(() -> InstructorSessionCursor.decode("YWJjfDE"))
                .isInstanceOfSatisfying(ClassitdaException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_INPUT));
    }
}
