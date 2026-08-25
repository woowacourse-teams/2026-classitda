package com.classitda.classes.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.infra.security.AuthenticationErrorHandler;
import com.classitda.authentication.infra.security.SecurityConfig;
import com.classitda.authentication.infra.security.jwt.JwtAuthenticationConverter;
import com.classitda.authentication.presentation.config.AuthenticationWebMvcConfig;
import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.application.ClassSessionQueryService;
import com.classitda.classes.application.instructor.calendar.InstructorCalendarQueryService;
import com.classitda.classes.application.instructor.daily.InstructorDailyQueryService;
import com.classitda.classes.application.student.calendar.StudentCalendarQueryService;
import com.classitda.classes.application.student.daily.StudentDailyQueryService;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV1Request;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@Import({
        ApiVersionConfig.class,
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        AuthenticationErrorHandler.class,
        JwtAuthenticationConverter.class,
        CurrentMemberIdArgumentResolver.class,
        AuthenticationWebMvcConfig.class,
        ClassSessionSecurityTest.TestSecurityConfiguration.class
})
@AutoConfigureRestTestClient
@WebMvcTest(ClassSessionController.class)
class ClassSessionSecurityTest {

    private static final String LIST_URI =
            "/api/studios/7/class-sessions/student/daily?date=2026-08-17";
    private static final String INSTRUCTOR_DAILY_URI =
            "/api/studios/7/class-sessions/instructor/daily?date=2026-08-17";
    private static final String STUDENT_CALENDAR_URI =
            "/api/studios/7/class-sessions/student/calendar"
                    + "?from=2026-08-15&to=2026-08-19";
    private static final String DETAIL_URI =
            "/api/studios/7/class-sessions/11";
    private static final String UPDATE_URI = DETAIL_URI;
    private static final String CANCEL_URI = DETAIL_URI;
    private static final String INSTRUCTOR_CALENDAR_URI =
            "/api/studios/7/class-sessions/instructor/calendar?from=2026-08-15&to=2026-08-19";

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
    private JwtDecoder jwtDecoder;

    @Autowired
    ClassSessionSecurityTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 인증이_없으면_회원용_수업_목록을_조회할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(LIST_URI)
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(studentDailyQueryService);
    }

    @Test
    void 가입_토큰으로는_회원용_수업_목록을_조회할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(LIST_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(studentDailyQueryService);
    }

    @Test
    void 액세스_토큰으로_회원용_수업_목록을_조회할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(LIST_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .exchange();

        // then
        result.expectStatus().isOk().expectBody().json("[]", JsonCompareMode.STRICT);
        verify(studentDailyQueryService).findAll(
                1L,
                7L,
                LocalDate.of(2026, 8, 17)
        );
    }

    @Test
    void 인증이_없으면_학생용_수업_달력을_조회할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(STUDENT_CALENDAR_URI)
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(studentCalendarQueryService);
    }

    @Test
    void 가입_토큰으로는_학생용_수업_달력을_조회할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(STUDENT_CALENDAR_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(studentCalendarQueryService);
    }

    @Test
    void 액세스_토큰으로_학생용_수업_달력을_조회할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(STUDENT_CALENDAR_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .exchange();

        // then
        result.expectStatus().isOk().expectBody().json("[]", JsonCompareMode.STRICT);
        verify(studentCalendarQueryService).findAll(
                1L,
                7L,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 19)
        );
    }

    @Test
    void 인증이_없으면_수업_회차_상세를_조회할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(DETAIL_URI)
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(queryService);
    }

    @Test
    void 가입_토큰으로는_수업_회차_상세를_조회할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(DETAIL_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(queryService);
    }

    @Test
    void 액세스_토큰으로_수업_회차_상세를_조회할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(DETAIL_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .exchange();

        // then
        result.expectStatus().isOk();
        verify(queryService).findOne(1L, 7L, 11L);
    }

    @Test
    void 인증이_없으면_수업_회차를_수정할_수_없다() {
        // given
        ClassSessionUpdateV1Request request = ClassSessionFixture.기본_수업_회차_수정_요청(3L);

        // when
        RestTestClient.ResponseSpec result = client.put()
                .uri(UPDATE_URI)
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(commandService);
    }

    @Test
    void 가입_토큰으로는_수업_회차를_수정할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        ClassSessionUpdateV1Request request = ClassSessionFixture.기본_수업_회차_수정_요청(3L);

        // when
        RestTestClient.ResponseSpec result = client.put()
                .uri(UPDATE_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(commandService);
    }

    @Test
    void 액세스_토큰으로_수업_회차를_수정할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));
        ClassSessionUpdateV1Request request = ClassSessionFixture.기본_수업_회차_수정_요청(3L);

        // when
        RestTestClient.ResponseSpec result = client.put()
                .uri(UPDATE_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isNoContent();
        verify(commandService).updateV1(1L, 7L, 11L, request);
    }

    @Test
    void 인증이_없으면_수업_회차를_취소할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri(CANCEL_URI)
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(commandService);
    }

    @Test
    void 가입_토큰으로는_수업_회차를_취소할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri(CANCEL_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(commandService);
    }

    @Test
    void 액세스_토큰으로_수업_회차를_취소할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri(CANCEL_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .exchange();

        // then
        result.expectStatus().isNoContent();
        verify(commandService).cancel(1L, 7L, 11L);
    }

    @Test
    void 인증이_없으면_강사용_일별_수업_목록을_조회할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(INSTRUCTOR_DAILY_URI)
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(instructorDailyQueryService);
    }

    @Test
    void 가입_토큰으로는_강사용_일별_수업_목록을_조회할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(INSTRUCTOR_DAILY_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(instructorDailyQueryService);
    }

    @Test
    void 액세스_토큰으로_강사용_일별_수업_목록을_조회할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(INSTRUCTOR_DAILY_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .exchange();

        // then
        result.expectStatus().isOk().expectBody().json("[]", JsonCompareMode.STRICT);
        verify(instructorDailyQueryService).findAll(
                1L,
                7L,
                LocalDate.of(2026, 8, 17)
        );
    }

    @Test
    void 인증이_없으면_강사용_수업_달력을_조회할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(INSTRUCTOR_CALENDAR_URI)
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(instructorCalendarQueryService);
    }

    @Test
    void 가입_토큰으로는_강사용_수업_달력을_조회할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(INSTRUCTOR_CALENDAR_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(instructorCalendarQueryService);
    }

    @Test
    void 액세스_토큰으로_강사용_수업_달력을_조회할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri(INSTRUCTOR_CALENDAR_URI)
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .exchange();

        // then
        result.expectStatus().isOk().expectBody().json("[]", JsonCompareMode.STRICT);
        verify(instructorCalendarQueryService).findAll(
                1L,
                7L,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 19)
        );
    }

    private void assertError(
            RestTestClient.ResponseSpec response,
            int status,
            String code,
            String message
    ) {
        response.expectStatus().isEqualTo(status)
                .expectBody()
                .json("""
                        {"code":"%s","message":"%s"}
                        """.formatted(code, message), JsonCompareMode.STRICT);
    }

    private Jwt jwt(String subject, TokenUse tokenUse) {
        Instant issuedAt = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(subject)
                .claim("jti", subject)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(1800))
                .claim("token_use", tokenUse.name())
                .build();
    }

    @EnableWebSecurity
    @TestConfiguration(proxyBeanMethods = false)
    static class TestSecurityConfiguration {
    }
}
