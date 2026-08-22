package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassTemplateFixture;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ClassTemplateTest {

    @Test
    void 수업_템플릿을_생성하면_입력한_모든_상세_정보를_가진다() {
        // given
        Long studioId = 1L;
        String name = "저녁 요가";
        String description = "퇴근 후 진행하는 수업";
        ClassForm classForm = ClassForm.GROUP;
        int durationMinutes = 60;
        LocalTime startTime = LocalTime.of(20, 0);
        Set<DayOfWeek> recurringDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        int capacity = 12;

        // when
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                studioId,
                name,
                description,
                classForm,
                durationMinutes,
                startTime,
                recurringDays,
                capacity
        );

        // then
        assertThat(classTemplate.getStudioId()).isEqualTo(studioId);
        assertThat(classTemplate.getName()).isEqualTo(name);
        assertThat(classTemplate.getDescription()).isEqualTo(description);
        assertThat(classTemplate.getClassForm()).isEqualTo(classForm);
        assertThat(classTemplate.getDurationMinutes()).isEqualTo(durationMinutes);
        assertThat(classTemplate.getStartTime()).isEqualTo(startTime);
        assertThat(classTemplate.getRecurringDays()).containsExactlyInAnyOrderElementsOf(recurringDays);
        assertThat(classTemplate.getCapacity()).isEqualTo(capacity);
    }

    @Test
    void 최소_경계값으로_수업_템플릿을_생성할_수_있다() {
        // given
        String name = "가";
        int durationMinutes = 1;
        int capacity = 1;

        // when
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                1L,
                name,
                "최소 경계값 수업",
                ClassForm.INDIVIDUAL,
                durationMinutes,
                LocalTime.MIN,
                Set.of(),
                capacity
        );

        // then
        assertThat(classTemplate.getName()).isEqualTo(name);
        assertThat(classTemplate.getDurationMinutes()).isEqualTo(durationMinutes);
        assertThat(classTemplate.getCapacity()).isEqualTo(capacity);
    }

    @Test
    void 진행_시간이_1440분이면_수업_템플릿을_생성할_수_있다() {
        // when
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                1_440,
                LocalTime.of(20, 0),
                Set.of(),
                12
        );

        // then
        assertThat(classTemplate.getDurationMinutes()).isEqualTo(1_440);
    }

    @Test
    void 이름이_100자면_수업_템플릿을_생성할_수_있다() {
        // given
        String name = "가".repeat(100);

        // when
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                1L,
                name,
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                Set.of(),
                12
        );

        // then
        assertThat(classTemplate.getName()).hasSize(100);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_이름")
    void 이름이_유효하지_않으면_001_예외가_발생한다(String name) {
        // given
        Set<DayOfWeek> recurringDays = Set.of(DayOfWeek.MONDAY);

        // when / then
        assertThatThrownBy(() -> 수업_템플릿을_생성한다(
                1L,
                name,
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                recurringDays,
                12
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_CLASS_TEMPLATE_NAME));
    }

    @Test
    void 시설_ID가_null이면_012_예외가_발생한다() {
        // given
        Long studioId = null;

        // when / then
        assertThatThrownBy(() -> 수업_템플릿을_생성한다(
                studioId,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                Set.of(),
                12
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.CLASS_TEMPLATE_STUDIO_REQUIRED));
    }

    @Test
    void 수업_형태가_null이면_002_예외가_발생한다() {
        // given
        ClassForm classForm = null;

        // when / then
        assertThatThrownBy(() -> 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                null,
                classForm,
                60,
                LocalTime.of(20, 0),
                Set.of(),
                12
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_CLASS_FORM));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 1_441})
    void 진행_시간이_유효한_범위를_벗어나면_003_예외가_발생한다(int durationMinutes) {
        // given
        LocalTime startTime = LocalTime.of(20, 0);

        // when / then
        assertThatThrownBy(() -> 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                durationMinutes,
                startTime,
                Set.of(),
                12
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_DURATION_MINUTES));
    }

    @Test
    void 시작_시간이_null이면_009_예외가_발생한다() {
        // given
        LocalTime startTime = null;

        // when / then
        assertThatThrownBy(() -> 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                startTime,
                Set.of(),
                12
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_START_TIME));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 정원이_1명_미만이면_004_예외가_발생한다(int capacity) {
        // given
        Set<DayOfWeek> recurringDays = Set.of();

        // when / then
        assertThatThrownBy(() -> 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                recurringDays,
                capacity
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_CAPACITY));
    }

    @Test
    void 설명이_null이어도_수업_템플릿을_생성할_수_있다() {
        // given
        String description = null;

        // when
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                description,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                Set.of(),
                12
        );

        // then
        assertThat(classTemplate.getDescription()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 반복_요일이_null이거나_비어_있으면_빈_요일로_생성된다(Set<DayOfWeek> recurringDays) {
        // given
        String name = "저녁 요가";

        // when
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                1L,
                name,
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                recurringDays,
                12
        );

        // then
        assertThat(classTemplate.getRecurringDays()).isEmpty();
    }

    @Test
    void 반복_요일에_null이_포함되면_011_예외가_발생한다() {
        // given
        Set<DayOfWeek> recurringDays = new HashSet<>();
        recurringDays.add(DayOfWeek.MONDAY);
        recurringDays.add(null);

        // when / then
        assertThatThrownBy(() -> 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                recurringDays,
                12
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_RECURRING_DAY));
    }

    @Test
    void 생성_후_입력한_반복_요일을_변경해도_템플릿의_요일은_변경되지_않는다() {
        // given
        Set<DayOfWeek> recurringDays = EnumSet.of(DayOfWeek.MONDAY);
        ClassTemplate classTemplate = ClassTemplateFixture.기본_수업_템플릿(1L, "저녁 요가", recurringDays);

        // when
        recurringDays.add(DayOfWeek.FRIDAY);

        // then
        assertThat(classTemplate.getRecurringDays()).containsExactly(DayOfWeek.MONDAY);
    }

    @Test
    void 조회한_반복_요일은_수정할_수_없고_템플릿의_요일도_유지된다() {
        // given
        ClassTemplate classTemplate = ClassTemplateFixture.기본_수업_템플릿(
                1L,
                "저녁 요가",
                Set.of(DayOfWeek.MONDAY)
        );
        Set<DayOfWeek> recurringDays = classTemplate.getRecurringDays();

        // when / then
        assertThatThrownBy(() -> recurringDays.add(DayOfWeek.FRIDAY))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(classTemplate.getRecurringDays()).containsExactly(DayOfWeek.MONDAY);
    }

    @Test
    void 수정_전에_조회한_반복_요일은_수정_후에도_기존_내용을_유지한다() {
        // given
        ClassTemplate classTemplate = ClassTemplateFixture.기본_수업_템플릿(
                1L,
                "저녁 요가",
                Set.of(DayOfWeek.MONDAY)
        );
        Set<DayOfWeek> previousRecurringDays = classTemplate.getRecurringDays();

        // when
        classTemplate.updateDetails(
                "아침 요가",
                "출근 전 수업",
                ClassForm.GROUP,
                50,
                LocalTime.of(7, 0),
                Set.of(DayOfWeek.TUESDAY),
                10
        );

        // then
        assertThat(previousRecurringDays).containsExactly(DayOfWeek.MONDAY);
        assertThat(classTemplate.getRecurringDays()).containsExactly(DayOfWeek.TUESDAY);
    }

    @Test
    void 수업_템플릿을_수정하면_모든_상세_정보가_교체되고_시설은_유지된다() {
        // given
        ClassTemplate classTemplate = ClassTemplateFixture.기본_수업_템플릿(
                1L,
                "저녁 요가",
                Set.of(DayOfWeek.MONDAY)
        );
        Long studioId = classTemplate.getStudioId();

        // when
        classTemplate.updateDetails(
                "아침 개인 필라테스",
                "개인별 자세 교정 수업",
                ClassForm.INDIVIDUAL,
                50,
                LocalTime.of(9, 30),
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                1
        );

        // then
        assertThat(classTemplate.getStudioId()).isEqualTo(studioId);
        assertThat(classTemplate.getName()).isEqualTo("아침 개인 필라테스");
        assertThat(classTemplate.getDescription()).isEqualTo("개인별 자세 교정 수업");
        assertThat(classTemplate.getClassForm()).isEqualTo(ClassForm.INDIVIDUAL);
        assertThat(classTemplate.getDurationMinutes()).isEqualTo(50);
        assertThat(classTemplate.getStartTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(classTemplate.getRecurringDays())
                .containsExactlyInAnyOrder(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
        assertThat(classTemplate.getCapacity()).isEqualTo(1);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 반복_요일이_null이거나_비어_있으면_요일과_설명이_초기화된다(Set<DayOfWeek> recurringDays) {
        // given
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                "퇴근 후 진행하는 수업",
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                12
        );

        // when
        classTemplate.updateDetails(
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                recurringDays,
                12
        );

        // then
        assertThat(classTemplate.getDescription()).isNull();
        assertThat(classTemplate.getRecurringDays()).isEmpty();
    }

    @Test
    void 수정_후_입력한_반복_요일을_변경해도_템플릿의_요일은_변경되지_않는다() {
        // given
        ClassTemplate classTemplate = ClassTemplateFixture.기본_수업_템플릿(
                1L,
                "저녁 요가",
                Set.of(DayOfWeek.MONDAY)
        );
        Set<DayOfWeek> recurringDays = EnumSet.of(DayOfWeek.TUESDAY);

        // when
        classTemplate.updateDetails(
                "아침 요가",
                "출근 전 수업",
                ClassForm.GROUP,
                50,
                LocalTime.of(7, 0),
                recurringDays,
                10
        );
        recurringDays.add(DayOfWeek.THURSDAY);

        // then
        assertThat(classTemplate.getRecurringDays()).containsExactly(DayOfWeek.TUESDAY);
    }

    @Test
    void 반복_요일_검증에_실패하면_011_예외가_발생하고_기존_상세_정보가_모두_유지된다() {
        // given
        ClassTemplate classTemplate = 수업_템플릿을_생성한다(
                1L,
                "저녁 요가",
                "퇴근 후 진행하는 수업",
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                12
        );
        Set<DayOfWeek> invalidRecurringDays = new HashSet<>();
        invalidRecurringDays.add(DayOfWeek.TUESDAY);
        invalidRecurringDays.add(null);

        // when / then
        assertThatThrownBy(() -> classTemplate.updateDetails(
                "아침 개인 필라테스",
                "개인별 자세 교정 수업",
                ClassForm.INDIVIDUAL,
                50,
                LocalTime.of(9, 30),
                invalidRecurringDays,
                1
        ))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_RECURRING_DAY));
        assertThat(classTemplate.getName()).isEqualTo("저녁 요가");
        assertThat(classTemplate.getDescription()).isEqualTo("퇴근 후 진행하는 수업");
        assertThat(classTemplate.getClassForm()).isEqualTo(ClassForm.GROUP);
        assertThat(classTemplate.getDurationMinutes()).isEqualTo(60);
        assertThat(classTemplate.getStartTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(classTemplate.getRecurringDays())
                .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        assertThat(classTemplate.getCapacity()).isEqualTo(12);
    }

    private static Stream<String> 유효하지_않은_이름() {
        return Stream.of(null, "", "   ", "가".repeat(101));
    }

    private ClassTemplate 수업_템플릿을_생성한다(
            Long studioId,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            LocalTime startTime,
            Set<DayOfWeek> recurringDays,
            int capacity
    ) {
        return ClassTemplate.builder()
                .studioId(studioId)
                .name(name)
                .description(description)
                .classForm(classForm)
                .durationMinutes(durationMinutes)
                .startTime(startTime)
                .recurringDays(recurringDays)
                .capacity(capacity)
                .build();
    }
}
