package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailQueryService;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailStatus;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailView;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@Import({ApiVersionConfig.class, GlobalExceptionHandler.class})
@WebMvcTest(StudentEnrollmentController.class)
class StudentEnrollmentControllerTest {

    private static final String DETAIL_URI =
            "/api/studios/7/class-session-enrollments/19";

    private final RestTestClient client;

    @MockitoBean
    private StudentEnrollmentDetailQueryService queryService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    StudentEnrollmentControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 학생_신청_상세를_조회하면_200과_중첩된_상세_응답을_반환한다() {
        // given
        when(queryService.findOne(1L, 7L, 19L)).thenReturn(제안_상세_뷰());

        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다("1");

        // then
        result.expectStatus().isOk().expectBody().json("""
                {
                  "id": 19,
                  "status": "OFFERED",
                  "createdAt": "2026-08-06T15:47:00",
                  "statusChangedAt": "2026-08-06T15:47:00",
                  "attendanceRecordedAt": null,
                  "waitingPosition": 0,
                  "offerExpiresAt": "2026-08-06T16:47:00",
                  "classSession": {
                    "id": 117,
                    "name": "리포머 베이직",
                    "description": "오늘 양말 꼭 챙겨오세요~",
                    "startAt": "2026-08-12T11:00:00",
                    "endAt": "2026-08-12T11:50:00",
                    "canceledAt": null
                  },
                  "usedPass": null,
                  "instructor": {
                    "membershipId": 3,
                    "name": "박소연 강사",
                    "profileImageUrl": null,
                    "studioName": "클래스잇다 금토동지점"
                  }
                }
                """, JsonCompareMode.STRICT);
        verify(queryService).findOne(1L, 7L, 19L);
    }

    @Test
    void 수강권이_연결된_신청은_사용_수강권을_반환한다() {
        // given
        when(queryService.findOne(1L, 7L, 19L)).thenReturn(예약_상세_뷰());

        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다("1");

        // then
        result.expectStatus().isOk().expectBody().jsonPath("$.usedPass").isEqualTo(
                java.util.Map.of(
                        "id", 42,
                        "name", "[8:1] 그룹 레슨 20회권",
                        "startedAt", "2026-06-30",
                        "expiresAt", "2026-08-20",
                        "remainingCount", 14
                )
        );
    }

    @ParameterizedTest
    @MethodSource("조회_서비스_오류")
    void 조회_서비스_오류를_기존_HTTP_오류로_반환한다(
            ClassitdaException exception,
            int status,
            String code,
            String message
    ) {
        // given
        when(queryService.findOne(1L, 7L, 19L)).thenThrow(exception);

        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다("1");

        // then
        오류를_검증한다(result, status, code, message);
    }

    @Test
    void API_버전_헤더가_없으면_400을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(DETAIL_URI)
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verifyNoInteractions(queryService);
    }

    @Test
    void 지원하지_않는_API_버전이면_400을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다("2");

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verifyNoInteractions(queryService);
    }

    private RestTestClient.ResponseSpec 상세를_조회한다(String apiVersion) {
        return client.get()
                .uri(DETAIL_URI)
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

    private static Stream<Arguments> 조회_서비스_오류() {
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
                        new ClassException(ClassErrorCode.CLASS_SESSION_ENROLLMENT_NOT_FOUND),
                        404,
                        "CLASS_SESSION_ENROLLMENT-009",
                        "수업 신청을 찾을 수 없습니다."
                )
        );
    }

    private StudentEnrollmentDetailView 제안_상세_뷰() {
        return 상세_뷰(
                StudentEnrollmentDetailStatus.OFFERED,
                0L,
                LocalDateTime.of(2026, 8, 6, 16, 47),
                null
        );
    }

    private StudentEnrollmentDetailView 예약_상세_뷰() {
        return 상세_뷰(
                StudentEnrollmentDetailStatus.RESERVED,
                null,
                null,
                new StudentEnrollmentDetailView.UsedPass(
                        42L,
                        "[8:1] 그룹 레슨 20회권",
                        LocalDate.of(2026, 6, 30),
                        LocalDate.of(2026, 8, 20),
                        14
                )
        );
    }

    private StudentEnrollmentDetailView 상세_뷰(
            StudentEnrollmentDetailStatus status,
            Long waitingPosition,
            LocalDateTime offerExpiresAt,
            StudentEnrollmentDetailView.UsedPass usedPass
    ) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 6, 15, 47);
        return new StudentEnrollmentDetailView(
                19L,
                status,
                createdAt,
                createdAt,
                null,
                waitingPosition,
                offerExpiresAt,
                new StudentEnrollmentDetailView.ClassSessionDetails(
                        117L,
                        "리포머 베이직",
                        "오늘 양말 꼭 챙겨오세요~",
                        LocalDateTime.of(2026, 8, 12, 11, 0),
                        LocalDateTime.of(2026, 8, 12, 11, 50),
                        null
                ),
                usedPass,
                new StudentEnrollmentDetailView.Instructor(
                        3L,
                        "박소연 강사",
                        null,
                        "클래스잇다 금토동지점"
                )
        );
    }
}
