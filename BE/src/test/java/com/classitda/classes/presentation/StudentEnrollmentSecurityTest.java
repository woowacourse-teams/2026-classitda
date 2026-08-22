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
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailQueryService;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailStatus;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailView;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
        StudentEnrollmentSecurityTest.TestSecurityConfiguration.class
})
@AutoConfigureRestTestClient
@WebMvcTest(StudentEnrollmentController.class)
class StudentEnrollmentSecurityTest {

    private static final String DETAIL_URI =
            "/api/studios/7/class-session-enrollments/19";

    private final RestTestClient client;

    @MockitoBean
    private StudentEnrollmentDetailQueryService queryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    StudentEnrollmentSecurityTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 인증이_없으면_학생_신청_상세를_조회할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다(null);

        // then
        오류를_검증한다(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(queryService);
    }

    @Test
    void 가입_토큰으로는_학생_신청_상세를_조회할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다("signup-token");

        // then
        오류를_검증한다(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(queryService);
    }

    @Test
    void 액세스_토큰으로_학생_신청_상세를_조회할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));
        given(queryService.findOne(1L, 7L, 19L)).willReturn(상세_뷰());

        // when
        RestTestClient.ResponseSpec result = 상세를_조회한다("access-token");

        // then
        result.expectStatus().isOk();
        verify(queryService).findOne(1L, 7L, 19L);
    }

    private RestTestClient.ResponseSpec 상세를_조회한다(String token) {
        RestTestClient.RequestHeadersSpec<?> request = client.get()
                .uri(DETAIL_URI)
                .header("X-API-Version", "1");
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }
        return request.exchange();
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

    private StudentEnrollmentDetailView 상세_뷰() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 6, 15, 47);
        return new StudentEnrollmentDetailView(
                19L,
                StudentEnrollmentDetailStatus.OFFERED,
                createdAt,
                createdAt,
                null,
                0L,
                createdAt.plusHours(1),
                new StudentEnrollmentDetailView.ClassSessionDetails(
                        117L,
                        "리포머 베이직",
                        null,
                        LocalDateTime.of(2026, 8, 12, 11, 0),
                        LocalDateTime.of(2026, 8, 12, 11, 50),
                        null
                ),
                null,
                new StudentEnrollmentDetailView.Instructor(
                        3L,
                        "박소연 강사",
                        null,
                        "클래스잇다 금토동지점"
                )
        );
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
