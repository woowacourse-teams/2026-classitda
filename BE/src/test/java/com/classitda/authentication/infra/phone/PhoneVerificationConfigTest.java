package com.classitda.authentication.infra.phone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.classitda.authentication.application.phone.OtpGenerator;
import com.classitda.authentication.application.phone.SmsSender;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.sms.LocalNoopSmsSender;
import com.classitda.authentication.infra.sms.UnavailableSmsSender;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;

class PhoneVerificationConfigTest {

    private static final String FIXED_OTP = "135790";
    private static final String VALID_HMAC_KEY = encodedKey(32);
    private static final String LOCAL_VALIDATION_MESSAGE = "local SMS 고정 인증번호는 숫자 6자리여야 합니다.";
    private static final String NON_LOCAL_VALIDATION_MESSAGE =
            "non-local profile에서는 local SMS 고정 인증번호를 사용할 수 없습니다.";
    private static final String HMAC_REQUIRED_MESSAGE = "휴대전화 인증 HMAC 키 설정은 필수입니다.";
    private static final String HMAC_BASE64_MESSAGE =
            "휴대전화 인증 HMAC 키 설정이 올바른 Base64 형식이 아닙니다.";
    private static final String HMAC_LENGTH_MESSAGE = "휴대전화 인증 HMAC 키는 32바이트 이상이어야 합니다.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PhoneVerificationConfig.class)
            .withPropertyValues("auth.phone.key-hmac-secret-base64=" + VALID_HMAC_KEY);

    @Test
    void local에서는_숫자_6자리_고정_인증번호로_noop_sender_하나를_사용한다() {
        // given
        ApplicationContextRunner localContextRunner = localContextRunner()
                .withPropertyValues("auth.sms.local.fixed-otp=" + FIXED_OTP);

        // when
        localContextRunner.run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SmsSender.class);
            assertThat(context).hasSingleBean(OtpGenerator.class);
            assertThat(context.getBean(SmsSender.class)).isInstanceOf(LocalNoopSmsSender.class);

            SmsSender smsSender = context.getBean(SmsSender.class);
            assertThatCode(() -> smsSender.send("01000000000", FIXED_OTP))
                    .doesNotThrowAnyException();
            assertThat(context.getBean(OtpGenerator.class).generate()).isEqualTo(FIXED_OTP);
        });
    }

    @Test
    void local에서_고정_인증번호가_누락되면_기동에_실패한다() {
        // given
        ApplicationContextRunner localContextRunner = localContextRunner();

        // when
        localContextRunner.run(context -> {
            // then
            assertLocalValidationFailure(context.getStartupFailure());
        });
    }

    @Test
    void local에서_고정_인증번호가_blank이면_기동에_실패한다() {
        // given
        ApplicationContextRunner localContextRunner = localContextRunner()
                .withPropertyValues("auth.sms.local.fixed-otp=   ");

        // when
        localContextRunner.run(context -> {
            // then
            assertLocalValidationFailure(context.getStartupFailure());
        });
    }

    @Test
    void local에서_고정_인증번호가_숫자가_아니면_기동에_실패한다() {
        // given
        ApplicationContextRunner localContextRunner = localContextRunner()
                .withPropertyValues("auth.sms.local.fixed-otp=abcdef");

        // when
        localContextRunner.run(context -> {
            // then
            assertLocalValidationFailure(context.getStartupFailure());
        });
    }

    @Test
    void local에서_고정_인증번호가_5자리이면_기동에_실패한다() {
        // given
        ApplicationContextRunner localContextRunner = localContextRunner()
                .withPropertyValues("auth.sms.local.fixed-otp=12345");

        // when
        localContextRunner.run(context -> {
            // then
            assertLocalValidationFailure(context.getStartupFailure());
        });
    }

    @Test
    void local에서_고정_인증번호가_7자리이면_기동에_실패한다() {
        // given
        ApplicationContextRunner localContextRunner = localContextRunner()
                .withPropertyValues("auth.sms.local.fixed-otp=1234567");

        // when
        localContextRunner.run(context -> {
            // then
            assertLocalValidationFailure(context.getStartupFailure());
        });
    }

    @Test
    void non_local에서_고정_인증번호가_누락되면_unavailable_sender_하나를_사용한다() {
        // given
        ApplicationContextRunner nonLocalContextRunner = nonLocalContextRunner();

        // when
        nonLocalContextRunner.run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SmsSender.class);
            assertThat(context).hasSingleBean(OtpGenerator.class);
            assertThat(context.getBean(SmsSender.class)).isInstanceOf(UnavailableSmsSender.class);
            OtpGenerator otpGenerator = context.getBean(OtpGenerator.class);
            for (int count = 0; count < 100; count++) {
                assertThat(otpGenerator.generate()).matches("^[0-9]{6}$");
            }
        });
    }

    @Test
    void non_local에서_고정_인증번호가_blank이면_unavailable_sender_하나를_사용한다() {
        // given
        ApplicationContextRunner nonLocalContextRunner = nonLocalContextRunner()
                .withPropertyValues("auth.sms.local.fixed-otp=   ");

        // when
        nonLocalContextRunner.run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SmsSender.class);
            assertThat(context.getBean(SmsSender.class)).isInstanceOf(UnavailableSmsSender.class);
        });
    }

    @Test
    void non_local에서_고정_인증번호가_설정되면_기동에_실패한다() {
        // given
        ApplicationContextRunner nonLocalContextRunner = nonLocalContextRunner()
                .withPropertyValues("auth.sms.local.fixed-otp=" + FIXED_OTP);

        // when
        nonLocalContextRunner.run(context -> {
            // then
            assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasRootCauseMessage(NON_LOCAL_VALIDATION_MESSAGE);
        });
    }

    @Test
    void unavailable_sender는_명시적인_인증_모듈_오류로_발송에_실패한다() {
        // given
        SmsSender smsSender = new UnavailableSmsSender();

        // when
        Throwable exception = catchThrowable(() -> smsSender.send("01000000000", FIXED_OTP));

        // then
        assertThat(exception)
                .isInstanceOf(AuthException.class)
                .satisfies(throwable -> {
                    AuthErrorCode errorCode = (AuthErrorCode) ((AuthException) throwable).getErrorCode();
                    assertThat(errorCode).isEqualTo(AuthErrorCode.PHONE_DELIVERY_FAILED);
                    assertThat(errorCode.getCode()).isEqualTo("PHONE-007");
                    assertThat(errorCode.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(errorCode.getMessage()).isEqualTo("문자 인증번호를 발송할 수 없습니다.");
                });
    }

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

    private ApplicationContextRunner localContextRunner() {
        return contextRunner.withPropertyValues("spring.profiles.active=local");
    }

    private ApplicationContextRunner nonLocalContextRunner() {
        return contextRunner.withPropertyValues("spring.profiles.active=prod");
    }

    private ApplicationContextRunner nonLocalContextRunnerWithoutHmacKey() {
        return new ApplicationContextRunner()
                .withUserConfiguration(PhoneVerificationConfig.class)
                .withPropertyValues("spring.profiles.active=prod");
    }

    private void assertLocalValidationFailure(Throwable startupFailure) {
        assertThat(startupFailure)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage(LOCAL_VALIDATION_MESSAGE);
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
