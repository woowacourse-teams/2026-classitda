package com.classitda.member.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.infra.security.AuthenticationErrorHandler;
import com.classitda.authentication.infra.security.SecurityConfig;
import com.classitda.authentication.infra.security.jwt.JwtAuthenticationConverter;
import com.classitda.authentication.presentation.config.AuthenticationWebMvcConfig;
import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.member.application.MemberService;
import java.time.Instant;
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
        MemberWithdrawalSecurityTest.TestSecurityConfiguration.class
})
@AutoConfigureRestTestClient
@WebMvcTest(MemberController.class)
class MemberWithdrawalSecurityTest {

    private final RestTestClient client;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    MemberWithdrawalSecurityTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 인증이_없으면_탈퇴할_수_없다() {
        // when
        RestTestClient.ResponseSpec result = withdraw(null);

        // then
        assertAuthError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(memberService);
    }

    @Test
    void 가입_토큰으로는_탈퇴할_수_없다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("1", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = withdraw("signup-token");

        // then
        assertAuthError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(memberService);
    }

    @Test
    void 액세스_토큰으로_탈퇴할_수_있다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("1", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = withdraw("access-token");

        // then
        result.expectStatus().isNoContent();
        verify(memberService).withdraw(1L);
    }

    private RestTestClient.ResponseSpec withdraw(String token) {
        RestTestClient.RequestHeadersSpec<?> request = client.delete()
                .uri("/api/members/me")
                .header("X-API-Version", "1");
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }
        return request.exchange();
    }

    private void assertAuthError(
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
