package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ClassSessionQueryRangeTest {

    @Test
    void 양끝을_포함한_42일을_시작_포함과_종료_제외_범위로_변환한다() {
        // given
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = from.plusDays(41);

        // when
        ClassSessionQueryRange range = ClassSessionQueryRange.calendar(from, to);

        // then
        assertThat(range.startInclusive()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0));
        assertThat(range.endExclusive()).isEqualTo(LocalDateTime.of(2026, 9, 12, 0, 0));
    }

    @ParameterizedTest
    @MethodSource("잘못된_조회_기간")
    void 필수값이_없거나_역전되거나_42일을_초과한_기간은_생성할_수_없다(
            LocalDate from,
            LocalDate to
    ) {
        // when / then
        assertThatThrownBy(() -> ClassSessionQueryRange.calendar(from, to))
                .isInstanceOfSatisfying(ClassitdaException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_INPUT));
    }

    private static Stream<Arguments> 잘못된_조회_기간() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        return Stream.of(
                Arguments.of(null, from),
                Arguments.of(from, null),
                Arguments.of(from.plusDays(1), from),
                Arguments.of(from, from.plusDays(42)),
                Arguments.of(LocalDate.MAX, LocalDate.MAX)
        );
    }
}
