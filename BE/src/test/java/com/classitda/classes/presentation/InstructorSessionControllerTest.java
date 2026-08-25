package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.calendar.InstructorCalendarQueryService;
import com.classitda.classes.application.instructor.daily.InstructorDailyQueryService;
import com.classitda.classes.application.instructor.enrollment.ClassSessionInstructorEnrollmentCommandService;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionDetailQueryService;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionDetailView;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.InstructorEnrollmentCreateRequest;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
@WebMvcTest(InstructorSessionController.class)
class InstructorSessionControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private ClassSessionCommandService classSessionCommandService;

    @MockitoBean
    private InstructorDailyQueryService instructorDailyQueryService;

    @MockitoBean
    private InstructorCalendarQueryService instructorCalendarQueryService;

    @MockitoBean
    private ClassSessionInstructorEnrollmentCommandService commandService;

    @MockitoBean
    private InstructorSessionDetailQueryService queryService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    InstructorSessionControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 강사용_수업_상세와_예약_회원_명단을_조회하면_200과_응답을_반환한다() {
        // given
        when(queryService.findOne(1L, 7L, 10L)).thenReturn(상세_뷰());

        // when
        RestTestClient.ResponseSpec result = 명단을_조회한다(7L, 10L, "1");

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "id":10,
                          "instructorMembershipId":12,
                          "instructorName":"이지은 강사",
                          "classForm":"GROUP",
                          "classType":{"id":3,"name":"리포머"},
                          "className":"리포머 밸런스",
                          "description":"체어룸에서 진행",
                          "capacity":8,
                          "reservedCount":1,
                          "startAt":"2026-08-17T12:00:00",
                          "endAt":"2026-08-17T13:00:00",
                          "status":"SCHEDULED_BOOKING_OPEN",
                          "mine":true,
                          "reservedMembers":[{
                            "enrollmentId":101,
                            "membershipId":31,
                            "name":"김민지",
                            "profileImageUrl":"https://images.example.com/minji.png"
                          }]
                        }
                        """, JsonCompareMode.STRICT);
        verify(queryService).findOne(1L, 7L, 10L);
    }

    @Test
    void 조회할_수_없는_수업이면_404를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND))
                .when(queryService).findOne(any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 명단을_조회한다(7L, 10L, "1");

        // then
        오류를_검증한다(result, 404, "CLASS_SESSION-014", "수업 회차를 찾을 수 없습니다.");
    }

    @Test
    void 예약_조회_권한이_없으면_403을_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED))
                .when(queryService).findOne(any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 명단을_조회한다(7L, 10L, "1");

        // then
        오류를_검증한다(result, 403, "PERMISSION-001", "이 작업을 수행할 권한이 없습니다.");
    }

    @Test
    void 회원을_예약하면_201과_빈_본문을_반환하고_서비스에_위임한다() {
        // given
        InstructorEnrollmentCreateRequest request = InstructorEnrollmentCreateRequest.from(12L);

        // when
        RestTestClient.ResponseSpec result = 예약한다(7L, 10L, "1", request);

        // then
        result.expectStatus().isCreated()
                .expectBody().isEmpty();
        verify(commandService).save(eq(1L), eq(7L), eq(10L), eq(12L));
    }

    @Test
    void 회원_소속_아이디가_없으면_COMMON_001을_반환한다() {
        // given
        InstructorEnrollmentCreateRequest request = InstructorEnrollmentCreateRequest.from(null);

        // when
        RestTestClient.ResponseSpec result = 예약한다(7L, 10L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).save(any(), any(), any(), any());
    }

    @Test
    void 정원이_모두_차면_409와_정원_초과_코드를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.ENROLLMENT_CAPACITY_EXCEEDED))
                .when(commandService).save(any(), any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 예약한다(7L, 10L, "1", InstructorEnrollmentCreateRequest.from(12L));

        // then
        오류를_검증한다(result, 409, "CLASS_SESSION_ENROLLMENT-012", "수업 회차의 정원이 모두 찼습니다.");
    }

    @Test
    void 이미_신청한_회원이면_409와_중복_신청_코드를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.ENROLLMENT_ALREADY_EXISTS))
                .when(commandService).save(any(), any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 예약한다(7L, 10L, "1", InstructorEnrollmentCreateRequest.from(12L));

        // then
        오류를_검증한다(result, 409, "CLASS_SESSION_ENROLLMENT-013", "이미 해당 수업 회차에 신청한 회원입니다.");
    }

    @Test
    void 예약할_회원_소속이_없으면_404를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.ENROLLMENT_MEMBER_NOT_FOUND))
                .when(commandService).save(any(), any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 예약한다(7L, 10L, "1", InstructorEnrollmentCreateRequest.from(12L));

        // then
        오류를_검증한다(result, 404, "CLASS_SESSION_ENROLLMENT-010", "예약할 회원 소속을 찾을 수 없습니다.");
    }

    @Test
    void 예약_관리_권한이_없으면_403을_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED))
                .when(commandService).save(any(), any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 예약한다(7L, 10L, "1", InstructorEnrollmentCreateRequest.from(12L));

        // then
        오류를_검증한다(result, 403, "PERMISSION-001", "이 작업을 수행할 권한이 없습니다.");
    }

    @Test
    void 예약을_취소하면_204와_빈_본문을_반환하고_서비스에_위임한다() {
        // when
        RestTestClient.ResponseSpec result = 예약을_취소한다(7L, 10L, 100L, "1");

        // then
        result.expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(commandService).cancel(eq(1L), eq(7L), eq(10L), eq(100L));
    }

    @Test
    void 취소할_예약이_없으면_404를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.CLASS_SESSION_ENROLLMENT_NOT_FOUND))
                .when(commandService).cancel(any(), any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 예약을_취소한다(7L, 10L, 100L, "1");

        // then
        오류를_검증한다(result, 404, "CLASS_SESSION_ENROLLMENT-009", "수업 신청을 찾을 수 없습니다.");
    }

    @Test
    void 예약_상태가_아니면_409를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION))
                .when(commandService).cancel(any(), any(), any(), any());

        // when
        RestTestClient.ResponseSpec result = 예약을_취소한다(7L, 10L, 100L, "1");

        // then
        오류를_검증한다(
                result,
                409,
                "CLASS_SESSION_ENROLLMENT-001",
                "현재 수업 신청 상태에서는 요청한 상태 전이를 수행할 수 없습니다."
        );
    }

    private RestTestClient.ResponseSpec 예약한다(
            Long studioId,
            Long classSessionId,
            String apiVersion,
            InstructorEnrollmentCreateRequest request
    ) {
        return client.post()
                .uri(
                        "/api/studios/{studioId}/instructor/class-sessions/{classSessionId}/enrollments",
                        studioId,
                        classSessionId
                )
                .header("X-API-Version", apiVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 명단을_조회한다(
            Long studioId,
            Long classSessionId,
            String apiVersion
    ) {
        return client.get()
                .uri(
                        "/api/studios/{studioId}/instructor/class-sessions/{classSessionId}",
                        studioId,
                        classSessionId
                )
                .header("X-API-Version", apiVersion)
                .exchange();
    }

    private RestTestClient.ResponseSpec 예약을_취소한다(
            Long studioId,
            Long classSessionId,
            Long enrollmentId,
            String apiVersion
    ) {
        return client.delete()
                .uri(
                        "/api/studios/{studioId}/instructor/class-sessions/{classSessionId}/enrollments/{enrollmentId}",
                        studioId,
                        classSessionId,
                        enrollmentId
                )
                .header("X-API-Version", apiVersion)
                .exchange();
    }

    private void 오류를_검증한다(
            RestTestClient.ResponseSpec result,
            int status,
            String code,
            String message
    ) {
        result.expectStatus().isEqualTo(status)
                .expectBody()
                .json("""
                        {"code":"%s","message":"%s"}
                        """.formatted(code, message), JsonCompareMode.STRICT);
    }

    private InstructorSessionDetailView 상세_뷰() {
        return new InstructorSessionDetailView(
                10L,
                12L,
                "이지은 강사",
                ClassForm.GROUP,
                3L,
                "리포머",
                "리포머 밸런스",
                "체어룸에서 진행",
                8,
                1,
                LocalDateTime.of(2026, 8, 17, 12, 0),
                LocalDateTime.of(2026, 8, 17, 13, 0),
                InstructorSessionStatus.SCHEDULED_BOOKING_OPEN,
                true,
                List.of(new InstructorSessionDetailView.ReservedMember(
                        101L,
                        31L,
                        "김민지",
                        "https://images.example.com/minji.png"
                ))
        );
    }
}
