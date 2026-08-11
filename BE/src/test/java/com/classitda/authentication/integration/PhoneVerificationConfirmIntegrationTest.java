package com.classitda.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

import com.classitda.authentication.application.phone.OtpGenerator;
import com.classitda.authentication.application.phone.PhoneVerificationHasher;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.application.phone.SmsSender;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.presentation.dto.PhoneVerificationResponse;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.exception.ErrorResponse;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.support.MySqlTestContainerConfiguration;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Import(MySqlTestContainerConfiguration.class)
@ActiveProfiles("local")
@ExtendWith(OutputCaptureExtension.class)
@Testcontainers
@SpringBootTest(properties = "spring.sql.init.mode=always")
class PhoneVerificationConfirmIntegrationTest {

    private static final String FIXED_OTP = "864209";
    private static final String WRONG_OTP = "000000";
    private static final String KEY_MATERIAL = encodedBytes(32);
    private static final KeyPair JWT_KEY_PAIR = createKeyPair();

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private PhoneVerificationService phoneVerificationService;

    @Autowired
    private PhoneVerificationHasher phoneVerificationHasher;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @MockitoBean
    private SmsSender smsSender;

    @MockitoBean
    private OtpGenerator otpGenerator;

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

    @Test
    void 올바른_인증번호는_완료_전화번호를_1800초로_저장하고_현재_상태만_소비한다() {
        // given
        String signupJti = "success-signup";
        String phoneNumber = phoneNumber(1);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        String verificationKey = verificationKey(sent.verificationId());
        String activeKey = activeKey(signupJti, phoneHmac);
        String cooldownKey = cooldownKey(signupJti, phoneHmac);
        assertThat(redisTemplate.opsForHash().entries(verificationKey).values()).doesNotContain(FIXED_OTP);

        // when
        confirm(signupJti, sent.verificationId(), FIXED_OTP);

        // then
        String verifiedPhoneKey = verifiedPhoneKey(signupJti);
        assertThat(redisTemplate.opsForValue().get(verifiedPhoneKey)).isEqualTo(phoneNumber);
        assertThat(redisTemplate.getExpire(verifiedPhoneKey, TimeUnit.SECONDS)).isBetween(1_795L, 1_800L);
        assertThat(redisTemplate.hasKey(verificationKey)).isFalse();
        assertThat(redisTemplate.hasKey(activeKey)).isFalse();
        assertThat(redisTemplate.hasKey(cooldownKey)).isTrue();
        assertThat(redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS)).isPositive();
        assertThat(redisTemplate.keys("*")).allMatch(key -> !key.contains(FIXED_OTP));
    }

    @Test
    void 첫_네_번의_오답은_횟수를_늘리고_다섯_번째부터_잠기며_TTL을_갱신하지_않는다() {
        // given
        String signupJti = "attempt-signup";
        String phoneNumber = phoneNumber(2);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);
        String verificationKey = verificationKey(sent.verificationId());
        redisTemplate.expire(verificationKey, Duration.ofSeconds(30));

        // when / then
        for (int attempt = 1; attempt <= 4; attempt++) {
            assertAuthError(
                    () -> confirm(signupJti, sent.verificationId(), WRONG_OTP),
                    AuthErrorCode.PHONE_OTP_INVALID);
            assertThat(redisTemplate.opsForHash().get(verificationKey, "failedAttempts"))
                    .isEqualTo(String.valueOf(attempt));
        }
        assertThat(redisTemplate.getExpire(verificationKey, TimeUnit.SECONDS)).isBetween(25L, 30L);

        assertAuthError(
                () -> confirm(signupJti, sent.verificationId(), WRONG_OTP),
                AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED);
        assertThat(redisTemplate.opsForHash().get(verificationKey, "failedAttempts")).isEqualTo("5");

        assertAuthError(
                () -> confirm(signupJti, sent.verificationId(), FIXED_OTP),
                AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED);
        assertThat(redisTemplate.opsForHash().get(verificationKey, "failedAttempts")).isEqualTo("5");
        assertThat(redisTemplate.hasKey(verifiedPhoneKey(signupJti))).isFalse();
    }

    @Test
    void 재발송한_새_인증_요청은_이전_오답_횟수를_이어받지_않는다() {
        // given
        String signupJti = "reset-signup";
        String phoneNumber = phoneNumber(3);
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        PhoneVerificationResponse previous = send(signupJti, phoneNumber);
        for (int attempt = 0; attempt < 5; attempt++) {
            AuthErrorCode expected = attempt < 4
                    ? AuthErrorCode.PHONE_OTP_INVALID
                    : AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED;
            assertAuthError(() -> confirm(signupJti, previous.verificationId(), WRONG_OTP), expected);
        }
        redisTemplate.delete(cooldownKey(signupJti, phoneHmac));

        // when
        PhoneVerificationResponse current = send(signupJti, phoneNumber);

        // then
        assertThat(redisTemplate.hasKey(verificationKey(previous.verificationId()))).isFalse();
        assertThat(redisTemplate.opsForHash().hasKey(
                verificationKey(current.verificationId()),
                "failedAttempts"
        )).isFalse();
        assertAuthError(
                () -> confirm(signupJti, current.verificationId(), WRONG_OTP),
                AuthErrorCode.PHONE_OTP_INVALID);
        assertThat(redisTemplate.opsForHash().get(
                verificationKey(current.verificationId()),
                "failedAttempts"
        )).isEqualTo("1");
    }

    @Test
    void 만료된_인증_요청은_PHONE_003이고_완료_상태를_만들지_않는다() throws InterruptedException {
        // given
        String signupJti = "expired-signup";
        String phoneNumber = phoneNumber(4);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        String verificationKey = verificationKey(sent.verificationId());
        String activeKey = activeKey(signupJti, phoneHmac);
        redisTemplate.expire(verificationKey, Duration.ofMillis(100));
        redisTemplate.expire(activeKey, Duration.ofMillis(100));
        awaitAbsent(verificationKey);
        awaitAbsent(activeKey);

        // when / then
        assertAuthError(
                () -> confirm(signupJti, sent.verificationId(), FIXED_OTP),
                AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE);
        assertThat(redisTemplate.hasKey(verifiedPhoneKey(signupJti))).isFalse();
    }

    @Test
    void 다른_가입_세션은_인증_상태와_오답_횟수를_변경하지_않는다() {
        // given
        String ownerJti = "owner-signup";
        String otherJti = "other-signup";
        String phoneNumber = phoneNumber(5);
        PhoneVerificationResponse sent = send(ownerJti, phoneNumber);
        String verificationKey = verificationKey(sent.verificationId());
        String activeKey = activeKey(ownerJti, phoneVerificationHasher.hashPhoneNumber(phoneNumber));
        Map<Object, Object> before = redisTemplate.opsForHash().entries(verificationKey);

        // when / then
        assertAuthError(
                () -> confirm(otherJti, sent.verificationId(), WRONG_OTP),
                AuthErrorCode.PHONE_VERIFICATION_SESSION_MISMATCH);
        assertThat(redisTemplate.opsForHash().entries(verificationKey)).isEqualTo(before);
        assertThat(redisTemplate.opsForValue().get(activeKey)).isEqualTo(sent.verificationId());
        assertThat(redisTemplate.hasKey(verifiedPhoneKey(ownerJti))).isFalse();
        assertThat(redisTemplate.hasKey(verifiedPhoneKey(otherJti))).isFalse();

        confirm(ownerJti, sent.verificationId(), FIXED_OTP);
        assertThat(redisTemplate.opsForValue().get(verifiedPhoneKey(ownerJti))).isEqualTo(phoneNumber);
    }

    @Test
    void 재발송으로_무효화된_이전_인증_요청은_PHONE_003이고_현재_요청은_유효하다() {
        // given
        String signupJti = "resend-invalidated-signup";
        String phoneNumber = phoneNumber(6);
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        PhoneVerificationResponse previous = send(signupJti, phoneNumber);
        redisTemplate.delete(cooldownKey(signupJti, phoneHmac));
        PhoneVerificationResponse current = send(signupJti, phoneNumber);

        // when / then
        assertAuthError(
                () -> confirm(signupJti, previous.verificationId(), FIXED_OTP),
                AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE);
        assertThat(redisTemplate.opsForValue().get(activeKey(signupJti, phoneHmac)))
                .isEqualTo(current.verificationId());
        assertThat(redisTemplate.hasKey(verificationKey(current.verificationId()))).isTrue();

        confirm(signupJti, current.verificationId(), FIXED_OTP);
        assertThat(redisTemplate.opsForValue().get(verifiedPhoneKey(signupJti))).isEqualTo(phoneNumber);
    }

    @Test
    void 성공한_인증_요청을_다시_사용하면_PHONE_003이고_완료_전화번호는_유지된다() {
        // given
        String signupJti = "single-use-signup";
        String phoneNumber = phoneNumber(7);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);
        confirm(signupJti, sent.verificationId(), FIXED_OTP);

        // when / then
        assertAuthError(
                () -> confirm(signupJti, sent.verificationId(), FIXED_OTP),
                AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE);
        assertThat(redisTemplate.opsForValue().get(verifiedPhoneKey(signupJti))).isEqualTo(phoneNumber);
    }

    @Test
    void 같은_인증_요청의_동시_확인은_한_건만_성공하고_나머지는_PHONE_003이다() throws Exception {
        // given
        int requestCount = 10;
        String signupJti = "same-id-signup";
        String phoneNumber = phoneNumber(8);
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);

        // when
        List<AuthErrorCode> results = confirmConcurrently(
                signupJti,
                Collections.nCopies(requestCount, sent.verificationId()),
                Collections.nCopies(requestCount, FIXED_OTP)
        );

        // then
        assertThat(results.stream().filter(Objects::isNull).count()).isEqualTo(1L);
        assertThat(results.stream()
                .filter(result -> result == AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE)
                .count()).isEqualTo(9L);
        assertThat(redisTemplate.opsForValue().get(verifiedPhoneKey(signupJti))).isEqualTo(phoneNumber);
        assertThat(redisTemplate.hasKey(verificationKey(sent.verificationId()))).isFalse();
        assertThat(redisTemplate.hasKey(activeKey(signupJti, phoneHmac))).isFalse();
        assertThat(redisTemplate.hasKey(cooldownKey(signupJti, phoneHmac))).isTrue();
    }

    @Test
    void 같은_가입_세션의_서로_다른_인증_요청도_첫_성공_하나만_완료_전화번호를_선점한다() throws Exception {
        // given
        String signupJti = "distinct-id-signup";
        String firstPhone = phoneNumber(9);
        String secondPhone = phoneNumber(10);
        PhoneVerificationResponse first = send(signupJti, firstPhone);
        PhoneVerificationResponse second = send(signupJti, secondPhone);

        // when
        List<AuthErrorCode> results = confirmConcurrently(
                signupJti,
                List.of(first.verificationId(), second.verificationId()),
                List.of(FIXED_OTP, FIXED_OTP)
        );

        // then
        assertThat(results.stream().filter(Objects::isNull).count()).isEqualTo(1L);
        assertThat(results.stream()
                .filter(result -> result == AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE)
                .count()).isEqualTo(1L);
        assertThat(redisTemplate.opsForValue().get(verifiedPhoneKey(signupJti)))
                .isIn(firstPhone, secondPhone);
        assertConsumedWithCooldown(signupJti, firstPhone, first.verificationId());
        assertConsumedWithCooldown(signupJti, secondPhone, second.verificationId());
    }

    @Test
    void 동시_오답은_한도를_넘겨_증가하지_않고_다섯_번째부터_PHONE_005이다() throws Exception {
        // given
        int requestCount = 10;
        String signupJti = "concurrent-wrong-signup";
        String phoneNumber = phoneNumber(11);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);

        // when
        List<AuthErrorCode> results = confirmConcurrently(
                signupJti,
                Collections.nCopies(requestCount, sent.verificationId()),
                Collections.nCopies(requestCount, WRONG_OTP)
        );

        // then
        assertThat(results.stream()
                .filter(result -> result == AuthErrorCode.PHONE_OTP_INVALID)
                .count()).isEqualTo(4L);
        assertThat(results.stream()
                .filter(result -> result == AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED)
                .count()).isEqualTo(6L);
        assertThat(redisTemplate.opsForHash().get(
                verificationKey(sent.verificationId()),
                "failedAttempts"
        )).isEqualTo("5");
        assertThat(redisTemplate.hasKey(verifiedPhoneKey(signupJti))).isFalse();
    }

    @Test
    void 성공과_오답이_경합해도_한_번만_성공하고_일관된_상태만_남는다() throws Exception {
        // given
        String signupJti = "success-wrong-race-signup";
        String phoneNumber = phoneNumber(12);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);

        // when
        List<AuthErrorCode> results = confirmConcurrently(
                signupJti,
                List.of(sent.verificationId(), sent.verificationId()),
                List.of(FIXED_OTP, WRONG_OTP)
        );

        // then
        assertThat(results.stream().filter(Objects::isNull).count()).isEqualTo(1L);
        assertThat(results.stream().filter(Objects::nonNull).findFirst())
                .hasValueSatisfying(result -> assertThat(result).isIn(
                        AuthErrorCode.PHONE_OTP_INVALID,
                        AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE
                ));
        assertThat(redisTemplate.opsForValue().get(verifiedPhoneKey(signupJti))).isEqualTo(phoneNumber);
        assertThat(redisTemplate.hasKey(verificationKey(sent.verificationId()))).isFalse();
    }

    @Test
    void 손상된_Redis_상태의_오류_응답과_로그에는_민감정보가_노출되지_않는다(CapturedOutput output) {
        // given
        String signupJti = "sensitive-signup";
        String phoneNumber = phoneNumber(13);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);
        String verificationKey = verificationKey(sent.verificationId());
        String otpDigest = (String) redisTemplate.opsForHash().get(verificationKey, "otpDigest");
        redisTemplate.opsForHash().delete(verificationKey, "otpDigest");

        // when
        Throwable exception = catchThrowable(
                () -> confirm(signupJti, sent.verificationId(), FIXED_OTP)
        );
        ResponseEntity<ErrorResponse> result = globalExceptionHandler.handleUnexpectedException(
                (Exception) exception
        );

        // then
        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("휴대전화 인증번호 확인 상태가 올바르지 않습니다.");
        assertThat(result.getStatusCode().value()).isEqualTo(500);
        assertThat(result.getBody()).isEqualTo(ErrorResponse.from(CommonErrorCode.INTERNAL_SERVER_ERROR));
        assertThat(output.getAll())
                .contains(
                        "처리되지 않은 예외가 발생했습니다. exceptionType=java.lang.IllegalStateException",
                        "휴대전화 인증번호 확인 상태가 올바르지 않습니다."
                )
                .doesNotContain(phoneNumber, FIXED_OTP, otpDigest);
    }

    @Test
    void 형식이_손상된_전화번호는_Redis_상태를_변경하지_않고_민감정보_없는_500으로_처리한다(CapturedOutput output) {
        // given
        String signupJti = "malformed-phone-signup";
        String phoneNumber = phoneNumber(14);
        String malformedPhoneNumber = "+8210-1234-5678";
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);
        String verificationKey = verificationKey(sent.verificationId());
        String otpDigest = (String) redisTemplate.opsForHash().get(verificationKey, "otpDigest");
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        String activeKey = activeKey(signupJti, phoneHmac);
        String cooldownKey = cooldownKey(signupJti, phoneHmac);
        redisTemplate.opsForHash().put(verificationKey, "phoneNumber", malformedPhoneNumber);
        Map<Object, Object> corruptedState = redisTemplate.opsForHash().entries(verificationKey);

        // when
        Throwable exception = catchThrowable(
                () -> confirm(signupJti, sent.verificationId(), WRONG_OTP)
        );
        ResponseEntity<ErrorResponse> result = globalExceptionHandler.handleUnexpectedException(
                (Exception) exception
        );

        // then
        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("휴대전화 인증번호 확인 상태가 올바르지 않습니다.");
        assertThat(result.getStatusCode().value()).isEqualTo(500);
        assertThat(result.getBody()).isEqualTo(ErrorResponse.from(CommonErrorCode.INTERNAL_SERVER_ERROR));
        assertThat(redisTemplate.opsForHash().entries(verificationKey)).isEqualTo(corruptedState);
        assertThat(redisTemplate.opsForHash().hasKey(verificationKey, "failedAttempts")).isFalse();
        assertThat(redisTemplate.opsForValue().get(activeKey)).isEqualTo(sent.verificationId());
        assertThat(redisTemplate.hasKey(cooldownKey)).isTrue();
        assertThat(redisTemplate.hasKey(verifiedPhoneKey(signupJti))).isFalse();
        assertThat(output.getAll())
                .contains(
                        "처리되지 않은 예외가 발생했습니다. exceptionType=java.lang.IllegalStateException",
                        "휴대전화 인증번호 확인 상태가 올바르지 않습니다."
                )
                .doesNotContain(phoneNumber, malformedPhoneNumber, FIXED_OTP, WRONG_OTP, otpDigest);
    }

    @Test
    void 형식이_손상된_OTP_digest는_Redis_상태를_변경하지_않고_민감정보_없는_500으로_처리한다(CapturedOutput output) {
        // given
        String signupJti = "malformed-digest-signup";
        String phoneNumber = phoneNumber(15);
        String malformedOtpDigest = "A".repeat(64);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);
        String verificationKey = verificationKey(sent.verificationId());
        String originalOtpDigest = (String) redisTemplate.opsForHash().get(verificationKey, "otpDigest");
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        String activeKey = activeKey(signupJti, phoneHmac);
        String cooldownKey = cooldownKey(signupJti, phoneHmac);
        redisTemplate.opsForHash().put(verificationKey, "otpDigest", malformedOtpDigest);
        Map<Object, Object> corruptedState = redisTemplate.opsForHash().entries(verificationKey);

        // when
        Throwable exception = catchThrowable(
                () -> confirm(signupJti, sent.verificationId(), WRONG_OTP)
        );
        ResponseEntity<ErrorResponse> result = globalExceptionHandler.handleUnexpectedException(
                (Exception) exception
        );

        // then
        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("휴대전화 인증번호 확인 상태가 올바르지 않습니다.");
        assertThat(result.getStatusCode().value()).isEqualTo(500);
        assertThat(result.getBody()).isEqualTo(ErrorResponse.from(CommonErrorCode.INTERNAL_SERVER_ERROR));
        assertThat(redisTemplate.opsForHash().entries(verificationKey)).isEqualTo(corruptedState);
        assertThat(redisTemplate.opsForHash().hasKey(verificationKey, "failedAttempts")).isFalse();
        assertThat(redisTemplate.opsForValue().get(activeKey)).isEqualTo(sent.verificationId());
        assertThat(redisTemplate.hasKey(cooldownKey)).isTrue();
        assertThat(redisTemplate.hasKey(verifiedPhoneKey(signupJti))).isFalse();
        assertThat(output.getAll())
                .contains(
                        "처리되지 않은 예외가 발생했습니다. exceptionType=java.lang.IllegalStateException",
                        "휴대전화 인증번호 확인 상태가 올바르지 않습니다."
                )
                .doesNotContain(
                        phoneNumber,
                        FIXED_OTP,
                        WRONG_OTP,
                        originalOtpDigest,
                        malformedOtpDigest
                );
    }

    private PhoneVerificationResponse send(String signupJti, String phoneNumber) {
        return phoneVerificationService.send(signupJti, phoneNumber);
    }

    private void confirm(String signupJti, String verificationId, String otp) {
        phoneVerificationService.confirm(signupJti, verificationId, otp);
    }

    private void assertAuthError(Runnable action, AuthErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorCode()).isEqualTo(expected));
    }

    private List<AuthErrorCode> confirmConcurrently(
            String signupJti,
            List<String> verificationIds,
            List<String> otps
    ) throws Exception {
        assertThat(verificationIds).hasSameSizeAs(otps);
        int requestCount = verificationIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<AuthErrorCode>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < requestCount; index++) {
                String verificationId = verificationIds.get(index);
                String otp = otps.get(index);
                futures.add(executor.submit(() -> concurrentConfirm(
                        ready,
                        start,
                        signupJti,
                        verificationId,
                        otp
                )));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<AuthErrorCode> results = new ArrayList<>();
            for (Future<AuthErrorCode> future : futures) {
                results.add(future.get(20, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private AuthErrorCode concurrentConfirm(
            CountDownLatch ready,
            CountDownLatch start,
            String signupJti,
            String verificationId,
            String otp
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            confirm(signupJti, verificationId, otp);
            return null;
        } catch (AuthException exception) {
            return (AuthErrorCode) exception.getErrorCode();
        }
    }

    private void assertConsumedWithCooldown(
            String signupJti,
            String phoneNumber,
            String verificationId
    ) {
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(phoneNumber);
        assertThat(redisTemplate.hasKey(verificationKey(verificationId))).isFalse();
        assertThat(redisTemplate.hasKey(activeKey(signupJti, phoneHmac))).isFalse();
        assertThat(redisTemplate.hasKey(cooldownKey(signupJti, phoneHmac))).isTrue();
    }

    private void awaitAbsent(String key) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (Boolean.TRUE.equals(redisTemplate.hasKey(key)) && System.nanoTime() < deadline) {
            Thread.sleep(25L);
        }
        assertThat(redisTemplate.hasKey(key)).isFalse();
    }

    private String phoneNumber(int suffix) {
        return "+8210%08d".formatted(10_000_000 + suffix);
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

    private String verifiedPhoneKey(String signupJti) {
        return "signup:verified-phone:" + signupJti;
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
