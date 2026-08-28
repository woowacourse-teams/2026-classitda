package com.classitda.authentication.infra.phone;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PhoneVerificationConfigTest {

    private static final String VALID_HMAC_KEY = encodedKey(32);
    private static final String HMAC_REQUIRED_MESSAGE = "휴대전화 인증 HMAC 키 설정은 필수입니다.";
    private static final String HMAC_BASE64_MESSAGE =
            "휴대전화 인증 HMAC 키 설정이 올바른 Base64 형식이 아닙니다.";
    private static final String HMAC_LENGTH_MESSAGE = "휴대전화 인증 HMAC 키는 32바이트 이상이어야 합니다.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PhoneVerificationConfig.class)
            .withPropertyValues("auth.phone.key-hmac-secret-base64=" + VALID_HMAC_KEY);

    @Test
    void HMAC_키가_누락되면_context_기동에_실패한다() {
        // given
        ApplicationContextRunner missingKeyContext = nonLocalContextRunnerWithoutHmacKey();

        // when
        missingKeyContext.run(context -> {
            // then
            assertHmacStartupFailure(context.getStartupFailure(), HMAC_REQUIRED_MESSAGE, null);
        });
    }

    @Test
    void HMAC_키가_blank이면_context_기동에_실패한다() {
        // given
        ApplicationContextRunner blankKeyContext = nonLocalContextRunnerWithoutHmacKey()
                .withPropertyValues("auth.phone.key-hmac-secret-base64=   ");

        // when
        blankKeyContext.run(context -> {
            // then
            assertHmacStartupFailure(context.getStartupFailure(), HMAC_REQUIRED_MESSAGE, null);
        });
    }

    @Test
    void HMAC_키가_Base64가_아니면_값을_노출하지_않고_context_기동에_실패한다() {
        // given
        String invalidKey = "%%%";
        ApplicationContextRunner invalidKeyContext = nonLocalContextRunnerWithoutHmacKey()
                .withPropertyValues("auth.phone.key-hmac-secret-base64=" + invalidKey);

        // when
        invalidKeyContext.run(context -> {
            // then
            assertHmacStartupFailure(context.getStartupFailure(), HMAC_BASE64_MESSAGE, invalidKey);
        });
    }

    @Test
    void HMAC_키가_31바이트이면_값을_노출하지_않고_context_기동에_실패한다() {
        // given
        String shortKey = encodedKey(31);
        ApplicationContextRunner shortKeyContext = nonLocalContextRunnerWithoutHmacKey()
                .withPropertyValues("auth.phone.key-hmac-secret-base64=" + shortKey);

        // when
        shortKeyContext.run(context -> {
            // then
            assertHmacStartupFailure(context.getStartupFailure(), HMAC_LENGTH_MESSAGE, shortKey);
        });
    }

    @Test
    void HMAC_키가_32바이트이면_SecretKey_하나로_binding된다() {
        // given
        ApplicationContextRunner validKeyContext = nonLocalContextRunner();

        // when
        validKeyContext.run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SecretKey.class);
            assertThat(context.getBean(SecretKey.class).getAlgorithm()).isEqualTo("HmacSHA256");
            assertThat(context.getBean(SecretKey.class).getEncoded())
                    .containsExactly(Base64.getDecoder().decode(VALID_HMAC_KEY));
        });
    }

    private ApplicationContextRunner nonLocalContextRunner() {
        return contextRunner.withPropertyValues("spring.profiles.active=prod");
    }

    private ApplicationContextRunner nonLocalContextRunnerWithoutHmacKey() {
        return new ApplicationContextRunner()
                .withUserConfiguration(PhoneVerificationConfig.class)
                .withPropertyValues("spring.profiles.active=prod");
    }

    private void assertHmacStartupFailure(
            Throwable startupFailure,
            String expectedMessage,
            String sensitiveValue
    ) {
        assertThat(startupFailure)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage(expectedMessage);

        Throwable current = startupFailure;
        while (current != null) {
            String message = current.getMessage();
            assertThat(message == null || sensitiveValue == null || !message.contains(sensitiveValue)).isTrue();
            current = current.getCause();
        }
    }

    private static String encodedKey(int length) {
        byte[] key = new byte[length];
        for (int index = 0; index < key.length; index++) {
            key[index] = (byte) (index + 1);
        }
        return Base64.getEncoder().encodeToString(key);
    }
}
