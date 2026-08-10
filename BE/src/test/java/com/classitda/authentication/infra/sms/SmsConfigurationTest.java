package com.classitda.authentication.infra.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.classitda.authentication.application.sms.SmsSender;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;

class SmsConfigurationTest {

    private static final String FIXED_OTP = "135790";
    private static final String LOCAL_VALIDATION_MESSAGE = "local SMS 고정 인증번호는 숫자 6자리여야 합니다.";
    private static final String NON_LOCAL_VALIDATION_MESSAGE =
            "non-local profile에서는 local SMS 고정 인증번호를 사용할 수 없습니다.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SmsConfiguration.class);

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
            assertThat(context.getBean(SmsSender.class)).isInstanceOf(LocalNoopSmsSender.class);

            SmsSender smsSender = context.getBean(SmsSender.class);
            assertThatCode(() -> smsSender.send("01000000000", FIXED_OTP))
                    .doesNotThrowAnyException();
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
            assertThat(context.getBean(SmsSender.class)).isInstanceOf(UnavailableSmsSender.class);
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

    private ApplicationContextRunner localContextRunner() {
        return contextRunner.withPropertyValues("spring.profiles.active=local");
    }

    private ApplicationContextRunner nonLocalContextRunner() {
        return contextRunner.withPropertyValues("spring.profiles.active=prod");
    }

    private void assertLocalValidationFailure(Throwable startupFailure) {
        assertThat(startupFailure)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage(LOCAL_VALIDATION_MESSAGE);
    }
}
