package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.student.BookingAvailability;
import com.classitda.classes.application.student.StudentBookingDecision;
import com.classitda.classes.application.student.StudentBookingRelation;
import com.classitda.classes.application.student.calendar.StudentCalendarQueryService;
import com.classitda.classes.application.student.daily.StudentDailyQueryService;
import com.classitda.classes.application.student.daily.StudentDailySessionView;
import com.classitda.classes.application.student.detail.StudentSessionDetailQueryService;
import com.classitda.classes.application.student.detail.StudentSessionDetailView;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@Import({ApiVersionConfig.class, GlobalExceptionHandler.class})
@WebMvcTest(StudentSessionController.class)
class StudentSessionControllerTest {

    private static final String DETAIL_URI =
            "/api/studios/7/student/class-sessions/117";

    private final RestTestClient client;

    @MockitoBean
    private StudentSessionDetailQueryService detailQueryService;

    @MockitoBean
    private StudentDailyQueryService dailyQueryService;

    @MockitoBean
    private StudentCalendarQueryService calendarQueryService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    StudentSessionControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 활성_신청이_없으면_예약_가능한_학생용_수업_상세를_반환한다() {
        // given
        when(detailQueryService.findOne(1L, 7L, 117L)).thenReturn(상세_뷰(
                null,
                new StudentBookingDecision(
                        StudentBookingRelation.NONE,
                        AttendanceResult.NOT_RECORDED,
                        Optional.of(BookingAvailability.RESERVABLE)
                )
        ));

        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다();

        // then
        result.expectStatus().isOk().expectBody().json("""
                {
                  "id":117,
                  "enrollment":null,
                  "instructor":{
                    "membershipId":3,
                    "name":"박소연 강사",
                    "profileImageUrl":"https://images.example.com/instructor.png",
                    "studioName":"클래스잇다 금토동지점"
                  },
                  "classForm":"GROUP",
                  "classType":{"id":5,"name":"리포머"},
                  "className":"리포머 베이직",
                  "description":"오늘 양말 꼭 챙겨오세요~",
                  "capacity":8,
                  "reservedCount":3,
                  "remainingCapacity":5,
                  "waitingCount":0,
                  "startAt":"2026-08-12T11:00:00",
                  "endAt":"2026-08-12T11:50:00",
                  "bookingRelation":"NONE",
                  "attendanceResult":"NOT_RECORDED",
                  "availability":"RESERVABLE"
                }
                """, JsonCompareMode.STRICT);
        verify(detailQueryService).findOne(1L, 7L, 117L);
    }

    @Test
    void 활성_신청이_있으면_신청_정보를_반환하고_예약_가능_상태는_반환하지_않는다() {
        // given
        StudentSessionDetailView.Enrollment enrollment = new StudentSessionDetailView.Enrollment(
                19L,
                LocalDateTime.of(2026, 8, 6, 15, 47),
                LocalDateTime.of(2026, 8, 6, 15, 47),
                null,
                1L,
                null,
                null
        );
        when(detailQueryService.findOne(1L, 7L, 117L)).thenReturn(상세_뷰(
                enrollment,
                new StudentBookingDecision(
                        StudentBookingRelation.WAITING,
                        AttendanceResult.NOT_RECORDED,
                        Optional.empty()
                )
        ));

        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.enrollment.id").isEqualTo(19)
                .jsonPath("$.enrollment.waitingPosition").isEqualTo(1)
                .jsonPath("$.bookingRelation").isEqualTo("WAITING")
                .jsonPath("$.availability").doesNotExist();
    }

    private RestTestClient.ResponseSpec 상세를_조회한다() {
        return client.get()
                .uri(DETAIL_URI)
                .header("X-API-Version", "1")
                .exchange();
    }

    private StudentSessionDetailView 상세_뷰(
            StudentSessionDetailView.Enrollment enrollment,
            StudentBookingDecision bookingDecision
    ) {
        return new StudentSessionDetailView(
                new StudentDailySessionView(
                        117L,
                        enrollment == null ? null : enrollment.id(),
                        3L,
                        "박소연 강사",
                        ClassForm.GROUP,
                        5L,
                        "리포머",
                        "리포머 베이직",
                        "오늘 양말 꼭 챙겨오세요~",
                        8,
                        3,
                        5,
                        0,
                        LocalDateTime.of(2026, 8, 12, 11, 0),
                        LocalDateTime.of(2026, 8, 12, 11, 50),
                        bookingDecision
                ),
                enrollment,
                new StudentSessionDetailView.Instructor(
                        3L,
                        "박소연 강사",
                        "https://images.example.com/instructor.png",
                        "클래스잇다 금토동지점"
                )
        );
    }
}
