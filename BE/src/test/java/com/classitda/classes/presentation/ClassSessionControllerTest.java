package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.application.ClassSessionQueryService;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.calendar.InstructorCalendarQueryService;
import com.classitda.classes.application.instructor.calendar.InstructorCalendarSummary;
import com.classitda.classes.application.instructor.daily.InstructorDailyQueryService;
import com.classitda.classes.application.instructor.daily.InstructorDailySessionView;
import com.classitda.classes.application.instructor.enrollment.ClassSessionInstructorEnrollmentCommandService;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionDetailQueryService;
import com.classitda.classes.application.student.StudentBookingDecision;
import com.classitda.classes.application.student.StudentBookingRelation;
import com.classitda.classes.application.student.calendar.StudentCalendarQueryService;
import com.classitda.classes.application.student.calendar.StudentCalendarSummary;
import com.classitda.classes.application.student.daily.StudentDailyQueryService;
import com.classitda.classes.application.student.daily.StudentDailySessionView;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.session.SessionPhase;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.presentation.dto.ClassSessionCreateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionCreateV2Request;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV2Request;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@Import({ApiVersionConfig.class, GlobalExceptionHandler.class})
@WebMvcTest({ClassSessionController.class, InstructorSessionController.class})
class ClassSessionControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private ClassSessionCommandService commandService;

    @MockitoBean
    private ClassSessionQueryService queryService;

    @MockitoBean
    private StudentDailyQueryService studentDailyQueryService;

    @MockitoBean
    private StudentCalendarQueryService studentCalendarQueryService;

    @MockitoBean
    private InstructorDailyQueryService instructorDailyQueryService;

    @MockitoBean
    private InstructorCalendarQueryService instructorCalendarQueryService;

    @MockitoBean
    private InstructorSessionDetailQueryService instructorSessionDetailQueryService;

    @MockitoBean
    private ClassSessionInstructorEnrollmentCommandService instructorEnrollmentCommandService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    ClassSessionControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void V2_수업_회차를_등록하면_201과_빈_본문을_반환하고_명령_서비스에_위임한다() {
        // given
        ClassSessionCreateV2Request request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = V2_수업_회차를_등록한다(7L, "2", request);

        // then
        result.expectStatus().isCreated().expectBody().isEmpty();
        verify(commandService).saveV2(eq(1L), eq(7L), eq(request));
    }

    @Test
    void V1_수업_회차를_등록하면_강사_ID_없이_명령_서비스에_위임한다() {
        // given
        ClassSessionCreateV1Request request =
                ClassSessionFixture.기본_단일_수업_회차_V1_생성_요청(3L);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/7/instructor/class-sessions")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isCreated().expectBody().isEmpty();
        verify(commandService).saveV1(1L, 7L, request);
    }

    @Test
    void V1_수업_회차를_수정하면_강사_ID_없이_명령_서비스에_위임한다() {
        // given
        ClassSessionUpdateV1Request request = ClassSessionFixture.기본_수업_회차_수정_요청(3L);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_수정한다(7L, 11L, "1", request);

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(commandService).updateV1(1L, 7L, 11L, request);
    }

    @Test
    void V2_수업_회차를_수정하면_강사_ID를_포함해_명령_서비스에_위임한다() {
        // given
        ClassSessionUpdateV2Request request =
                ClassSessionFixture.기본_수업_회차_V2_수정_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = V2_수업_회차를_수정한다(7L, 11L, request);

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(commandService).updateV2(1L, 7L, 11L, request);
    }

    @Test
    void V2_담당_강사_ID가_누락되면_COMMON_001을_반환한다() {
        // given
        ClassSessionUpdateV2Request request =
                ClassSessionFixture.기본_수업_회차_V2_수정_요청(null, 3L);

        // when
        RestTestClient.ResponseSpec result = V2_수업_회차를_수정한다(7L, 11L, request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).updateV2(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 수업_회차_수정에서_지원하지_않는_버전이면_API_002를_반환한다() {
        // given
        ClassSessionUpdateV2Request request =
                ClassSessionFixture.기본_수업_회차_V2_수정_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = client.put()
                .uri("/api/studios/7/instructor/class-sessions/11")
                .header("X-API-Version", "3")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(commandService, never()).updateV1(anyLong(), anyLong(), anyLong(), any());
        verify(commandService, never()).updateV2(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 필수_필드를_누락하면_COMMON_001을_반환하고_명령_서비스를_호출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = client.put()
                .uri("/api/studios/7/instructor/class-sessions/11")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"className":"이름만 수정"}
                        """)
                .exchange();

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).updateV1(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 전달한_수업_회차_수정값이_유효하지_않으면_COMMON_001을_반환한다() {
        // given
        ClassSessionUpdateV1Request request = ClassSessionUpdateV1Request.of(
                null, null, " ", 0, 0, null, null);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_수정한다(7L, 11L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).updateV1(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 수업_회차_수정_중_시간이_겹치면_CLASS_SESSION_015를_반환한다() {
        // given
        ClassSessionUpdateV1Request request = ClassSessionFixture.기본_수업_회차_수정_요청(3L);
        doThrow(new ClassException(ClassErrorCode.CLASS_SESSION_TIME_CONFLICT))
                .when(commandService).updateV1(1L, 7L, 11L, request);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_수정한다(7L, 11L, "1", request);

        // then
        오류를_검증한다(
                result,
                409,
                "CLASS_SESSION-015",
                "담당 강사의 기존 수업과 시간이 겹칩니다."
        );
    }

    @Test
    void 수업_회차를_취소하면_204와_빈_본문을_반환하고_명령_서비스에_위임한다() {
        // when
        RestTestClient.ResponseSpec result = 수업_회차를_취소한다(7L, 11L, "1");

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(commandService).cancel(1L, 7L, 11L);
    }

    @Test
    void 이미_취소된_수업_회차를_다시_취소하면_CLASS_SESSION_020을_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.CLASS_SESSION_ALREADY_CANCELED))
                .when(commandService).cancel(1L, 7L, 11L);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_취소한다(7L, 11L, "1");

        // then
        오류를_검증한다(result, 409, "CLASS_SESSION-020", "이미 취소된 수업입니다.");
    }

    @Test
    void 시작된_수업_회차를_취소하면_CLASS_SESSION_021을_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.CLASS_SESSION_ALREADY_STARTED))
                .when(commandService).cancel(1L, 7L, 11L);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_취소한다(7L, 11L, "1");

        // then
        오류를_검증한다(
                result,
                409,
                "CLASS_SESSION-021",
                "이미 시작된 수업은 취소할 수 없습니다."
        );
    }

    @Test
    void 수업_회차_상세를_조회하면_200과_공용_상세_정보를_반환한다() {
        // given
        ClassSessionDetailResponse response = 수업_회차_상세_응답();
        when(queryService.findOne(1L, 7L, 11L)).thenReturn(response);

        // when
        RestTestClient.ResponseSpec result = 수업_회차_상세를_조회한다(7L, 11L, "1");

        // then
        result.expectStatus().isOk().expectBody().json("""
                {
                  "id": 11,
                  "instructorMembershipId": 12,
                  "instructorName": "김강사",
                  "classForm": "GROUP",
                  "classType": {"id": 3, "name": "요가"},
                  "className": "저녁 요가",
                  "description": "편한 복장과 개인 수건을 준비해 주세요.",
                  "capacity": 12,
                  "durationMinutes": 60,
                  "startAt": "2026-08-17T20:00:00",
                  "endAt": "2026-08-17T21:00:00",
                  "sessionPhase": "SCHEDULED"
                }
                """, JsonCompareMode.STRICT);
        verify(queryService).findOne(1L, 7L, 11L);
    }

    @Test
    void 회원용_일별_수업_목록을_조회하면_200과_목록을_반환하고_조회_서비스에_위임한다() {
        // given
        LocalDate date = LocalDate.of(2026, 8, 17);
        when(studentDailyQueryService.findAll(1L, 7L, date))
                .thenReturn(List.of(회원용_일별_수업_뷰()));

        // when
        RestTestClient.ResponseSpec result = 회원용_일별_수업_목록을_조회한다(
                7L,
                "date=2026-08-17",
                "1"
        );

        // then
        result.expectStatus().isOk().expectBody().json("""
                [
                  {
                    "id": 11,
                    "enrollmentId": 19,
                    "instructorMembershipId": 12,
                    "instructorName": "김강사",
                    "classForm": "GROUP",
                    "classType": {"id": 3, "name": "요가"},
                    "className": "저녁 요가",
                    "description": "3층 A룸에서 진행합니다.",
                    "capacity": 12,
                    "reservedCount": 8,
                    "remainingCapacity": 4,
                    "waitingCount": 2,
                    "startAt": "2026-08-17T20:00:00",
                    "endAt": "2026-08-17T21:00:00",
                    "bookingRelation": "RESERVED",
                    "attendanceResult": "NOT_RECORDED",
                    "availability": null
                  }
                ]
                """, JsonCompareMode.STRICT);
        verify(studentDailyQueryService).findAll(1L, 7L, date);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_회원용_목록_쿼리")
    void 회원용_목록의_날짜_요청값이_유효하지_않으면_COMMON_001을_반환한다(String query) {
        // when
        RestTestClient.ResponseSpec result = 회원용_일별_수업_목록을_조회한다(7L, query, "1");

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(studentDailyQueryService, never()).findAll(any(), any(), any());
    }

    @Test
    void 회원용_목록에서_버전_헤더가_없으면_API_001을_반환하고_조회_서비스를_호출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/7/class-sessions/student/daily?date=2026-08-17")
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(studentDailyQueryService, never()).findAll(any(), any(), any());
    }

    @Test
    void 회원용_목록에서_지원하지_않는_버전이면_API_002를_반환하고_조회_서비스를_호출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = 회원용_일별_수업_목록을_조회한다(
                7L,
                "date=2026-08-17",
                "3"
        );

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(studentDailyQueryService, never()).findAll(any(), any(), any());
    }

    @Test
    void 학생용_수업_달력을_조회하면_200과_날짜별_상태를_반환하고_조회_서비스에_위임한다() {
        // given
        LocalDate from = LocalDate.of(2026, 8, 15);
        LocalDate to = LocalDate.of(2026, 8, 19);
        when(studentCalendarQueryService.findAll(1L, 7L, from, to))
                .thenReturn(학생용_수업_달력_응답());

        // when
        RestTestClient.ResponseSpec result = 학생용_수업_달력을_조회한다(
                7L,
                "from=2026-08-15&to=2026-08-19",
                "1"
        );

        // then
        result.expectStatus().isOk().expectBody().json("""
                [
                  {
                    "date": "2026-08-16",
                    "pastReservation": true,
                    "reserved": false,
                    "waiting": false
                  },
                  {
                    "date": "2026-08-18",
                    "pastReservation": false,
                    "reserved": true,
                    "waiting": true
                  }
                ]
                """, JsonCompareMode.STRICT);
        verify(studentCalendarQueryService).findAll(1L, 7L, from, to);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_학생용_달력_쿼리")
    void 학생용_달력의_필수값이_없거나_형식이_유효하지_않으면_COMMON_001을_반환한다(String query) {
        // when
        RestTestClient.ResponseSpec result = 학생용_수업_달력을_조회한다(7L, query, "1");

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(studentCalendarQueryService, never()).findAll(any(), any(), any(), any());
    }

    @Test
    void 학생용_달력의_조회_기간_정책_예외를_COMMON_001로_직렬화한다() {
        // given
        LocalDate from = LocalDate.of(2026, 8, 19);
        LocalDate to = LocalDate.of(2026, 8, 15);
        when(studentCalendarQueryService.findAll(1L, 7L, from, to))
                .thenThrow(new ClassitdaException(CommonErrorCode.INVALID_INPUT));

        // when
        RestTestClient.ResponseSpec result = 학생용_수업_달력을_조회한다(
                7L,
                "from=2026-08-19&to=2026-08-15",
                "1"
        );

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 학생용_달력에서_버전_헤더가_없으면_API_001을_반환하고_조회_서비스를_호출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/7/class-sessions/student/calendar"
                        + "?from=2026-08-15&to=2026-08-19")
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(studentCalendarQueryService, never()).findAll(any(), any(), any(), any());
    }

    @Test
    void 학생용_달력에서_지원하지_않는_버전이면_API_002를_반환하고_조회_서비스를_호출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = 학생용_수업_달력을_조회한다(
                7L,
                "from=2026-08-15&to=2026-08-19",
                "3"
        );

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(studentCalendarQueryService, never()).findAll(any(), any(), any(), any());
    }

    @Test
    void 강사용_일별_수업_목록을_조회하면_200과_목록을_반환하고_조회_서비스에_위임한다() {
        // given
        LocalDate date = LocalDate.of(2026, 8, 17);
        when(instructorDailyQueryService.findAll(1L, 7L, date))
                .thenReturn(List.of(강사용_일별_수업_응답()));

        // when
        RestTestClient.ResponseSpec result = 강사용_일별_수업_목록을_조회한다(
                7L,
                "date=2026-08-17",
                "1"
        );

        // then
        result.expectStatus().isOk().expectBody().json("""
                [
                  {
                    "id": 11,
                    "instructorMembershipId": 12,
                    "instructorName": "김강사",
                    "classForm": "GROUP",
                    "classType": {"id": 3, "name": "요가"},
                    "className": "저녁 요가",
                    "description": "3층 A룸에서 진행합니다.",
                    "capacity": 12,
                    "reservedCount": 8,
                    "waitingCount": 2,
                    "startAt": "2026-08-17T20:00:00",
                    "endAt": "2026-08-17T21:00:00",
                    "status": "SCHEDULED_BOOKING_OPEN",
                    "mine": true
                  }
                ]
                """, JsonCompareMode.STRICT);
        verify(instructorDailyQueryService).findAll(1L, 7L, date);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_강사용_일별_목록_쿼리")
    void 강사용_일별_목록의_날짜가_유효하지_않으면_COMMON_001을_반환한다(String query) {
        // when
        RestTestClient.ResponseSpec result = 강사용_일별_수업_목록을_조회한다(7L, query, "1");

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(instructorDailyQueryService, never()).findAll(any(), any(), any());
    }

    @Test
    void 강사용_일별_목록에서_버전_헤더가_없으면_API_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/7/instructor/class-sessions/daily?date=2026-08-17")
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(instructorDailyQueryService, never()).findAll(any(), any(), any());
    }

    @Test
    void 강사용_일별_목록에서_지원하지_않는_버전이면_API_002를_반환한다() {
        // when
        RestTestClient.ResponseSpec result = 강사용_일별_수업_목록을_조회한다(
                7L,
                "date=2026-08-17",
                "3"
        );

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(instructorDailyQueryService, never()).findAll(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("강사용_일별_목록_조회_예외")
    void 강사용_일별_목록의_조회_예외를_정확한_HTTP_응답으로_직렬화한다(
            RuntimeException exception,
            int status,
            String code,
            String message
    ) {
        // given
        LocalDate date = LocalDate.of(2026, 8, 17);
        when(instructorDailyQueryService.findAll(1L, 7L, date)).thenThrow(exception);

        // when
        RestTestClient.ResponseSpec result = 강사용_일별_수업_목록을_조회한다(
                7L,
                "date=2026-08-17",
                "1"
        );

        // then
        오류를_검증한다(result, status, code, message);
    }

    @Test
    void 강사용_수업_달력을_조회하면_200과_날짜별_수업_존재_여부를_반환하고_조회_서비스에_위임한다() {
        // given
        LocalDate from = LocalDate.of(2026, 8, 15);
        LocalDate to = LocalDate.of(2026, 8, 19);
        when(instructorCalendarQueryService.findAll(1L, 7L, from, to))
                .thenReturn(강사용_수업_달력_응답());

        // when
        RestTestClient.ResponseSpec result = 강사용_수업_달력을_조회한다(
                7L,
                "from=2026-08-15&to=2026-08-19",
                "1"
        );

        // then
        result.expectStatus().isOk().expectBody().json("""
                [
                  {
                    "date": "2026-08-16",
                    "scheduled": false,
                    "completed": true,
                    "mineScheduled": false,
                    "mineCompleted": true
                  },
                  {
                    "date": "2026-08-18",
                    "scheduled": true,
                    "completed": false,
                    "mineScheduled": true,
                    "mineCompleted": false
                  }
                ]
                """, JsonCompareMode.STRICT);
        verify(instructorCalendarQueryService).findAll(1L, 7L, from, to);
    }

    @ParameterizedTest
    @MethodSource("유효하지_않은_강사용_달력_쿼리")
    void 강사용_달력의_필수_날짜가_없거나_형식이_유효하지_않으면_COMMON_001을_반환한다(String query) {
        // when
        RestTestClient.ResponseSpec result = 강사용_수업_달력을_조회한다(7L, query, "1");

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(instructorCalendarQueryService, never()).findAll(any(), any(), any(), any());
    }

    @Test
    void 강사용_달력의_조회_기간_정책_예외를_COMMON_001로_직렬화한다() {
        // given
        LocalDate from = LocalDate.of(2026, 8, 19);
        LocalDate to = LocalDate.of(2026, 8, 15);
        when(instructorCalendarQueryService.findAll(1L, 7L, from, to))
                .thenThrow(new ClassitdaException(CommonErrorCode.INVALID_INPUT));

        // when
        RestTestClient.ResponseSpec result = 강사용_수업_달력을_조회한다(
                7L,
                "from=2026-08-19&to=2026-08-15",
                "1"
        );

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 강사용_달력에서_버전_헤더가_없으면_API_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/7/instructor/class-sessions/calendar?from=2026-08-15&to=2026-08-19")
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(instructorCalendarQueryService, never()).findAll(any(), any(), any(), any());
    }

    @Test
    void 강사용_달력에서_지원하지_않는_버전이면_API_002를_반환한다() {
        // when
        RestTestClient.ResponseSpec result = 강사용_수업_달력을_조회한다(
                7L,
                "from=2026-08-15&to=2026-08-19",
                "3"
        );

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(instructorCalendarQueryService, never()).findAll(any(), any(), any(), any());
    }

    @Test
    void 상세_조회에서_버전_헤더가_없으면_API_001을_반환하고_조회_서비스를_호출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/7/class-sessions/11")
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(queryService, never()).findOne(anyLong(), anyLong(), anyLong());
    }

    @Test
    void 상세_조회에서_지원하지_않는_버전이면_API_002를_반환하고_조회_서비스를_호출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = 수업_회차_상세를_조회한다(7L, 11L, "3");

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(queryService, never()).findOne(anyLong(), anyLong(), anyLong());
    }

    @Test
    void 학생용_기존_상세_조회_경로는_노출하지_않는다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/7/class-sessions/student/11")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNotFound();
        verify(queryService, never()).findOne(anyLong(), anyLong(), anyLong());
    }

    @ParameterizedTest
    @MethodSource("수업_회차_조회_예외")
    void 조회_서비스_예외를_정확한_HTTP_응답으로_직렬화한다(
            RuntimeException exception,
            int status,
            String code,
            String message
    ) {
        // given
        when(queryService.findOne(1L, 7L, 11L)).thenThrow(exception);

        // when
        RestTestClient.ResponseSpec result = 수업_회차_상세를_조회한다(7L, 11L, "1");

        // then
        오류를_검증한다(result, status, code, message);
    }

    @Test
    void 필수_요청값이_유효하지_않으면_COMMON_001을_반환하고_명령_서비스를_호출하지_않는다() {
        // given
        ClassSessionCreateV2Request request = ClassSessionFixture.수업_회차_생성_요청(
                null, null, null, " ", 0, 0, null, null,
                null, null, null, null, null);

        // when
        RestTestClient.ResponseSpec result = V2_수업_회차를_등록한다(7L, "2", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).saveV2(anyLong(), anyLong(), any());
    }

    @Test
    void 버전_헤더가_없으면_API_001을_반환하고_명령_서비스를_호출하지_않는다() {
        // given
        ClassSessionCreateV2Request request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/7/instructor/class-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(commandService, never()).saveV2(anyLong(), anyLong(), any());
    }

    @Test
    void 지원하지_않는_버전이면_API_002를_반환하고_명령_서비스를_호출하지_않는다() {
        // given
        ClassSessionCreateV2Request request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = V2_수업_회차를_등록한다(7L, "3", request);

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(commandService, never()).saveV2(anyLong(), anyLong(), any());
    }

    @ParameterizedTest
    @MethodSource("수업_회차_생성_예외")
    void 명령_서비스_예외를_정확한_HTTP_응답으로_직렬화한다(
            RuntimeException exception,
            int status,
            String code,
            String message
    ) {
        // given
        ClassSessionCreateV2Request request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);
        doThrow(exception).when(commandService).saveV2(1L, 7L, request);

        // when
        RestTestClient.ResponseSpec result = V2_수업_회차를_등록한다(7L, "2", request);

        // then
        오류를_검증한다(result, status, code, message);
    }

    private RestTestClient.ResponseSpec V2_수업_회차를_등록한다(
            Long studioId,
            String version,
            ClassSessionCreateV2Request request
    ) {
        return client.post()
                .uri("/api/studios/{studioId}/instructor/class-sessions", studioId)
                .header("X-API-Version", version)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_회차를_수정한다(
            Long studioId,
            Long classSessionId,
            String version,
            ClassSessionUpdateV1Request request
    ) {
        return client.put()
                .uri(
                        "/api/studios/{studioId}/instructor/class-sessions/{classSessionId}",
                        studioId,
                        classSessionId
                )
                .header("X-API-Version", version)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec V2_수업_회차를_수정한다(
            Long studioId,
            Long classSessionId,
            ClassSessionUpdateV2Request request
    ) {
        return client.put()
                .uri(
                        "/api/studios/{studioId}/instructor/class-sessions/{classSessionId}",
                        studioId,
                        classSessionId
                )
                .header("X-API-Version", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_회차를_취소한다(
            Long studioId,
            Long classSessionId,
            String version
    ) {
        return client.delete()
                .uri(
                        "/api/studios/{studioId}/instructor/class-sessions/{classSessionId}",
                        studioId,
                        classSessionId
                )
                .header("X-API-Version", version)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_회차_상세를_조회한다(
            Long studioId,
            Long classSessionId,
            String version
    ) {
        return client.get()
                .uri(
                        "/api/studios/{studioId}/class-sessions/{classSessionId}",
                        studioId,
                        classSessionId
                )
                .header("X-API-Version", version)
                .exchange();
    }

    private RestTestClient.ResponseSpec 회원용_일별_수업_목록을_조회한다(
            Long studioId,
            String query,
            String version
    ) {
        return client.get()
                .uri("/api/studios/%d/class-sessions/student/daily?%s".formatted(studioId, query))
                .header("X-API-Version", version)
                .exchange();
    }

    private RestTestClient.ResponseSpec 강사용_일별_수업_목록을_조회한다(
            Long studioId,
            String query,
            String version
    ) {
        return client.get()
                .uri("/api/studios/%d/instructor/class-sessions/daily?%s".formatted(studioId, query))
                .header("X-API-Version", version)
                .exchange();
    }

    private RestTestClient.ResponseSpec 학생용_수업_달력을_조회한다(
            Long studioId,
            String query,
            String version
    ) {
        return client.get()
                .uri("/api/studios/%d/class-sessions/student/calendar?%s".formatted(studioId, query))
                .header("X-API-Version", version)
                .exchange();
    }

    private RestTestClient.ResponseSpec 강사용_수업_달력을_조회한다(
            Long studioId,
            String query,
            String version
    ) {
        return client.get()
                .uri("/api/studios/%d/instructor/class-sessions/calendar?%s".formatted(studioId, query))
                .header("X-API-Version", version)
                .exchange();
    }

    private void 오류를_검증한다(
            RestTestClient.ResponseSpec result,
            int status,
            String code,
            String message
    ) {
        result.expectStatus().isEqualTo(status).expectBody().json("""
                {"code":"%s","message":"%s"}
                """.formatted(code, message), JsonCompareMode.STRICT);
    }

    private static Stream<Arguments> 수업_회차_생성_예외() {
        return Stream.of(
                Arguments.of(
                        new StudioException(StudioErrorCode.PERMISSION_DENIED),
                        403,
                        "PERMISSION-001",
                        "이 작업을 수행할 권한이 없습니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.NOT_FOUND),
                        404,
                        "STUDIO-002",
                        "시설을 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND),
                        404,
                        "CLASS_TYPE-003",
                        "수업 종류를 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND),
                        404,
                        "CLASS_SESSION-017",
                        "담당 가능한 강사 소속을 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE),
                        400,
                        "CLASS_SESSION-009",
                        "반복 여부에 맞는 수업 일정 정보가 필요합니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_SESSION_TIME_CONFLICT),
                        409,
                        "CLASS_SESSION-015",
                        "담당 강사의 기존 수업과 시간이 겹칩니다."
                )
        );
    }

    private static Stream<Arguments> 수업_회차_조회_예외() {
        return Stream.of(
                Arguments.of(
                        new StudioException(StudioErrorCode.NOT_MEMBERSHIP),
                        403,
                        "MEMBERSHIP-001",
                        "해당 시설의 소속이 아닙니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.MEMBERSHIP_INACTIVE),
                        403,
                        "MEMBERSHIP-002",
                        "이용이 정지된 소속입니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.NOT_FOUND),
                        404,
                        "STUDIO-002",
                        "시설을 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND),
                        404,
                        "CLASS_SESSION-014",
                        "수업 회차를 찾을 수 없습니다."
                )
        );
    }

    private static Stream<Arguments> 유효하지_않은_회원용_목록_쿼리() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("date=invalid")
        );
    }

    private static Stream<Arguments> 유효하지_않은_강사용_일별_목록_쿼리() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("date=invalid")
        );
    }

    private static Stream<Arguments> 유효하지_않은_학생용_달력_쿼리() {
        return Stream.of(
                Arguments.of("to=2026-08-19"),
                Arguments.of("from=2026-08-15"),
                Arguments.of("from=invalid&to=2026-08-19"),
                Arguments.of("from=2026-08-15&to=invalid")
        );
    }

    private static Stream<Arguments> 유효하지_않은_강사용_달력_쿼리() {
        return Stream.of(
                Arguments.of("to=2026-08-19"),
                Arguments.of("from=2026-08-15"),
                Arguments.of("from=invalid&to=2026-08-19"),
                Arguments.of("from=2026-08-15&to=invalid")
        );
    }

    private static Stream<Arguments> 강사용_일별_목록_조회_예외() {
        return Stream.of(
                Arguments.of(
                        new StudioException(StudioErrorCode.NOT_MEMBERSHIP),
                        403,
                        "MEMBERSHIP-001",
                        "해당 시설의 소속이 아닙니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.MEMBERSHIP_INACTIVE),
                        403,
                        "MEMBERSHIP-002",
                        "이용이 정지된 소속입니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.PERMISSION_DENIED),
                        403,
                        "PERMISSION-001",
                        "이 작업을 수행할 권한이 없습니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.NOT_FOUND),
                        404,
                        "STUDIO-002",
                        "시설을 찾을 수 없습니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.POLICY_NOT_FOUND),
                        404,
                        "POLICY-001",
                        "운영 정책을 찾을 수 없습니다."
                )
        );
    }

    private static ClassSessionDetailResponse 수업_회차_상세_응답() {
        return new ClassSessionDetailResponse(
                11L,
                12L,
                "김강사",
                ClassForm.GROUP,
                ClassTypeResponse.of(3L, "요가"),
                "저녁 요가",
                "편한 복장과 개인 수건을 준비해 주세요.",
                12,
                60,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                LocalDateTime.of(2026, 8, 17, 21, 0),
                SessionPhase.SCHEDULED
        );
    }

    private static StudentDailySessionView 회원용_일별_수업_뷰() {
        return new StudentDailySessionView(
                11L,
                19L,
                12L,
                "김강사",
                ClassForm.GROUP,
                3L,
                "요가",
                "저녁 요가",
                "3층 A룸에서 진행합니다.",
                12,
                8,
                4,
                2,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                LocalDateTime.of(2026, 8, 17, 21, 0),
                new StudentBookingDecision(
                        StudentBookingRelation.RESERVED,
                        AttendanceResult.NOT_RECORDED,
                        Optional.empty()
                )
        );
    }

    private static InstructorDailySessionView 강사용_일별_수업_응답() {
        return new InstructorDailySessionView(
                11L,
                12L,
                "김강사",
                ClassForm.GROUP,
                3L,
                "요가",
                "저녁 요가",
                "3층 A룸에서 진행합니다.",
                12,
                8,
                2,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                LocalDateTime.of(2026, 8, 17, 21, 0),
                InstructorSessionStatus.SCHEDULED_BOOKING_OPEN,
                true
        );
    }

    private static List<StudentCalendarSummary> 학생용_수업_달력_응답() {
        return List.of(
                StudentCalendarSummary.of(
                        LocalDate.of(2026, 8, 16),
                        true,
                        false,
                        false
                ),
                StudentCalendarSummary.of(
                        LocalDate.of(2026, 8, 18),
                        false,
                        true,
                        true
                )
        );
    }

    private static List<InstructorCalendarSummary> 강사용_수업_달력_응답() {
        return List.of(
                new InstructorCalendarSummary(
                        LocalDate.of(2026, 8, 16),
                        false,
                        true,
                        false,
                        true
                ),
                new InstructorCalendarSummary(
                        LocalDate.of(2026, 8, 18),
                        true,
                        false,
                        true,
                        false
                )
        );
    }
}
