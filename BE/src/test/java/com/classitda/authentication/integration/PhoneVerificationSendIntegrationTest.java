package com.classitda.authentication.integration;

import static com.classitda.support.AuthenticationIntegrationTestConfiguration.FIXED_OTP;
import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(AuthenticationIntegrationTestConfiguration.class)
@ActiveProfiles("local")
@SpringBootTest(properties = "spring.sql.init.mode=always")
class PhoneVerificationSendIntegrationTest {

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
    void 발송하면_인증_상태와_재발송_제한을_Redis에_저장한다() {
        // given
        String signupJti = "normal-signup";
        String phoneNumber = phoneNumber(1);

        // when
        PhoneVerificationResponse response = phoneVerificationService.send(signupJti, phoneNumber);

        // then
        Map<Object, Object> stored = redisTemplate.opsForHash()
                .entries("signup:phone-verification:" + response.verificationId());
        assertThat(stored)
                .containsEntry("signupJti", signupJti)
                .containsEntry("phoneNumber", phoneNumber);
        assertThat(stored.get("otpDigest")).asString().isNotBlank().isNotEqualTo(FIXED_OTP);
        assertThat(keys("signup:phone-active:" + signupJti + ":*")).singleElement();
        assertThat(keys("signup:phone-cooldown:" + signupJti + ":*")).singleElement();
    }

    @Test
    void 동시_발송은_한_건만_성공한다() throws Exception {
        // given
        int requestCount = 10;
        String phoneNumber = phoneNumber(2);

        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<AuthErrorCode>> futures = new ArrayList<>();

        try {
            for (int index = 0; index < requestCount; index++) {
                futures.add(executor.submit(() -> concurrentSend(ready, start, phoneNumber)));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

            // when
            start.countDown();
            List<AuthErrorCode> results = new ArrayList<>();
            for (Future<AuthErrorCode> future : futures) {
                results.add(future.get(20, TimeUnit.SECONDS));
            }

            // then
            assertThat(results).filteredOn(Objects::isNull).hasSize(1);
            assertThat(results).filteredOn(AuthErrorCode.PHONE_RESEND_COOLDOWN::equals).hasSize(9);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private AuthErrorCode concurrentSend(CountDownLatch ready, CountDownLatch start, String phoneNumber) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            phoneVerificationService.send("concurrent-signup", phoneNumber);
            return null;
        } catch (AuthException exception) {
            return (AuthErrorCode) exception.getErrorCode();
        }
    }

    private Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    private String phoneNumber(int suffix) {
        return "010%08d".formatted(10_000_000 + suffix);
    }

}
