package com.classitda.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

import com.classitda.authentication.application.SignupService;
import com.classitda.authentication.application.phone.PhoneVerificationStore;
import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.application.session.SignupSessionStore;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.signup.SignupResponse;
import com.classitda.member.domain.Term;
import com.classitda.member.domain.TermCode;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.domain.repository.MemberTermAgreementRepository;
import com.classitda.member.domain.repository.TermRepository;
import com.classitda.support.AuthenticationIntegrationTestConfiguration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Import(AuthenticationIntegrationTestConfiguration.class)
@ActiveProfiles("local")
@SpringBootTest(properties = "spring.sql.init.mode=always")
class SignupCompletionConcurrencyIntegrationTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthAccountRepository authAccountRepository;

    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @MockitoSpyBean
    private SignupSessionStore signupSessionStore;

    @MockitoSpyBean
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
    void 같은_JTI의_동시_가입은_한_계정만_남고_두_요청_모두_로그인_토큰에_성공한다() throws Exception {
        // given
        String signupJti = "same-jti-race";
        String providerSubject = "same-jti-sensitive-subject";
        String phoneNumber = "01055555555";
        prepareDirectState(signupJti, providerSubject, "same-jti@example.com", phoneNumber);
        installTwoRequestBarriers();

        // when
        List<CompletionAttempt> attempts = completeConcurrently(List.of(
                new CompletionCommand(signupJti, validRequest()),
                new CompletionCommand(signupJti, validRequest())
        ));
        resetBoundarySpies();

        // then
        assertTwoSuccessfulTokens(attempts);
        assertSingleCompleteAccount();
        assertThat(signupSessionStore.hasActiveSession(signupJti)).isFalse();
        assertThat(phoneVerificationStore.findVerifiedPhoneNumber(signupJti)).isEmpty();
    }

    @Test
    void 서로_다른_JTI의_같은_전화번호_경합은_PHONE_001과_한_계정만_남긴다() throws Exception {
        // given
        String phoneNumber = "01066666666";
        prepareDirectState("phone-race-a", "phone-subject-a", "phone-a@example.com", phoneNumber);
        prepareDirectState("phone-race-b", "phone-subject-b", "phone-b@example.com", phoneNumber);
        installTwoRequestBarriers();

        // when
        List<CompletionAttempt> attempts = completeConcurrently(List.of(
                new CompletionCommand("phone-race-a", validRequest()),
                new CompletionCommand("phone-race-b", validRequest())
        ));
        resetBoundarySpies();

        // then
        CompletionAttempt loser = assertOnePhoneConflict(attempts);
        assertSingleCompleteAccount();
        CompletionAttempt winner = attempts.stream()
                .filter(attempt -> attempt.failure() == null)
                .findFirst()
                .orElseThrow();
        assertThat(signupSessionStore.hasActiveSession(winner.signupJti())).isFalse();
        assertThat(phoneVerificationStore.findVerifiedPhoneNumber(winner.signupJti())).isEmpty();
        assertThat(signupSessionStore.hasActiveSession(loser.signupJti())).isTrue();
        assertThat(phoneVerificationStore.findVerifiedPhoneNumber(loser.signupJti())).contains(phoneNumber);
    }

    @Test
    void 서로_다른_JTI의_같은_소셜계정_경합은_한_계정만_남고_두_요청_모두_성공한다() throws Exception {
        // given
        String providerSubject = "social-race-sensitive-subject";
        prepareDirectState("social-race-a", providerSubject, "social-a@example.com", "01077777777");
        prepareDirectState("social-race-b", providerSubject, "social-b@example.com", "01088888888");
        installTwoRequestBarriers();

        // when
        List<CompletionAttempt> attempts = completeConcurrently(List.of(
                new CompletionCommand("social-race-a", validRequest()),
                new CompletionCommand("social-race-b", validRequest())
        ));
        resetBoundarySpies();

        // then
        assertTwoSuccessfulTokens(attempts);
        assertSingleCompleteAccount();
        assertThat(signupSessionStore.hasActiveSession("social-race-a")).isFalse();
        assertThat(signupSessionStore.hasActiveSession("social-race-b")).isFalse();
        assertThat(phoneVerificationStore.findVerifiedPhoneNumber("social-race-a")).isEmpty();
        assertThat(phoneVerificationStore.findVerifiedPhoneNumber("social-race-b")).isEmpty();
    }

    private void prepareDirectState(
            String signupJti,
            String providerSubject,
            String providerEmail,
            String phoneNumber
    ) {
        signupSessionStore.save(
                signupJti,
                new SignupSession(OauthProvider.GOOGLE, providerSubject, providerEmail)
        );
        redisTemplate.opsForValue().set(
                verifiedPhoneKey(signupJti),
                phoneNumber,
                tokenProperties.signupTtl()
        );
    }

    private void installTwoRequestBarriers() {
        CyclicBarrier sessionBarrier = new CyclicBarrier(2);
        CyclicBarrier phoneStateBarrier = new CyclicBarrier(2);

        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            sessionBarrier.await(10, TimeUnit.SECONDS);
            return result;
        }).when(signupSessionStore).findBySignupJti(anyString());
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            phoneStateBarrier.await(10, TimeUnit.SECONDS);
            return result;
        }).when(phoneVerificationStore).findVerifiedPhoneNumber(anyString());
    }

    private List<CompletionAttempt> completeConcurrently(List<CompletionCommand> commands) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(commands.size());
        CountDownLatch ready = new CountDownLatch(commands.size());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<CompletionAttempt>> futures = new ArrayList<>();
        try {
            for (CompletionCommand command : commands) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        SignupResponse response = signupService.complete(command.signupJti(), command.request());
                        return new CompletionAttempt(command.signupJti(), response, null);
                    } catch (RuntimeException exception) {
                        return new CompletionAttempt(command.signupJti(), null, exception);
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<CompletionAttempt> attempts = new ArrayList<>();
            for (Future<CompletionAttempt> future : futures) {
                attempts.add(future.get(30, TimeUnit.SECONDS));
            }
            return attempts;
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private void assertTwoSuccessfulTokens(List<CompletionAttempt> attempts) {
        assertThat(attempts).hasSize(2);
        assertThat(attempts).allSatisfy(attempt -> {
            assertThat(attempt.failure()).isNull();
            assertThat(attempt.response()).isNotNull();
            assertThat(attempt.response().accessToken()).isNotBlank();
            assertThat(attempt.response().refreshToken()).isNotBlank();
        });
    }

    private CompletionAttempt assertOnePhoneConflict(List<CompletionAttempt> attempts) {
        assertThat(attempts).hasSize(2);
        assertThat(attempts.stream().filter(attempt -> attempt.failure() == null)).hasSize(1);
        CompletionAttempt loser = attempts.stream()
                .filter(attempt -> attempt.failure() != null)
                .findFirst()
                .orElseThrow();
        assertThat(loser.failure()).isInstanceOf(AuthException.class);
        assertThat(((AuthException) loser.failure()).getErrorCode())
                .isEqualTo(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        return loser;
    }

    private void assertSingleCompleteAccount() {
        assertThat(memberRepository.count()).isEqualTo(1L);
        assertThat(authAccountRepository.count()).isEqualTo(1L);
        assertThat(memberTermAgreementRepository.count()).isEqualTo(2L);
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

    private void resetBoundarySpies() {
        reset(signupSessionStore, phoneVerificationStore);
    }

    private void cleanState() {
        memberTermAgreementRepository.deleteAll();
        authAccountRepository.deleteAll();
        memberRepository.deleteAll();
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushDb();
        }
    }

    private String verifiedPhoneKey(String signupJti) {
        return "signup:verified-phone:" + signupJti;
    }

    private record CompletionCommand(String signupJti, SignupRequest request) {
    }

    private record CompletionAttempt(String signupJti, SignupResponse response, RuntimeException failure) {
    }
}
