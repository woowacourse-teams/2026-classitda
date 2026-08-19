package com.classitda.classes.domain;

import static com.classitda.classes.fixture.ClassSessionFixture.기본_담당_강사_소속;
import static com.classitda.classes.fixture.ClassSessionFixture.기본_수업_회차;
import static com.classitda.classes.fixture.ClassSessionFixture.상태가_다른_수업_회차;
import static com.classitda.classes.fixture.ClassSessionFixture.수업_종류_연결;
import static com.classitda.classes.fixture.ClassSessionFixture.수업_회차;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.studio.domain.StudioMembership;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClassSessionTest {

    @Test
    void 수업_회차를_생성하면_종료_시각을_시작_시각과_진행_시간으로_계산한다() {
        // given
        StudioMembership instructorMembership = 기본_담당_강사_소속();
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 17, 20, 0);

        // when
        ClassSession classSession = 수업_회차(
                1L,
                instructorMembership,
                "저녁 요가",
                "퇴근 후 진행하는 수업",
                ClassForm.GROUP,
                60,
                12,
                startAt,
                ClassSessionStatus.OPENED
        );

        // then
        assertThat(classSession.getStudioId()).isEqualTo(1L);
        assertThat(classSession.getInstructorMembership()).isSameAs(instructorMembership);
        assertThat(classSession.getName()).isEqualTo("저녁 요가");
        assertThat(classSession.getDescription()).isEqualTo("퇴근 후 진행하는 수업");
        assertThat(classSession.getClassForm()).isEqualTo(ClassForm.GROUP);
        assertThat(classSession.getDurationMinutes()).isEqualTo(60);
        assertThat(classSession.getCapacity()).isEqualTo(12);
        assertThat(classSession.getStartAt()).isEqualTo(startAt);
        assertThat(classSession.getEndAt()).isEqualTo(startAt.plusMinutes(60));
        assertThat(classSession.getStatus()).isEqualTo(ClassSessionStatus.OPENED);
    }

    @Test
    void 이름_진행_시간_정원의_최소_경계값으로_수업_회차를_생성할_수_있다() {
        // given
        String name = "가";
        int durationMinutes = 1;
        int capacity = 1;

        // when
        ClassSession classSession = 수업_회차(
                1L,
                기본_담당_강사_소속(),
                name,
                null,
                ClassForm.INDIVIDUAL,
                durationMinutes,
                capacity,
                LocalDateTime.MIN,
                ClassSessionStatus.OPENED
        );

        // then
        assertThat(classSession.getName()).isEqualTo(name);
        assertThat(classSession.getDurationMinutes()).isEqualTo(durationMinutes);
        assertThat(classSession.getCapacity()).isEqualTo(capacity);
        assertThat(classSession.getDescription()).isNull();
    }

    @Test
    void 진행_시간이_1440분이면_수업_회차를_생성할_수_있다() {
        // when
        ClassSession classSession = 수업_회차(
                1L,
                기본_담당_강사_소속(),
                "저녁 요가",
                null,
                ClassForm.GROUP,
                1_440,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        );

        // then
        assertThat(classSession.getDurationMinutes()).isEqualTo(1_440);
    }

    @Test
    void 이름이_100자면_수업_회차를_생성할_수_있다() {
        // given
        String name = "가".repeat(100);

        // when
        ClassSession classSession = 수업_회차(
                1L,
                기본_담당_강사_소속(),
                name,
                null,
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        );

        // then
        assertThat(classSession.getName()).hasSize(100);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_이름")
    void 이름이_유효하지_않으면_수업_회차를_생성할_수_없다(String name) {
        // given
        StudioMembership instructorMembership = 기본_담당_강사_소속();

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                instructorMembership,
                name,
                null,
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        ), ClassErrorCode.INVALID_CLASS_SESSION_NAME);
    }

    @Test
    void 시설_ID가_null이면_수업_회차를_생성할_수_없다() {
        // given
        Long studioId = null;

        // when / then
        assertClassError(() -> 수업_회차(
                studioId,
                기본_담당_강사_소속(),
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        ), ClassErrorCode.CLASS_SESSION_STUDIO_REQUIRED);
    }

    @Test
    void 담당_강사_소속이_null이면_수업_회차를_생성할_수_없다() {
        // given
        StudioMembership instructorMembership = null;

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                instructorMembership,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        ), ClassErrorCode.CLASS_SESSION_INSTRUCTOR_REQUIRED);
    }

    @Test
    void 수업_형태가_null이면_수업_회차를_생성할_수_없다() {
        // given
        ClassForm classForm = null;

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                기본_담당_강사_소속(),
                "저녁 요가",
                null,
                classForm,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        ), ClassErrorCode.INVALID_CLASS_SESSION_FORM);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 1_441})
    void 진행_시간이_유효한_범위를_벗어나면_수업_회차를_생성할_수_없다(int durationMinutes) {
        // given
        StudioMembership instructorMembership = 기본_담당_강사_소속();

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                instructorMembership,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                durationMinutes,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        ), ClassErrorCode.INVALID_CLASS_SESSION_DURATION_MINUTES);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 정원이_1명_미만이면_수업_회차를_생성할_수_없다(int capacity) {
        // given
        StudioMembership instructorMembership = 기본_담당_강사_소속();

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                instructorMembership,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                capacity,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        ), ClassErrorCode.INVALID_CLASS_SESSION_CAPACITY);
    }

    @Test
    void 시작_시각이_null이면_수업_회차를_생성할_수_없다() {
        // given
        LocalDateTime startAt = null;

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                기본_담당_강사_소속(),
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                12,
                startAt,
                ClassSessionStatus.OPENED
        ), ClassErrorCode.INVALID_CLASS_SESSION_START_AT);
    }

    @Test
    void 종료_시각을_계산할_수_없으면_수업_회차를_생성할_수_없다() {
        // given
        LocalDateTime startAt = LocalDateTime.MAX;

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                기본_담당_강사_소속(),
                "저녁 요가",
                null,
                ClassForm.GROUP,
                1,
                12,
                startAt,
                ClassSessionStatus.OPENED
        ), ClassErrorCode.INVALID_CLASS_SESSION_START_AT);
    }

    @Test
    void 상태가_null이면_수업_회차를_생성할_수_없다() {
        // given
        ClassSessionStatus status = null;

        // when / then
        assertClassError(() -> 수업_회차(
                1L,
                기본_담당_강사_소속(),
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                status
        ), ClassErrorCode.CLASS_SESSION_STATUS_REQUIRED);
    }

    @Test
    void 수업_회차_상세_정보를_수정하면_종료_시각을_다시_계산한다() {
        // given
        ClassSession classSession = 기본_수업_회차();
        Long originalStudioId = classSession.getStudioId();
        StudioMembership originalInstructor = classSession.getInstructorMembership();
        ClassSessionStatus originalStatus = classSession.getStatus();
        LocalDateTime changedStartAt = LocalDateTime.of(2026, 8, 18, 11, 0);

        // when
        classSession.updateDetails(
                "아침 개인 필라테스",
                "개인별 자세 교정",
                ClassForm.INDIVIDUAL,
                90,
                1,
                changedStartAt
        );

        // then
        assertThat(classSession.getName()).isEqualTo("아침 개인 필라테스");
        assertThat(classSession.getDescription()).isEqualTo("개인별 자세 교정");
        assertThat(classSession.getClassForm()).isEqualTo(ClassForm.INDIVIDUAL);
        assertThat(classSession.getDurationMinutes()).isEqualTo(90);
        assertThat(classSession.getCapacity()).isEqualTo(1);
        assertThat(classSession.getStartAt()).isEqualTo(changedStartAt);
        assertThat(classSession.getEndAt()).isEqualTo(changedStartAt.plusMinutes(90));
        assertThat(classSession.getStudioId()).isEqualTo(originalStudioId);
        assertThat(classSession.getInstructorMembership()).isSameAs(originalInstructor);
        assertThat(classSession.getStatus()).isEqualTo(originalStatus);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_수정")
    void 수정할_상세_정보가_유효하지_않으면_예외가_발생하고_기존_정보를_유지한다(
            ClassErrorCode expectedErrorCode,
            Consumer<ClassSession> update
    ) {
        // given
        ClassSession classSession = 기본_수업_회차();

        // when / then
        assertClassError(() -> update.accept(classSession), expectedErrorCode);
        assertDefaultDetails(classSession);
    }

    @Test
    void 취소된_수업_회차는_상세_정보를_수정할_수_없다() {
        // given
        ClassSession classSession = 상태가_다른_수업_회차(ClassSessionStatus.CANCELED);

        // when / then
        assertClassError(() -> classSession.updateDetails(
                "아침 요가",
                null,
                ClassForm.INDIVIDUAL,
                30,
                1,
                LocalDateTime.of(2026, 8, 18, 9, 0)
        ), ClassErrorCode.CLASS_SESSION_CANCELED);
        assertThat(classSession.getName()).isEqualTo("저녁 요가");
        assertThat(classSession.getStatus()).isEqualTo(ClassSessionStatus.CANCELED);
    }

    @ParameterizedTest
    @MethodSource("수업_단계")
    void 현재_시각에_따라_수업_단계를_계산한다(
            ClassSessionStatus status,
            LocalDateTime now,
            SessionPhase expectedPhase
    ) {
        // given
        ClassSession classSession = 상태가_다른_수업_회차(status);

        // when
        SessionPhase phase = classSession.phaseAt(now);

        // then
        assertThat(phase).isEqualTo(expectedPhase);
    }

    @Test
    void 수업_단계를_계산할_현재_시각은_필수다() {
        // given
        ClassSession classSession = 기본_수업_회차();

        // when / then
        assertClassError(
                () -> classSession.phaseAt(null),
                ClassErrorCode.CLASS_SESSION_CURRENT_TIME_REQUIRED
        );
    }

    @Test
    void 수업을_취소하면_취소_시각을_기록한다() {
        // given
        ClassSession classSession = 기본_수업_회차();
        LocalDateTime canceledAt = classSession.getStartAt().minusMinutes(10);

        // when
        classSession.cancel(canceledAt);

        // then
        assertThat(classSession.getCanceledAt()).isEqualTo(canceledAt);
        assertThat(classSession.getStatus()).isEqualTo(ClassSessionStatus.CANCELED);
        assertThat(classSession.isCanceled()).isTrue();
        assertThat(classSession.phaseAt(canceledAt)).isEqualTo(SessionPhase.CANCELED);
    }

    @Test
    void 수업_취소_시각은_필수다() {
        // given
        ClassSession classSession = 기본_수업_회차();

        // when / then
        assertClassError(() -> classSession.cancel(null), ClassErrorCode.CLASS_SESSION_CANCEL_OCCURRED_AT_REQUIRED);
        assertThat(classSession.getCanceledAt()).isNull();
        assertThat(classSession.getStatus()).isEqualTo(ClassSessionStatus.OPENED);
    }

    @Test
    void 이미_취소된_수업은_다시_취소할_수_없다() {
        // given
        ClassSession classSession = 기본_수업_회차();
        LocalDateTime originalCanceledAt = classSession.getStartAt().minusMinutes(10);
        classSession.cancel(originalCanceledAt);

        // when / then
        assertClassError(() -> classSession.cancel(originalCanceledAt.plusMinutes(1)), ClassErrorCode.CLASS_SESSION_ALREADY_CANCELED);
        assertThat(classSession.getCanceledAt()).isEqualTo(originalCanceledAt);
        assertThat(classSession.getStatus()).isEqualTo(ClassSessionStatus.CANCELED);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 30, 60})
    void 수업_시작_시각부터는_취소할_수_없다(int minutesAfterStart) {
        // given
        ClassSession classSession = 기본_수업_회차();
        LocalDateTime occurredAt = classSession.getStartAt().plusMinutes(minutesAfterStart);

        // when / then
        assertClassError(() -> classSession.cancel(occurredAt), ClassErrorCode.CLASS_SESSION_ALREADY_STARTED);
        assertThat(classSession.getCanceledAt()).isNull();
        assertThat(classSession.getStatus()).isEqualTo(ClassSessionStatus.OPENED);
    }

    @Test
    void 취소_시각이_기록된_수업은_상세_정보를_수정할_수_없다() {
        // given
        ClassSession classSession = 기본_수업_회차();
        classSession.cancel(classSession.getStartAt().minusMinutes(10));

        // when / then
        assertClassError(() -> classSession.updateDetails(
                "아침 요가",
                null,
                ClassForm.INDIVIDUAL,
                30,
                1,
                LocalDateTime.of(2026, 8, 18, 9, 0)
        ), ClassErrorCode.CLASS_SESSION_CANCELED);
        assertThat(classSession.getName()).isEqualTo("저녁 요가");
    }

    @Test
    void 수업_회차와_양수_수업_종류_ID로_연결을_생성할_수_있다() {
        // given
        Long classSessionId = 1L;
        Long classTypeId = 2L;

        // when
        ClassSessionClassType link = 수업_종류_연결(classSessionId, classTypeId);

        // then
        assertThat(link.getClassSessionId()).isEqualTo(classSessionId);
        assertThat(link.getClassTypeId()).isEqualTo(classTypeId);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_수업_종류_ID")
    void 수업_종류_ID가_null이거나_양수가_아니면_연결을_생성할_수_없다(Long classTypeId) {
        // given
        Long classSessionId = 1L;

        // when / then
        assertClassError(
                () -> 수업_종류_연결(classSessionId, classTypeId),
                ClassErrorCode.INVALID_CLASS_SESSION_CLASS_TYPE_ID
        );
    }

    @Test
    void 수업_종류_ID를_수정할_수_있다() {
        // given
        ClassSessionClassType link = 수업_종류_연결(1L, 2L);

        // when
        link.updateClassTypeId(3L);

        // then
        assertThat(link.getClassSessionId()).isEqualTo(1L);
        assertThat(link.getClassTypeId()).isEqualTo(3L);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_수업_종류_ID")
    void 수정할_수업_종류_ID가_null이거나_양수가_아니면_예외가_발생하고_기존_ID를_유지한다(Long classTypeId) {
        // given
        ClassSessionClassType link = 수업_종류_연결(1L, 2L);

        // when / then
        assertClassError(
                () -> link.updateClassTypeId(classTypeId),
                ClassErrorCode.INVALID_CLASS_SESSION_CLASS_TYPE_ID
        );
        assertThat(link.getClassTypeId()).isEqualTo(2L);
    }

    private static Stream<Arguments> 유효하지_않은_이름() {
        return Stream.of(
                arguments((String) null),
                arguments(""),
                arguments("   "),
                arguments("가".repeat(101))
        );
    }

    private static Stream<Arguments> 유효하지_않은_수정() {
        LocalDateTime changedStartAt = LocalDateTime.of(2026, 8, 18, 11, 0);
        return Stream.of(
                arguments(
                        ClassErrorCode.INVALID_CLASS_SESSION_NAME,
                        (Consumer<ClassSession>) session -> session.updateDetails(
                                "   ", null, ClassForm.GROUP, 60, 12, changedStartAt)
                ),
                arguments(
                        ClassErrorCode.INVALID_CLASS_SESSION_FORM,
                        (Consumer<ClassSession>) session -> session.updateDetails(
                                "아침 요가", null, null, 60, 12, changedStartAt)
                ),
                arguments(
                        ClassErrorCode.INVALID_CLASS_SESSION_DURATION_MINUTES,
                        (Consumer<ClassSession>) session -> session.updateDetails(
                                "아침 요가", null, ClassForm.GROUP, 0, 12, changedStartAt)
                ),
                arguments(
                        ClassErrorCode.INVALID_CLASS_SESSION_CAPACITY,
                        (Consumer<ClassSession>) session -> session.updateDetails(
                                "아침 요가", null, ClassForm.GROUP, 60, 0, changedStartAt)
                ),
                arguments(
                        ClassErrorCode.INVALID_CLASS_SESSION_START_AT,
                        (Consumer<ClassSession>) session -> session.updateDetails(
                                "아침 요가", null, ClassForm.GROUP, 60, 12, null)
                ),
                arguments(
                        ClassErrorCode.INVALID_CLASS_SESSION_START_AT,
                        (Consumer<ClassSession>) session -> session.updateDetails(
                                "아침 요가", null, ClassForm.GROUP, 1, 12, LocalDateTime.MAX)
                )
        );
    }

    private static Stream<Arguments> 유효하지_않은_수업_종류_ID() {
        return Stream.of(
                arguments((Long) null),
                arguments(0L),
                arguments(-1L)
        );
    }

    private static Stream<Arguments> 수업_단계() {
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 17, 20, 0);
        LocalDateTime endAt = startAt.plusHours(1);
        return Stream.of(
                arguments(ClassSessionStatus.OPENED, startAt.minusNanos(1), SessionPhase.SCHEDULED),
                arguments(ClassSessionStatus.CLOSED, startAt.minusNanos(1), SessionPhase.SCHEDULED),
                arguments(ClassSessionStatus.OPENED, startAt, SessionPhase.IN_PROGRESS),
                arguments(ClassSessionStatus.OPENED, endAt.minusNanos(1), SessionPhase.IN_PROGRESS),
                arguments(ClassSessionStatus.OPENED, endAt, SessionPhase.COMPLETED),
                arguments(ClassSessionStatus.CANCELED, endAt.plusHours(1), SessionPhase.CANCELED)
        );
    }

    private void assertClassError(ThrowingCallable callable, ClassErrorCode expectedErrorCode) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(expectedErrorCode));
    }

    private void assertDefaultDetails(ClassSession classSession) {
        assertThat(classSession.getName()).isEqualTo("저녁 요가");
        assertThat(classSession.getDescription()).isEqualTo("퇴근 후 진행하는 수업");
        assertThat(classSession.getClassForm()).isEqualTo(ClassForm.GROUP);
        assertThat(classSession.getDurationMinutes()).isEqualTo(60);
        assertThat(classSession.getCapacity()).isEqualTo(12);
        assertThat(classSession.getStartAt()).isEqualTo(LocalDateTime.of(2026, 8, 17, 20, 0));
        assertThat(classSession.getEndAt()).isEqualTo(LocalDateTime.of(2026, 8, 17, 21, 0));
    }
}
