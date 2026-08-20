package com.classitda.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.classitda.authentication.application.LogoutService;
import com.classitda.authentication.application.RefreshTokenService;
import com.classitda.authentication.application.session.RefreshSession;
import com.classitda.authentication.application.session.RefreshSessionStore;
import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.application.session.SignupSessionStore;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.RefreshTokenIssuer;
import com.classitda.authentication.application.token.RefreshTokenVerifier;
import com.classitda.authentication.application.token.SignupTokenIssuer;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.application.token.result.IssuedRefreshToken;
import com.classitda.authentication.application.token.result.IssuedSignupToken;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.security.jwt.JwtContract;
import com.classitda.authentication.infra.security.jwt.JwtTokenEncoder;
import com.classitda.authentication.infra.security.jwt.SignupSessionJwtValidator;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import com.classitda.authentication.infra.session.RedisRefreshSessionStore;
import com.classitda.authentication.infra.session.RedisSignupSessionStore;
import com.classitda.authentication.presentation.dto.logout.LogoutRequest;
import com.classitda.authentication.presentation.dto.token.RefreshTokenRequest;
import com.classitda.authentication.presentation.dto.token.LoginTokenResponse;
import com.classitda.authentication.support.JwtTestSupport;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
class RedisTokenSessionIntegrationTest {

