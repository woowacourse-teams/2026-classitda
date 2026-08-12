package com.classitda.authentication.integration;

import static com.classitda.support.AuthenticationIntegrationTestConfiguration.FIXED_OTP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.classitda.authentication.application.phone.OtpGenerator;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.application.phone.SmsSender;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationResponse;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.support.AuthenticationIntegrationTestConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(AuthenticationIntegrationTestConfiguration.class)
@ActiveProfiles("local")
@SpringBootTest(properties = "spring.sql.init.mode=always")
class PhoneVerificationConfirmIntegrationTest {

    private static final String WRONG_OTP = "000000";

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

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushDb();
        }
        given(otpGenerator.generate()).willReturn(FIXED_OTP);
    }

    @Test
    void 올바른_인증번호는_전화번호를_완료_상태로_옮긴다() {
        // given
        String signupJti = "success-signup";
        String phoneNumber = phoneNumber(1);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);

        // when
        confirm(signupJti, sent.verificationId(), FIXED_OTP);

        // then
        assertThat(redisTemplate.opsForValue().get("signup:verified-phone:" + signupJti))
                .isEqualTo(phoneNumber);
        assertThat(redisTemplate.hasKey("signup:phone-verification:" + sent.verificationId())).isFalse();
    }

    @Test
    void 다섯_번째_오답부터_인증_요청을_잠근다() {
        // given
        String signupJti = "attempt-signup";
        PhoneVerificationResponse sent = send(signupJti, phoneNumber(2));

        // when / then
        for (int attempt = 1; attempt <= 4; attempt++) {
            assertAuthError(
                    () -> confirm(signupJti, sent.verificationId(), WRONG_OTP),
                    AuthErrorCode.PHONE_OTP_INVALID);
        }
        assertAuthError(
                () -> confirm(signupJti, sent.verificationId(), WRONG_OTP),
                AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED);
        assertAuthError(
                () -> confirm(signupJti, sent.verificationId(), FIXED_OTP),
                AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED);
    }

    @Test
    void 동시_확인은_한_건만_성공한다() throws Exception {
        // given
        int requestCount = 10;
        String signupJti = "concurrent-signup";
        String phoneNumber = phoneNumber(3);
        PhoneVerificationResponse sent = send(signupJti, phoneNumber);

        // when
        List<AuthErrorCode> results = confirmConcurrently(
                requestCount,
                signupJti,
                sent.verificationId(),
                FIXED_OTP
        );

        // then
        assertThat(results.stream().filter(Objects::isNull).count()).isEqualTo(1L);
        assertThat(results.stream().filter(AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE::equals).count())
                .isEqualTo(9L);
        assertThat(redisTemplate.opsForValue().get("signup:verified-phone:" + signupJti))
                .isEqualTo(phoneNumber);
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
            int requestCount,
            String signupJti,
            String verificationId,
            String otp
    ) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<AuthErrorCode>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < requestCount; index++) {
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

    private String phoneNumber(int suffix) {
        return "+8210%08d".formatted(10_000_000 + suffix);
    }

}
