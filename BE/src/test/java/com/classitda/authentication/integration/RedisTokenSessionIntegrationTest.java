package com.classitda.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.authentication.application.session.SignupSessionRegistry;
import com.classitda.authentication.application.token.IssuedLoginTokens;
import com.classitda.authentication.application.token.IssuedSignupToken;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.SignupTokenIssuer;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.infra.security.jwt.JwtContract;
import com.classitda.authentication.infra.security.jwt.JwtTokenEncoder;
import com.classitda.authentication.infra.security.jwt.SignupSessionJwtValidator;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import com.classitda.authentication.support.JwtTestSupport;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
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
    void 가입_세션을_약속된_key_JSON_TTL로_저장한다() throws Exception {
        // given
        SignupSessionRegistry registry = signupSessionRegistry();
        String signupJti = "signup-jti";

        // when
        registry.save(signupJti, OauthProvider.GOOGLE, "provider-subject", "member@example.com");

        // then
        String key = "signup:session:" + signupJti;
        String storedValue = redisTemplate.opsForValue().get(key);
        JsonNode json = objectMapper.readTree(storedValue);
        assertThat(json.get("provider").asText()).isEqualTo("GOOGLE");
        assertThat(json.get("providerSubject").asText()).isEqualTo("provider-subject");
        assertThat(json.get("providerEmail").asText()).isEqualTo("member@example.com");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isBetween(1750L, 1800L);
    }

    @Test
    void 회원가입_토큰_JWT를_발급하면_같은_jti의_30분_활성_가입_세션을_생성한다() throws Exception {
        // given
        JwtTestSupport jwtSupport = JwtTestSupport.create();
        SignupSessionRegistry registry = signupSessionRegistry();
        SignupSessionJwtValidator validator = new SignupSessionJwtValidator(registry);
        SignupTokenIssuer issuer = new SignupTokenIssuer(
                registry,
                new JwtTokenEncoder(jwtSupport.encoder()),
                tokenProperties);

        // when
        IssuedSignupToken issuedToken = issuer.issueSignupToken(
                OauthProvider.GOOGLE,
                "provider-subject",
                "member@example.com");
        Jwt signupJwt = jwtSupport.decoder(validator).decode(issuedToken.signupToken());

        // then
        assertThat(signupJwt.getHeaders()).containsEntry("alg", "RS256");
        assertThat(signupJwt.getClaimAsString("iss")).isEqualTo(JwtContract.ISSUER);
        assertThat(signupJwt.getClaimAsString(JwtContract.TOKEN_USE_CLAIM)).isEqualTo(TokenUse.SIGNUP.name());
        assertThat(signupJwt.getId()).isNotBlank();
        assertThat(signupJwt.getSubject()).isEqualTo(signupJwt.getId());
        assertThat(Duration.between(signupJwt.getIssuedAt(), signupJwt.getExpiresAt())).isEqualTo(SIGNUP_TTL);
        assertThat(issuedToken.signupTokenExpiresIn()).isEqualTo(SIGNUP_TTL.toSeconds());

        String key = "signup:session:" + signupJwt.getId();
        String storedValue = redisTemplate.opsForValue().get(key);
        JsonNode json = objectMapper.readTree(storedValue);
        assertThat(json.get("provider").asText()).isEqualTo("GOOGLE");
        assertThat(json.get("providerSubject").asText()).isEqualTo("provider-subject");
        assertThat(json.get("providerEmail").asText()).isEqualTo("member@example.com");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isBetween(1750L, 1800L);
    }

    @Test
    void 활성_가입_세션과_일치하는_회원가입_토큰_JWT는_유효하다() {
        // given
        SignupSessionRegistry registry = signupSessionRegistry();
        SignupSessionJwtValidator validator = new SignupSessionJwtValidator(registry);
        registry.save("signup-jti", OauthProvider.GOOGLE, "provider-subject", "member@example.com");
        Jwt jwt = signupJwt("signup-jti", "signup-jti");

        // when
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // then
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void 가입_세션이_없으면_회원가입_토큰_JWT를_거부한다() {
        // given
        SignupSessionJwtValidator validator = new SignupSessionJwtValidator(signupSessionRegistry());
        Jwt jwt = signupJwt("missing-jti", "missing-jti");

        // when
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting("description")
                .containsExactly("활성 가입 세션이 존재하지 않습니다.");
    }

    @Test
    void 회원가입_토큰_JWT의_jti와_sub가_다르면_거부한다() {
        // given
        SignupSessionRegistry registry = signupSessionRegistry();
        SignupSessionJwtValidator validator = new SignupSessionJwtValidator(registry);
        registry.save("signup-jti", OauthProvider.GOOGLE, "provider-subject", "member@example.com");
        Jwt jwt = signupJwt("signup-jti", "different-subject");

        // when
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting("description")
                .containsExactly("가입 토큰 식별자(jti/sub)가 올바르지 않습니다.");
    }

    @Test
    void 로그인_토큰은_15분_액세스_토큰_JWT와_digest만_저장한_30일_리프레시_토큰을_발급한다() throws Exception {
        // given
        JwtTestSupport jwtSupport = JwtTestSupport.create();
        SignupSessionJwtValidator signupValidator = new SignupSessionJwtValidator(signupSessionRegistry());
        LoginTokenIssuer issuer = new LoginTokenIssuer(
                redisTemplate,
                objectMapper,
                new JwtTokenEncoder(jwtSupport.encoder()),
                tokenProperties);
        Instant issuedAfter = Instant.now();

        // when
        IssuedLoginTokens issuedTokens = issuer.issueLoginTokens(42L);
        Jwt accessJwt = jwtSupport.decoder(signupValidator).decode(issuedTokens.accessToken());

        // then
        assertThat(Duration.between(accessJwt.getIssuedAt(), accessJwt.getExpiresAt())).isEqualTo(ACCESS_TTL);
        assertThat(accessJwt.getSubject()).isEqualTo("42");
        assertThat(accessJwt.getClaimAsString(JwtContract.TOKEN_USE_CLAIM)).isEqualTo(TokenUse.ACCESS.name());
        assertThat(issuedTokens.accessTokenExpiresIn()).isEqualTo(ACCESS_TTL.toSeconds());
        assertThat(issuedTokens.refreshTokenExpiresIn()).isEqualTo(REFRESH_TTL.toSeconds());

        String[] refreshParts = issuedTokens.refreshToken().split("\\.", 2);
        assertThat(refreshParts).hasSize(2).allSatisfy(part -> assertThat(part).isNotBlank());
        String key = "auth:refresh:" + refreshParts[0];
        String storedValue = redisTemplate.opsForValue().get(key);
        JsonNode json = objectMapper.readTree(storedValue);
        assertThat(storedValue).doesNotContain(issuedTokens.refreshToken());
        assertThat(json.get("tokenDigest").asText()).isEqualTo(sha256(issuedTokens.refreshToken()));
        assertThat(json.get("memberId").asLong()).isEqualTo(42L);
        assertThat(json.get("expiresAtEpochSecond").asLong())
                .isBetween(issuedAfter.plus(REFRESH_TTL).minusSeconds(2).getEpochSecond(),
                        Instant.now().plus(REFRESH_TTL).plusSeconds(2).getEpochSecond());
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isBetween(2591950L, 2592000L);
    }

    private SignupSessionRegistry signupSessionRegistry() {
        return new SignupSessionRegistry(redisTemplate, objectMapper, tokenProperties);
    }

    private Jwt signupJwt(String jti, String subject) {
        Instant issuedAt = Instant.now();
        return Jwt.withTokenValue("signup-token")
                .header("alg", "RS256")
                .issuer(JwtContract.ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(SIGNUP_TTL))
                .claim("jti", jti)
                .subject(subject)
                .claim(JwtContract.TOKEN_USE_CLAIM, TokenUse.SIGNUP.name())
                .build();
    }

    private String sha256(String value) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(messageDigest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
