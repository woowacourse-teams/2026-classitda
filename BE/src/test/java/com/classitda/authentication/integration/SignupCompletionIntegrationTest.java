package com.classitda.authentication.integration;

import static com.classitda.support.AuthenticationIntegrationTestConfiguration.FIXED_OTP;
import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.application.phone.PhoneVerificationStore;
import com.classitda.authentication.application.session.SignupSessionStore;
import com.classitda.authentication.application.token.SignupTokenIssuer;
import com.classitda.authentication.application.token.IssuedSignupToken;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationResponse;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.signup.SignupResponse;
import com.classitda.member.domain.Term;
import com.classitda.member.domain.TermCode;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.domain.repository.MemberTermAgreementRepository;
import com.classitda.member.domain.repository.TermRepository;
import com.classitda.support.AuthenticationIntegrationTestConfiguration;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@Import(AuthenticationIntegrationTestConfiguration.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@SpringBootTest(properties = "spring.sql.init.mode=always")
class SignupCompletionIntegrationTest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private SignupTokenIssuer signupTokenIssuer;

    @Autowired
    private PhoneVerificationService phoneVerificationService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthAccountRepository authAccountRepository;

    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @Autowired
    private SignupSessionStore signupSessionStore;

    @Autowired
    private PhoneVerificationStore phoneVerificationStore;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        cleanState();
    }

    @AfterEach
    void tearDown() {
        cleanState();
    }

    @Test
    void 인증된_가입_요청은_계정을_만들고_가입_상태를_소비한다() {
        // given
        String phoneNumber = "+821012345678";
        VerifiedSignup signup = verifiedSignup(phoneNumber);

        // when
        SignupResponse response = signup(signup.token(), validRequest())
                .expectStatus().isCreated()
                .expectBody(SignupResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(memberRepository.findAll()).singleElement().satisfies(member -> {
            assertThat(member.getName()).isEqualTo("가입회원");
            assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        });
        assertThat(authAccountRepository.count()).isEqualTo(1L);
        assertThat(signupSessionStore.hasActiveSession(signup.jti())).isFalse();
        assertThat(phoneVerificationStore.findVerifiedPhoneNumber(signup.jti())).isEmpty();
    }

    @Test
    void 인증된_전화번호가_없으면_가입하지_않는다() {
        // given
        IssuedSignupToken issued = signupTokenIssuer.issueSignupToken(
                OauthProvider.GOOGLE,
                "missing-phone-subject",
                "missing-phone@example.com"
        );

        // when
        RestTestClient.ResponseSpec result = signup(issued.signupToken(), validRequest());

        // then
        result.expectStatus().isEqualTo(410)
                .expectBody()
                .json(
                        """
                                {"code":"PHONE-008","message":"인증이 완료된 휴대전화 번호가 없거나 만료되었습니다."}
                                """,
                        JsonCompareMode.STRICT
                );
        assertThat(memberRepository.count()).isZero();
    }

    private VerifiedSignup verifiedSignup(String phoneNumber) {
        IssuedSignupToken issued = signupTokenIssuer.issueSignupToken(
                OauthProvider.GOOGLE,
                "provider-subject",
                "provider@example.com"
        );
        Jwt signupJwt = jwtDecoder.decode(issued.signupToken());
        PhoneVerificationResponse verification = phoneVerificationService.send(signupJwt.getId(), phoneNumber);
        phoneVerificationService.confirm(signupJwt.getId(), verification.verificationId(), FIXED_OTP);
        return new VerifiedSignup(issued.signupToken(), signupJwt.getId());
    }

    private RestTestClient.ResponseSpec signup(String token, SignupRequest body) {
        return client.post()
                .uri("/api/auth/signup")
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .exchange();
    }

    private SignupRequest validRequest() {
        return SignupRequest.of(
                "가입회원",
                currentTerms().stream()
                        .filter(Term::isRequired)
                        .map(Term::getId)
                        .toList()
        );
    }

    private List<Term> currentTerms() {
        return List.of(
                currentTerm(TermCode.SERVICE_TERMS),
                currentTerm(TermCode.PRIVACY_POLICY),
                currentTerm(TermCode.MARKETING_CONSENT)
        );
    }

    private Term currentTerm(TermCode code) {
        return termRepository.findAll().stream()
                .filter(term -> term.getCode() == code)
                .max(Comparator.comparingInt(Term::getVersion))
                .orElseThrow();
    }

    private void cleanState() {
        memberTermAgreementRepository.deleteAll();
        authAccountRepository.deleteAll();
        memberRepository.deleteAll();
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushDb();
        }
    }

    private record VerifiedSignup(String token, String jti) {
    }
}
