package com.classitda.authentication.presentation.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.authentication.presentation.config.AuthenticationWebMvcConfig;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

@ExtendWith(MockitoExtension.class)
class CurrentMemberIdArgumentResolverTest {

    private static final String INTERNAL_FAILURE_MESSAGE = "인증 회원 정보를 확인할 수 없습니다.";

    @Mock
    private NativeWebRequest webRequest;

    private CurrentMemberIdArgumentResolver argumentResolver;

    @BeforeEach
    void setUp() {
        argumentResolver = new CurrentMemberIdArgumentResolver();
    }

    @Test
    void CurrentMemberId가_붙은_Long_매개변수만_지원한다() throws Exception {
        // given
        MethodParameter annotatedLong = parameter("annotatedLong", Long.class);
        MethodParameter annotatedString = parameter("annotatedString", String.class);
        MethodParameter unannotatedLong = parameter("unannotatedLong", Long.class);

        // when
        boolean supportsAnnotatedLong = argumentResolver.supportsParameter(annotatedLong);
        boolean supportsAnnotatedString = argumentResolver.supportsParameter(annotatedString);
        boolean supportsUnannotatedLong = argumentResolver.supportsParameter(unannotatedLong);

        // then
        assertThat(supportsAnnotatedLong).isTrue();
        assertThat(supportsAnnotatedString).isFalse();
        assertThat(supportsUnannotatedLong).isFalse();
    }

    @Test
    void JWT_인증의_양수_subject를_Long_회원_ID로_변환한다() throws Exception {
        // given
        Jwt jwt = Jwt.withTokenValue("sensitive-access-token")
                .header("alg", "RS256")
                .subject("987654321")
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("MEMBER"))
        );
        given(webRequest.getUserPrincipal()).willReturn(authentication);

        // when
        Long memberId = argumentResolver.resolveArgument(
                parameter("annotatedLong", Long.class),
                null,
                webRequest,
                null
        );

        // then
        assertThat(memberId).isEqualTo(987_654_321L);
    }

    @Test
    void 인증되지_않은_JWT_인증은_고정된_원인없는_내부오류다() {
        // given
        Jwt jwt = Jwt.withTokenValue("sensitive-unauthenticated-token")
                .header("alg", "RS256")
                .subject("987654321")
                .claim("memberId", "sensitive-member-id")
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        given(webRequest.getUserPrincipal()).willReturn(authentication);

        // when / then
        assertSanitizedInternalFailure(() -> resolveAnnotatedLong());
    }

    @Test
    void 인증이_없거나_JWT_인증이_아니면_고정된_원인없는_내부오류다() throws Exception {
        // given
        Principal nonJwtPrincipal = () -> "sensitive-principal";
        given(webRequest.getUserPrincipal()).willReturn(null, nonJwtPrincipal);

        // when / then
        assertSanitizedInternalFailure(() -> resolveAnnotatedLong());
        assertSanitizedInternalFailure(() -> resolveAnnotatedLong());
    }

    @ParameterizedTest
    @MethodSource("invalidSubjects")
    void 유효한_양수_Long이_아닌_JWT_subject는_고정된_원인없는_내부오류다(String subject) {
        // given
        Jwt jwt = mock(Jwt.class);
        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getToken()).willReturn(jwt);
        given(jwt.getSubject()).willReturn(subject);
        given(webRequest.getUserPrincipal()).willReturn(authentication);

        // when / then
        assertSanitizedInternalFailure(() -> resolveAnnotatedLong());
    }

    @Test
    void MVC_설정은_현재_회원_ID_resolver를_등록한다() {
        // given
        AuthenticationWebMvcConfig config = new AuthenticationWebMvcConfig(argumentResolver);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // when
        config.addArgumentResolvers(resolvers);

        // then
        assertThat(resolvers).containsExactly(argumentResolver);
    }

    private static Stream<Arguments> invalidSubjects() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("sensitive-member-subject"),
                Arguments.of("0"),
                Arguments.of("-1"),
                Arguments.of("9223372036854775808")
        );
    }

    private MethodParameter parameter(String methodName, Class<?> parameterType) throws Exception {
        Method method = TestController.class.getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    private Long resolveAnnotatedLong() throws Exception {
        return argumentResolver.resolveArgument(
                parameter("annotatedLong", Long.class),
                null,
                webRequest,
                null
        );
    }

    private void assertSanitizedInternalFailure(ThrowingAction action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INTERNAL_FAILURE_MESSAGE)
                .hasNoCause()
                .message()
                .doesNotContain(
                        "sensitive-access-token",
                        "sensitive-unauthenticated-token",
                        "sensitive-principal",
                        "sensitive-member-subject",
                        "sensitive-member-id",
                        "987654321",
                        "9223372036854775808"
                );
    }

    private interface ThrowingAction {

        void run() throws Exception;
    }

    private static class TestController {

        private void annotatedLong(@CurrentMemberId Long memberId) {
        }

        private void annotatedString(@CurrentMemberId String memberId) {
        }

        private void unannotatedLong(Long memberId) {
        }
    }
}
