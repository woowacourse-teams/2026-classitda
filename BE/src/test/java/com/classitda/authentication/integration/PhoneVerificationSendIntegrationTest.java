package com.classitda.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.phone.OtpGenerator;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.application.phone.SmsSender;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.presentation.dto.PhoneVerificationResponse;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.fixture.MemberFixture;
import com.classitda.support.MySqlTestContainerConfiguration;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Import(MySqlTestContainerConfiguration.class)
@ActiveProfiles("local")
@Testcontainers
@SpringBootTest(properties = "spring.sql.init.mode=always")
class PhoneVerificationSendIntegrationTest {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String FIXED_OTP = "864209";
    private static final String KEY_MATERIAL = encodedBytes(32);
    private static final KeyPair JWT_KEY_PAIR = createKeyPair();

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private PhoneVerificationService phoneVerificationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private SmsSender smsSender;

    @MockitoBean
    private OtpGenerator otpGenerator;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("auth.sms.local.fixed-otp", () -> FIXED_OTP);
        registry.add("auth.phone.key-hmac-secret-base64", () -> KEY_MATERIAL);
        registry.add("auth.google.web-client-id", () -> "test-client");
        registry.add("auth.jwt.private-key-base64", () -> Base64.getEncoder()
                .encodeToString(JWT_KEY_PAIR.getPrivate().getEncoded()));
        registry.add("auth.jwt.public-key-base64", () -> Base64.getEncoder()
                .encodeToString(JWT_KEY_PAIR.getPublic().getEncoded()));
    }

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushDb();
        }
        given(otpGenerator.generate()).willReturn(FIXED_OTP);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    void local_발송은_검증_상태와_HMAC_digest를_180초와_60초로_저장한다() {
        // given
        String signupJti = "normal-signup";
        String phoneNumber = phoneNumber(1);

        // when
        PhoneVerificationResponse response = send(signupJti, phoneNumber);

        // then
        assertThat(response.expiresInSeconds()).isEqualTo(180L);
        assertThat(response.resendAfterSeconds()).isEqualTo(60L);
        String phoneHmac = hashPhone(phoneNumber);
        String verificationKey = verificationKey(response.verificationId());
        String activeKey = activeKey(signupJti, phoneHmac);
        String cooldownKey = cooldownKey(signupJti, phoneHmac);
        Map<Object, Object> stored = redisTemplate.opsForHash().entries(verificationKey);
        String expectedDigest = hashOtp(signupJti, response.verificationId(), phoneNumber, FIXED_OTP);
        assertThat(stored).hasSize(3);
        assertThat(stored.get("signupJti")).isEqualTo(signupJti);
        assertThat(stored.get("phoneNumber")).isEqualTo(phoneNumber);
        assertThat(stored.get("otpDigest")).isEqualTo(expectedDigest);
        assertThat(redisTemplate.opsForValue().get(activeKey)).isEqualTo(response.verificationId());
        assertThat(redisTemplate.getExpire(verificationKey, TimeUnit.SECONDS)).isBetween(175L, 180L);
        assertThat(redisTemplate.getExpire(activeKey, TimeUnit.SECONDS)).isBetween(175L, 180L);
        assertThat(redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS)).isBetween(55L, 60L);
        assertThat(redisTemplate.keys("signup:phone-*").stream()
                .noneMatch(key -> key.contains(phoneNumber))).isTrue();
    }

    @Test
    void 이미_가입된_번호는_PHONE_001이고_Redis와_sender를_사용하지_않는다() {
        // given
        memberRepository.saveAndFlush(MemberFixture.기본_회원());

        // when / then
        assertAuthError(
                () -> send("existing-signup", "+821012345678"),
                AuthErrorCode.PHONE_ALREADY_REGISTERED);
        assertThat(redisTemplate.keys("signup:phone-*")).isEmpty();
        verifyNoInteractions(smsSender);
    }

    @Test
    void 같은_가입자와_번호의_60초_내_재발송은_PHONE_002이다() {
        // given
        String signupJti = "cooldown-signup";
        String phoneNumber = phoneNumber(2);
        send(signupJti, phoneNumber);

        // when / then
        assertAuthError(
                () -> send(signupJti, phoneNumber),
                AuthErrorCode.PHONE_RESEND_COOLDOWN);
    }

    @Test
    void cooldown_후_재발송은_이전_verification을_소비하고_새_상태만_활성화한다() {
        // given
        String signupJti = "resend-signup";
        String phoneNumber = phoneNumber(32);
        PhoneVerificationResponse previous = send(signupJti, phoneNumber);
        String phoneHmac = hashPhone(phoneNumber);
        redisTemplate.delete(cooldownKey(signupJti, phoneHmac));

        // when
        PhoneVerificationResponse current = send(signupJti, phoneNumber);

        // then
        assertThat(redisTemplate.hasKey(verificationKey(previous.verificationId()))).isFalse();
        assertThat(redisTemplate.hasKey(verificationKey(current.verificationId()))).isTrue();
        assertThat(redisTemplate.opsForValue().get(activeKey(signupJti, phoneHmac)))
                .isEqualTo(current.verificationId());
    }

    @Test
    void 같은_가입자와_번호의_동시_발송은_한_건만_성공하고_나머지는_cooldown이다() throws Exception {
        // given
        int requestCount = 10;
        String phoneNumber = phoneNumber(40);
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<AuthErrorCode>> futures = new ArrayList<>();
        for (int index = 0; index < requestCount; index++) {
            futures.add(executor.submit(() -> concurrentSend(ready, start, phoneNumber)));
        }
        assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

        // when
        start.countDown();
        AtomicInteger admitted = new AtomicInteger();
        AtomicInteger cooledDown = new AtomicInteger();
        for (Future<AuthErrorCode> future : futures) {
            AuthErrorCode result = future.get(20, TimeUnit.SECONDS);
            if (result == null) {
                admitted.incrementAndGet();
            } else if (result == AuthErrorCode.PHONE_RESEND_COOLDOWN) {
                cooledDown.incrementAndGet();
            }
        }
        executor.shutdownNow();

        // then
        assertThat(admitted).hasValue(1);
        assertThat(cooledDown).hasValue(9);
    }

    @Test
    void provider_실패는_신규_상태를_소비하고_cooldown을_유지한다() {
        // given
        String signupJti = "failed-provider-signup";
        String phoneNumber = phoneNumber(60);
        String phoneHmac = hashPhone(phoneNumber);
        doThrow(new AuthException(AuthErrorCode.PHONE_DELIVERY_FAILED))
                .when(smsSender).send(phoneNumber, FIXED_OTP);

        // when / then
        assertAuthError(
                () -> send(signupJti, phoneNumber),
                AuthErrorCode.PHONE_DELIVERY_FAILED);
        assertThat(redisTemplate.keys("signup:phone-verification:*")).isEmpty();
        assertThat(redisTemplate.hasKey(activeKey(signupJti, phoneHmac))).isFalse();
        assertThat(redisTemplate.hasKey(cooldownKey(signupJti, phoneHmac))).isTrue();
        assertAuthError(
                () -> send(signupJti, phoneNumber),
                AuthErrorCode.PHONE_RESEND_COOLDOWN);
    }

    @Test
    void provider_실패_cleanup은_더_최신_active_pointer를_삭제하지_않는다() {
        // given
        String signupJti = "conditional-cleanup-signup";
        String phoneNumber = phoneNumber(61);
        String phoneHmac = hashPhone(phoneNumber);
        String newerVerificationId = "newer-verification";
        String newerVerificationKey = verificationKey(newerVerificationId);
        doAnswer(invocation -> {
            redisTemplate.opsForHash().putAll(newerVerificationKey, Map.of(
                    "signupJti", signupJti,
                    "phoneNumber", phoneNumber,
                    "otpDigest", "newer-digest"));
            redisTemplate.expire(newerVerificationKey, Duration.ofSeconds(180));
            redisTemplate.opsForValue().set(
                    activeKey(signupJti, phoneHmac),
                    newerVerificationId,
                    Duration.ofSeconds(180));
            throw new AuthException(AuthErrorCode.PHONE_DELIVERY_FAILED);
        }).when(smsSender).send(phoneNumber, FIXED_OTP);

        // when / then
        assertAuthError(
                () -> send(signupJti, phoneNumber),
                AuthErrorCode.PHONE_DELIVERY_FAILED);
        assertThat(redisTemplate.opsForValue().get(activeKey(signupJti, phoneHmac)))
                .isEqualTo(newerVerificationId);
        assertThat(redisTemplate.hasKey(newerVerificationKey)).isTrue();
    }

    private PhoneVerificationResponse send(String signupJti, String phoneNumber) {
        return phoneVerificationService.send(signupJti, phoneNumber);
    }

    private AuthErrorCode concurrentSend(
            CountDownLatch ready,
            CountDownLatch start,
            String phoneNumber
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            send("concurrent-signup", phoneNumber);
            return null;
        } catch (AuthException exception) {
            return (AuthErrorCode) exception.getErrorCode();
        }
    }

    private void assertAuthError(Runnable action, AuthErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorCode()).isEqualTo(expected));
    }

    private String phoneNumber(int suffix) {
        return "+8210%08d".formatted(10_000_000 + suffix);
    }

    private String hashPhone(String phoneNumber) {
        return hash("phone:" + phoneNumber);
    }

    private String hashOtp(
            String signupJti,
            String verificationId,
            String phoneNumber,
            String otp
    ) {
        return hash("otp:" + signupJti + ":" + verificationId + ":" + phoneNumber + ":" + otp);
    }

    private String hash(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(KEY_MATERIAL), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("테스트 HMAC을 계산할 수 없습니다.", exception);
        }
    }

    private String verificationKey(String verificationId) {
        return "signup:phone-verification:" + verificationId;
    }

    private String activeKey(String signupJti, String phoneHmac) {
        return "signup:phone-active:" + signupJti + ":" + phoneHmac;
    }

    private String cooldownKey(String signupJti, String phoneHmac) {
        return "signup:phone-cooldown:" + signupJti + ":" + phoneHmac;
    }

    private static String encodedBytes(int length) {
        byte[] bytes = new byte[length];
        for (int index = 0; index < length; index++) {
            bytes[index] = (byte) (index + 11);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static KeyPair createKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("테스트 JWT 키를 생성할 수 없습니다.", exception);
        }
    }
}
