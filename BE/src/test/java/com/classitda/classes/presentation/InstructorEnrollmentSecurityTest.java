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
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.enrollment.ClassSessionInstructorEnrollmentCommandService;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionDetailQueryService;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionDetailView;
import com.classitda.classes.domain.ClassForm;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
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
        InstructorEnrollmentSecurityTest.TestSecurityConfiguration.class
})
@AutoConfigureRestTestClient
@WebMvcTest(InstructorEnrollmentController.class)
class InstructorEnrollmentSecurityTest {

    private static final String ROSTER_URI =
            "/api/studios/7/class-sessions/instructor/10";

    private final RestTestClient client;

    @MockitoBean
    private ClassSessionInstructorEnrollmentCommandService commandService;

    @MockitoBean
    private InstructorSessionDetailQueryService queryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    InstructorEnrollmentSecurityTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 인증이_없으면_강사용_수업_상세를_조회할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = 명단을_조회한다(null);

        // then
        오류를_검증한다(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(queryService);
    }

    @Test
    void 가입_토큰으로는_강사용_수업_상세를_조회할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = 명단을_조회한다("signup-token");

        // then
        오류를_검증한다(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(queryService);
    }

    @Test
    void 액세스_토큰으로_강사용_수업_상세를_조회할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));
        given(queryService.findOne(1L, 7L, 10L)).willReturn(상세_뷰());

        // when
        RestTestClient.ResponseSpec result = 명단을_조회한다("access-token");

        // then
        result.expectStatus().isOk();
        verify(queryService).findOne(1L, 7L, 10L);
    }

    private RestTestClient.ResponseSpec 명단을_조회한다(String token) {
        RestTestClient.RequestHeadersSpec<?> request = client.get()
                .uri(ROSTER_URI)
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
                0,
                LocalDateTime.of(2026, 8, 17, 12, 0),
                LocalDateTime.of(2026, 8, 17, 13, 0),
                InstructorSessionStatus.SCHEDULED_BOOKING_OPEN,
                true,
                List.of()
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