    private static final Duration SIGNUP_TTL = Duration.ofMinutes(30);
    private static final Duration ACCESS_TTL = Duration.ofHours(1);
    private static final Duration REFRESH_TTL = Duration.ofDays(30);
    private static final long REFRESH_TTL_SECONDS = 2_592_000L;
    private static final String REFRESH_KEY_PREFIX = "auth:refresh:";

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private ObjectMapper objectMapper;
    private TokenProperties tokenProperties;
    private RefreshTokenIssuer refreshTokenIssuer;
    private RefreshTokenVerifier refreshTokenVerifier;
    private RefreshSessionStore refreshSessionStore;
    private JwtTestSupport jwtSupport;
    private LoginTokenIssuer loginTokenIssuer;
    private RefreshTokenService refreshTokenService;
    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        objectMapper = new ObjectMapper();
        tokenProperties = new TokenProperties(SIGNUP_TTL, ACCESS_TTL, REFRESH_TTL);
        refreshTokenIssuer = new RefreshTokenIssuer();
        refreshTokenVerifier = new RefreshTokenVerifier();
        refreshSessionStore = new RedisRefreshSessionStore(redisTemplate, objectMapper);
        jwtSupport = JwtTestSupport.create();
        loginTokenIssuer = new LoginTokenIssuer(
                refreshTokenIssuer,
                refreshSessionStore,
                new JwtTokenEncoder(jwtSupport.encoder()),
                tokenProperties
        );
        refreshTokenService = new RefreshTokenService(
                refreshTokenIssuer,
                refreshTokenVerifier,
                refreshSessionStore,
                loginTokenIssuer,
                tokenProperties
        );
        logoutService = new LogoutService(refreshTokenVerifier, refreshSessionStore);

        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }
    }

    @AfterEach
    void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void 가입_세션을_JSON과_TTL로_저장하고_다시_읽는다() throws Exception {
        // given
        SignupSessionStore signupSessionStore = signupSessionStore();
        SignupSession expected = new SignupSession(
                OauthProvider.GOOGLE,
                "provider-subject",
                "member@example.com"
        );

        // when
        signupSessionStore.save("signup-jti", expected);

        // then
        assertThat(signupSessionStore.findBySignupJti("signup-jti")).contains(expected);
        String key = "signup:session:signup-jti";
        JsonNode stored = objectMapper.readTree(redisTemplate.opsForValue().get(key));
        assertThat(stored.get("provider").asText()).isEqualTo("GOOGLE");
        assertThat(stored.get("providerSubject").asText()).isEqualTo("provider-subject");
        assertThat(stored.get("providerEmail").asText()).isEqualTo("member@example.com");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isBetween(1_750L, 1_800L);
    }

    @Test
    void 가입_세션이_없거나_만료되면_조회되지_않는다() throws InterruptedException {
        // given
        SignupSessionStore signupSessionStore = signupSessionStore();
        signupSessionStore.save(
                "expired-jti",
                new SignupSession(OauthProvider.GOOGLE, "provider-subject", "member@example.com")
        );
        redisTemplate.expire("signup:session:expired-jti", Duration.ofMillis(100));
        awaitAbsent("signup:session:expired-jti");

        // when / then
        assertThat(signupSessionStore.findBySignupJti("missing-jti")).isEmpty();
        assertThat(signupSessionStore.findBySignupJti("expired-jti")).isEmpty();
    }

    @Test
    void 회원가입_토큰을_발급하면_같은_JTI의_가입_세션을_만든다() {
        // given
        SignupSessionStore signupSessionStore = signupSessionStore();
        SignupSessionJwtValidator validator = new SignupSessionJwtValidator(signupSessionStore);
        SignupTokenIssuer issuer = new SignupTokenIssuer(
                signupSessionStore,
                new JwtTokenEncoder(jwtSupport.encoder()),
                tokenProperties
        );

        // when
        IssuedSignupToken issued = issuer.issueSignupToken(
                OauthProvider.GOOGLE,
                "provider-subject",
                "member@example.com"
        );
        Jwt jwt = jwtSupport.decoder(validator).decode(issued.signupToken());

        // then
        assertThat(signupSessionStore.findBySignupJti(jwt.getId())).contains(
                new SignupSession(OauthProvider.GOOGLE, "provider-subject", "member@example.com")
        );
    }

    @Test
    void 로그인_토큰은_액세스_claim을_유지하고_리프레시_해시만_Redis에_저장한다() throws Exception {
        // given / when
        IssuedLoginTokens issued = loginTokenIssuer.issueLoginTokens(42L);
        Jwt accessJwt = accessDecoder().decode(issued.accessToken());

        // then
        assertThat(issued.accessTokenExpiresIn()).isEqualTo(3_600L);
        assertThat(issued.refreshTokenExpiresIn()).isEqualTo(REFRESH_TTL_SECONDS);
        assertAccessClaims(accessJwt, 42L);
        String key = refreshKey(issued.refreshToken());
        String storedValue = redisTemplate.opsForValue().get(key);
        JsonNode stored = objectMapper.readTree(storedValue);
        assertThat(storedValue).doesNotContain(issued.refreshToken());
        assertThat(stored.get("tokenHash").asText()).isEqualTo(sha256(issued.refreshToken()));
        assertThat(stored.get("memberId").asLong()).isEqualTo(42L);
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS))
                .isBetween(2_591_950L, REFRESH_TTL_SECONDS);
    }

    @Test
    void 정상_갱신은_기존_session을_소비하고_새_30일_session과_1시간_JWT를_만든다() throws Exception {
        // given
        IssuedLoginTokens loginTokens = loginTokenIssuer.issueLoginTokens(42L);
        String oldKey = refreshKey(loginTokens.refreshToken());
        long beforeExpiry = Instant.now().plusSeconds(REFRESH_TTL_SECONDS).getEpochSecond();

        // when
        LoginTokenResponse response = refreshTokenService.refresh(
                RefreshTokenRequest.from(loginTokens.refreshToken())
        );

        // then
        long afterExpiry = Instant.now().plusSeconds(REFRESH_TTL_SECONDS).getEpochSecond();
        assertThat(response.accessTokenExpiresIn()).isEqualTo(3_600L);
        assertThat(response.refreshTokenExpiresIn()).isEqualTo(REFRESH_TTL_SECONDS);
        assertThat(response.refreshToken())
                .matches("^[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}$")
                .isNotEqualTo(loginTokens.refreshToken());
        assertThat(redisTemplate.hasKey(oldKey)).isFalse();
        String newKey = refreshKey(response.refreshToken());
        String newValue = redisTemplate.opsForValue().get(newKey);
        JsonNode newSession = objectMapper.readTree(newValue);
        assertThat(newValue).doesNotContain(loginTokens.refreshToken(), response.refreshToken());
        assertThat(newSession.get("tokenHash").asText()).isEqualTo(sha256(response.refreshToken()));
        assertThat(newSession.get("memberId").asLong()).isEqualTo(42L);
        assertThat(newSession.get("expiresAtEpochSecond").asLong()).isBetween(beforeExpiry, afterExpiry);
        assertThat(redisTemplate.getExpire(newKey, TimeUnit.SECONDS))
                .isBetween(2_591_950L, REFRESH_TTL_SECONDS);
        assertAccessClaims(accessDecoder().decode(response.accessToken()), 42L);
    }

    @Test
    void 없거나_만료되거나_해시가_다르거나_소비된_토큰은_모두_AUTH_008이다() {
        // given
        IssuedRefreshToken missing = refreshTokenIssuer.issue();
        IssuedRefreshToken expired = refreshTokenIssuer.issue();
        refreshSessionStore.save(
                expired.sessionId(),
                RefreshSession.of(expired.tokenHash(), 42L, Instant.now().minusSeconds(1).getEpochSecond()),
                60L
        );
        IssuedRefreshToken original = refreshTokenIssuer.issue();
        String forged = original.sessionId() + "." + "Z".repeat(43);
        refreshSessionStore.save(
                original.sessionId(),
                RefreshSession.of(original.tokenHash(), 42L, Instant.now().plusSeconds(60).getEpochSecond()),
                60L
        );
        IssuedLoginTokens consumable = loginTokenIssuer.issueLoginTokens(42L);
        refreshTokenService.refresh(RefreshTokenRequest.from(consumable.refreshToken()));

        // when / then
        List<String> invalidTokens = List.of(
                missing.refreshToken(),
                expired.refreshToken(),
                forged,
                consumable.refreshToken()
        );
        for (String invalidToken : invalidTokens) {
            assertAuthError(() -> refreshTokenService.refresh(RefreshTokenRequest.from(invalidToken)));
        }
    }

    @Test
    void 손상된_리프레시_JSON과_필드는_인증실패가_아닌_내부오류다() {
        // given
        String sessionId = "A".repeat(43);
        List<String> corruptValues = List.of(
                "not-json",
                "{}",
                "{\"tokenHash\":1,\"memberId\":42,\"expiresAtEpochSecond\":100}",
                "{\"tokenHash\":\"%s\",\"memberId\":\"42\",\"expiresAtEpochSecond\":100}"
                        .formatted("a".repeat(64)),
                "{\"tokenHash\":\"invalid\",\"memberId\":42,\"expiresAtEpochSecond\":100}",
                "{\"tokenHash\":\"%s\",\"memberId\":0,\"expiresAtEpochSecond\":100}"
                        .formatted("a".repeat(64)),
                "{\"tokenHash\":\"%s\",\"memberId\":42,\"expiresAtEpochSecond\":0}"
                        .formatted("a".repeat(64))
        );

        // when / then
        for (String corruptValue : corruptValues) {
            redisTemplate.opsForValue().set(REFRESH_KEY_PREFIX + sessionId, corruptValue, Duration.ofMinutes(1));
            assertThatThrownBy(() -> refreshSessionStore.findBySessionId(sessionId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("리프레시 세션 저장소 처리에 실패했습니다.")
                    .hasNoCause();
        }
    }

    @Test
    void Lua의_새_key_충돌과_손상된_old_JSON은_기존_상태를_소비하지_않는다() {
        // given
        IssuedRefreshToken oldToken = refreshTokenIssuer.issue();
        IssuedRefreshToken newToken = refreshTokenIssuer.issue();
        RefreshSession oldSession = activeSession(oldToken.tokenHash(), 42L);
        RefreshSession newSession = activeSession(newToken.tokenHash(), 42L);
        refreshSessionStore.save(oldToken.sessionId(), oldSession, 60L);
        refreshSessionStore.save(newToken.sessionId(), newSession, 60L);

        // when
        RefreshSessionStore.RotateOutcome collision = refreshSessionStore.rotate(
                oldToken.sessionId(),
                oldSession,
                newToken.sessionId(),
                newSession,
                60L
        );

        // then
        assertThat(collision).isEqualTo(RefreshSessionStore.RotateOutcome.NEW_SESSION_CONFLICT);
        assertThat(refreshSessionStore.findBySessionId(oldToken.sessionId())).contains(oldSession);
        assertThat(refreshSessionStore.findBySessionId(newToken.sessionId())).contains(newSession);

        // given
        redisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + oldToken.sessionId(),
                "{broken-json",
                Duration.ofMinutes(1)
        );

        // when / then
        assertThatThrownBy(() -> refreshSessionStore.rotate(
                oldToken.sessionId(),
                oldSession,
                newToken.sessionId() + "X",
                newSession,
                60L
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("리프레시 세션 저장소 처리에 실패했습니다.")
                .hasNoCause();
        assertThat(redisTemplate.hasKey(REFRESH_KEY_PREFIX + oldToken.sessionId())).isTrue();
    }

    @Test
    void Lua는_2의_53승을_넘는_인접한_회원_ID를_정확히_구분한다() {
        // given
        IssuedRefreshToken oldToken = refreshTokenIssuer.issue();
        IssuedRefreshToken newToken = refreshTokenIssuer.issue();
        long expiresAtEpochSecond = Instant.now().plusSeconds(60).getEpochSecond();
        RefreshSession storedOldSession = RefreshSession.of(
                oldToken.tokenHash(),
                9_007_199_254_740_992L,
                expiresAtEpochSecond
        );
        RefreshSession adjacentExpectedSession = RefreshSession.of(
                oldToken.tokenHash(),
                9_007_199_254_740_993L,
                expiresAtEpochSecond
        );
        RefreshSession newSession = RefreshSession.of(
                newToken.tokenHash(),
                storedOldSession.memberId(),
                expiresAtEpochSecond
        );
        refreshSessionStore.save(oldToken.sessionId(), storedOldSession, 60L);

        // when
        RefreshSessionStore.RotateOutcome mismatch = refreshSessionStore.rotate(
                oldToken.sessionId(),
                adjacentExpectedSession,
                newToken.sessionId(),
                newSession,
                60L
        );

        // then
        assertThat(mismatch).isEqualTo(RefreshSessionStore.RotateOutcome.OLD_SESSION_MISMATCH);
        assertThat(refreshSessionStore.findBySessionId(oldToken.sessionId())).contains(storedOldSession);
        assertThat(refreshSessionStore.findBySessionId(newToken.sessionId())).isEmpty();

        // when
        RefreshSessionStore.RotateOutcome matching = refreshSessionStore.rotate(
                oldToken.sessionId(),
                storedOldSession,
                newToken.sessionId(),
                newSession,
                60L
        );

        // then
        assertThat(matching).isEqualTo(RefreshSessionStore.RotateOutcome.ROTATED);
        assertThat(refreshSessionStore.findBySessionId(oldToken.sessionId())).isEmpty();
        assertThat(refreshSessionStore.findBySessionId(newToken.sessionId())).contains(newSession);
    }

    @Test
    void 동일_리프레시_토큰의_동시_요청은_정확히_하나만_성공하고_새_session_하나만_남긴다() throws Exception {
        // given
        IssuedLoginTokens loginTokens = loginTokenIssuer.issueLoginTokens(42L);
        int requestCount = 8;
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<Future<RotationAttempt>> futures = new ArrayList<>();
        for (int index = 0; index < requestCount; index++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    LoginTokenResponse response = refreshTokenService.refresh(
                            RefreshTokenRequest.from(loginTokens.refreshToken())
                    );
                    return RotationAttempt.success(response);
                } catch (AuthException exception) {
                    return RotationAttempt.failure(exception);
                }
            }));
        }
        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();

        // when
        start.countDown();
        List<RotationAttempt> attempts = new ArrayList<>();
        for (Future<RotationAttempt> future : futures) {
            attempts.add(future.get(10, TimeUnit.SECONDS));
        }
        executor.shutdownNow();

        // then
        assertThat(attempts).filteredOn(RotationAttempt::succeeded).singleElement();
        assertThat(attempts).filteredOn(attempt -> !attempt.succeeded()).hasSize(requestCount - 1)
                .allSatisfy(attempt -> assertThat(attempt.errorCode())
                        .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID));
        LoginTokenResponse winner = attempts.stream()
                .filter(RotationAttempt::succeeded)
                .map(RotationAttempt::response)
                .findFirst()
                .orElseThrow();
        assertThat(redisTemplate.hasKey(refreshKey(loginTokens.refreshToken()))).isFalse();
        assertThat(redisTemplate.keys(REFRESH_KEY_PREFIX + "*")).containsExactly(refreshKey(winner.refreshToken()));
        assertThat(redisTemplate.opsForValue().get(refreshKey(winner.refreshToken())))
                .doesNotContain(loginTokens.refreshToken(), winner.refreshToken());
    }

    @Test
    void 조건부_삭제는_정확한_세션만_삭제하고_다른_회원_해시_세션은_보존한다() {
        // given
        IssuedRefreshToken targetToken = refreshTokenIssuer.issue();
        IssuedRefreshToken otherToken = refreshTokenIssuer.issue();
        RefreshSession targetSession = activeSession(targetToken.tokenHash(), 42L);
        RefreshSession otherSession = activeSession(otherToken.tokenHash(), 84L);
        refreshSessionStore.save(targetToken.sessionId(), targetSession, 60L);
        refreshSessionStore.save(otherToken.sessionId(), otherSession, 60L);

        // when
        RefreshSessionStore.DeleteOutcome wrongMember = refreshSessionStore.deleteIfMatches(
                targetToken.sessionId(),
                RefreshSession.of(
                        targetSession.tokenHash(),
                        84L,
                        targetSession.expiresAtEpochSecond()
                )
        );
        RefreshSessionStore.DeleteOutcome wrongHash = refreshSessionStore.deleteIfMatches(
                targetToken.sessionId(),
                RefreshSession.of(
                        "f".repeat(64),
                        targetSession.memberId(),
                        targetSession.expiresAtEpochSecond()
                )
        );
        RefreshSessionStore.DeleteOutcome deleted = refreshSessionStore.deleteIfMatches(
                targetToken.sessionId(),
                targetSession
        );

        // then
        assertThat(wrongMember).isEqualTo(RefreshSessionStore.DeleteOutcome.SESSION_MISMATCH);
        assertThat(wrongHash).isEqualTo(RefreshSessionStore.DeleteOutcome.SESSION_MISMATCH);
        assertThat(deleted).isEqualTo(RefreshSessionStore.DeleteOutcome.DELETED);
        assertThat(refreshSessionStore.findBySessionId(targetToken.sessionId())).isEmpty();
        assertThat(refreshSessionStore.findBySessionId(otherToken.sessionId())).contains(otherSession);
        assertThat(redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + otherToken.sessionId()))
                .doesNotContain(targetToken.refreshToken(), otherToken.refreshToken());
    }

    @Test
    void 없거나_논리적으로_만료된_세션의_로그아웃은_멱등하게_완료한다() {
        // given
        IssuedRefreshToken missing = refreshTokenIssuer.issue();
        IssuedRefreshToken expired = refreshTokenIssuer.issue();
        RefreshSession expiredSession = RefreshSession.of(
                expired.tokenHash(),
                42L,
                Instant.now().minusSeconds(1).getEpochSecond()
        );
        refreshSessionStore.save(expired.sessionId(), expiredSession, 60L);

        // when
        logoutService.logout(42L, LogoutRequest.from(missing.refreshToken()));
        logoutService.logout(42L, LogoutRequest.from(expired.refreshToken()));

        // then
        assertThat(refreshSessionStore.findBySessionId(missing.sessionId())).isEmpty();
        assertThat(refreshSessionStore.findBySessionId(expired.sessionId())).contains(expiredSession);
    }

    @Test
    void 조건부_삭제는_손상된_JSON과_필드를_삭제하지_않고_내부오류로_처리한다() {
        // given
        IssuedRefreshToken token = refreshTokenIssuer.issue();
        RefreshSession expected = activeSession(token.tokenHash(), 42L);
        List<String> corruptValues = List.of(
                "not-json",
                "{}",
                "{\"tokenHash\":1,\"memberId\":42,\"expiresAtEpochSecond\":100}",
                "{\"tokenHash\":\"%s\",\"memberId\":0,\"expiresAtEpochSecond\":100}"
                        .formatted(token.tokenHash())
        );

        // when / then
        for (String corruptValue : corruptValues) {
            redisTemplate.opsForValue().set(
                    REFRESH_KEY_PREFIX + token.sessionId(),
                    corruptValue,
                    Duration.ofMinutes(1)
            );
            assertThatThrownBy(() -> refreshSessionStore.deleteIfMatches(token.sessionId(), expected))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("리프레시 세션 저장소 처리에 실패했습니다.")
                    .hasNoCause();
            assertThat(redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + token.sessionId()))
                    .isEqualTo(corruptValue);
        }
    }

    @Test
    void 로그아웃한_리프레시_토큰은_다시_갱신할_수_없다() {
        // given
        IssuedLoginTokens loginTokens = loginTokenIssuer.issueLoginTokens(42L);

        // when
        logoutService.logout(42L, LogoutRequest.from(loginTokens.refreshToken()));

        // then
        assertThat(redisTemplate.hasKey(refreshKey(loginTokens.refreshToken()))).isFalse();
        assertAuthError(() -> refreshTokenService.refresh(
                RefreshTokenRequest.from(loginTokens.refreshToken())
        ));
    }

    @Test
    void 로그아웃과_갱신이_동시에_요청되면_정확히_하나의_Redis_mutation만_성공한다() throws Exception {
        // given
        IssuedLoginTokens loginTokens = loginTokenIssuer.issueLoginTokens(42L);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Void> logoutFuture = executor.submit(() -> {
            ready.countDown();
            start.await();
            logoutService.logout(42L, LogoutRequest.from(loginTokens.refreshToken()));
            return null;
        });
        Future<RotationAttempt> refreshFuture = executor.submit(() -> {
            ready.countDown();
            start.await();
            try {
                return RotationAttempt.success(refreshTokenService.refresh(
                        RefreshTokenRequest.from(loginTokens.refreshToken())
                ));
            } catch (AuthException exception) {
                return RotationAttempt.failure(exception);
            }
        });
        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();

        // when
        start.countDown();
        logoutFuture.get(10, TimeUnit.SECONDS);
        RotationAttempt refreshAttempt = refreshFuture.get(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        // then
        assertThat(redisTemplate.hasKey(refreshKey(loginTokens.refreshToken()))).isFalse();
        if (refreshAttempt.succeeded()) {
            assertThat(redisTemplate.keys(REFRESH_KEY_PREFIX + "*"))
                    .containsExactly(refreshKey(refreshAttempt.response().refreshToken()));
            assertThat(redisTemplate.opsForValue().get(refreshKey(refreshAttempt.response().refreshToken())))
                    .doesNotContain(loginTokens.refreshToken(), refreshAttempt.response().refreshToken());
        } else {
            assertThat(refreshAttempt.errorCode()).isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
            assertThat(redisTemplate.keys(REFRESH_KEY_PREFIX + "*")).isEmpty();
        }
    }

    @Test
    void Redis_연결_실패는_하위_오류를_숨긴_내부오류다() {
        // given
        IssuedRefreshToken token = refreshTokenIssuer.issue();
        RefreshSession session = activeSession(token.tokenHash(), 42L);
        connectionFactory.stop();

        // when / then
        assertThatThrownBy(() -> refreshSessionStore.deleteIfMatches(token.sessionId(), session))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("리프레시 세션 저장소 처리에 실패했습니다.")
                .hasNoCause();
    }

    private SignupSessionStore signupSessionStore() {
        return new RedisSignupSessionStore(redisTemplate, objectMapper, tokenProperties);
    }

    private JwtDecoder accessDecoder() {
        return jwtSupport.decoder(new SignupSessionJwtValidator(mock(SignupSessionStore.class)));
    }

    private RefreshSession activeSession(String hash, Long memberId) {
        return RefreshSession.of(hash, memberId, Instant.now().plusSeconds(60).getEpochSecond());
    }

    private String refreshKey(String refreshToken) {
        return REFRESH_KEY_PREFIX + refreshTokenVerifier.extractSessionId(refreshToken);
    }

    private String sha256(String value) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(messageDigest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private void assertAccessClaims(Jwt jwt, Long memberId) {
        assertThat(jwt.getHeaders()).containsEntry("alg", "RS256");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(JwtContract.ISSUER);
        assertThat(jwt.getClaimAsString(JwtContract.TOKEN_USE_CLAIM)).isEqualTo(TokenUse.ACCESS.name());
        assertThat(jwt.getSubject()).isEqualTo(memberId.toString());
        assertThat(UUID.fromString(jwt.getId())).isNotNull();
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(ACCESS_TTL);
    }

    private void assertAuthError(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }

    private void awaitAbsent(String key) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (Boolean.TRUE.equals(redisTemplate.hasKey(key)) && System.nanoTime() < deadline) {
            Thread.sleep(25L);
        }
        assertThat(redisTemplate.hasKey(key)).isFalse();
    }

    private record RotationAttempt(
            LoginTokenResponse response,
            AuthErrorCode errorCode
    ) {

        private static RotationAttempt success(LoginTokenResponse response) {
            return new RotationAttempt(response, null);
        }

        private static RotationAttempt failure(AuthException exception) {
            return new RotationAttempt(null, (AuthErrorCode) exception.getErrorCode());
        }

        private boolean succeeded() {
            return response != null;
        }
    }
}
