package com.classitda.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.authentication.application.session.RefreshSessionStore;
import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.application.session.SignupSessionStore;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.application.token.result.IssuedSignupToken;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.RefreshTokenIssuer;
import com.classitda.authentication.application.token.SignupTokenIssuer;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.infra.security.jwt.JwtTokenEncoder;
import com.classitda.authentication.infra.security.jwt.SignupSessionJwtValidator;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import com.classitda.authentication.infra.session.RedisRefreshSessionStore;
import com.classitda.authentication.infra.session.RedisSignupSessionStore;
import com.classitda.authentication.support.JwtTestSupport;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
class RedisTokenSessionIntegrationTest {

    private static final Duration SIGNUP_TTL = Duration.ofMinutes(30);
    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TTL = Duration.ofDays(30);

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private ObjectMapper objectMapper;
    private TokenProperties tokenProperties;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        objectMapper = new ObjectMapper();
        tokenProperties = new TokenProperties(SIGNUP_TTL, ACCESS_TTL, REFRESH_TTL);

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
        SignupSessionStore registry = signupSessionRegistry();
        SignupSession expected = new SignupSession(
                OauthProvider.GOOGLE,
                "provider-subject",
                "member@example.com"
        );

        // when
        registry.save("signup-jti", expected);

        // then
        assertThat(registry.findBySignupJti("signup-jti")).contains(expected);
        String key = "signup:session:signup-jti";
        JsonNode stored = objectMapper.readTree(redisTemplate.opsForValue().get(key));
        assertThat(stored.get("provider").asText()).isEqualTo("GOOGLE");
        assertThat(stored.get("providerSubject").asText()).isEqualTo("provider-subject");
        assertThat(stored.get("providerEmail").asText()).isEqualTo("member@example.com");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isBetween(1750L, 1800L);
    }

    @Test
    void 가입_세션이_없거나_만료되면_조회되지_않는다() throws InterruptedException {
        // given
        SignupSessionStore registry = signupSessionRegistry();
        registry.save(
                "expired-jti",
                new SignupSession(OauthProvider.GOOGLE, "provider-subject", "member@example.com")
        );
        redisTemplate.expire("signup:session:expired-jti", Duration.ofMillis(100));
        awaitAbsent("signup:session:expired-jti");

        // when / then
        assertThat(registry.findBySignupJti("missing-jti")).isEmpty();
        assertThat(registry.findBySignupJti("expired-jti")).isEmpty();
    }

    @Test
    void 회원가입_토큰을_발급하면_같은_JTI의_가입_세션을_만든다() {
        // given
        JwtTestSupport jwtSupport = JwtTestSupport.create();
        SignupSessionStore registry = signupSessionRegistry();
        SignupSessionJwtValidator validator = new SignupSessionJwtValidator(registry);
        SignupTokenIssuer issuer = new SignupTokenIssuer(
                registry,
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
        assertThat(registry.findBySignupJti(jwt.getId())).contains(
                new SignupSession(OauthProvider.GOOGLE, "provider-subject", "member@example.com")
        );
    }

    @Test
    void 로그인_토큰은_리프레시_토큰의_해시만_Redis에_저장한다() throws Exception {
        // given
        JwtTestSupport jwtSupport = JwtTestSupport.create();
        RefreshSessionStore refreshSessionStore = new RedisRefreshSessionStore(redisTemplate, objectMapper);
        LoginTokenIssuer issuer = new LoginTokenIssuer(
                new RefreshTokenIssuer(),
                refreshSessionStore,
                new JwtTokenEncoder(jwtSupport.encoder()),
                tokenProperties
        );

        // when
        IssuedLoginTokens issued = issuer.issueLoginTokens(42L);

        // then
        String[] refreshParts = issued.refreshToken().split("\\.", 2);
        String key = "auth:refresh:" + refreshParts[0];
        String storedValue = redisTemplate.opsForValue().get(key);
        JsonNode stored = objectMapper.readTree(storedValue);
        assertThat(storedValue).doesNotContain(issued.refreshToken());
        assertThat(stored.get("tokenHash").asText()).isEqualTo(sha256(issued.refreshToken()));
        assertThat(stored.get("memberId").asLong()).isEqualTo(42L);
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isBetween(2_591_950L, 2_592_000L);
    }

    private SignupSessionStore signupSessionRegistry() {
        return new RedisSignupSessionStore(redisTemplate, objectMapper, tokenProperties);
    }

    private String sha256(String value) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(messageDigest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private void awaitAbsent(String key) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (Boolean.TRUE.equals(redisTemplate.hasKey(key)) && System.nanoTime() < deadline) {
            Thread.sleep(25L);
        }
        assertThat(redisTemplate.hasKey(key)).isFalse();
    }
}
