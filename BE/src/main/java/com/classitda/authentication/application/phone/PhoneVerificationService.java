package com.classitda.authentication.application.phone;

import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationResponse;
import com.classitda.member.domain.repository.MemberRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class PhoneVerificationService {

    private static final long VERIFICATION_TTL_SECONDS = 180L;
    private static final long COOLDOWN_TTL_SECONDS = 60L;
    private static final int MAX_CONFIRM_ATTEMPTS = 5;
    private static final long VERIFIED_PHONE_TTL_SECONDS = 1_800L;

    private final MemberRepository memberRepository;
    private final PhoneVerificationStore phoneVerificationStore;

    private final PhoneVerificationHasher phoneVerificationHasher;
    private final OtpGenerator otpGenerator;

    private final SmsSender smsSender;

    // 회원 조회와 인증 상태 저장, 외부 SMS 발송을 함께 수행하므로 DB 트랜잭션을 열지 않는다.
    public PhoneVerificationResponse send(String signupJti, String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new AuthException(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        }

        String verificationId = UUID.randomUUID().toString();
        String otp = otpGenerator.generate();
        String otpDigest = phoneVerificationHasher.hashOtp(
                signupJti,
                verificationId,
                phoneNumber,
                otp
        );
        PhoneVerificationState state = new PhoneVerificationState(
                verificationId,
                signupJti,
                phoneNumber,
                otpDigest
        );

        boolean saved = phoneVerificationStore.saveIfCooldownExpired(state, VERIFICATION_TTL_SECONDS, COOLDOWN_TTL_SECONDS);
        if (!saved) {
            throw new AuthException(AuthErrorCode.PHONE_RESEND_COOLDOWN);
        }

        try {
            smsSender.send(phoneNumber, otp);
        } catch (RuntimeException senderException) {
            cleanupFailedDelivery(state, senderException);
            throw senderException;
        }

        return PhoneVerificationResponse.of(verificationId, VERIFICATION_TTL_SECONDS, COOLDOWN_TTL_SECONDS);
    }

    public void confirm(String signupJti, String verificationId, String otp) {
        PhoneVerificationState state = phoneVerificationStore.findByVerificationId(verificationId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE));

        if (!state.signupJti().equals(signupJti)) {
            throw new AuthException(AuthErrorCode.PHONE_VERIFICATION_SESSION_MISMATCH);
        }

        boolean otpMatches = phoneVerificationHasher.matchesOtp(state, otp);
        PhoneVerificationStore.ConfirmOutcome outcome = phoneVerificationStore.confirm(
                state,
                otpMatches,
                MAX_CONFIRM_ATTEMPTS,
                VERIFIED_PHONE_TTL_SECONDS
        );

        switch (outcome) {
            case CONFIRMED -> {}
            case UNAVAILABLE -> throw new AuthException(AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE);
            case SESSION_MISMATCH -> throw new AuthException(AuthErrorCode.PHONE_VERIFICATION_SESSION_MISMATCH);
            case OTP_INVALID -> throw new AuthException(AuthErrorCode.PHONE_OTP_INVALID);
            case ATTEMPTS_EXCEEDED -> throw new AuthException(AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED);
        }
    }

    private void cleanupFailedDelivery(PhoneVerificationState state, RuntimeException senderException) {
        try {
            phoneVerificationStore.deleteIfActive(state);
        } catch (RuntimeException cleanupException) {
            senderException.addSuppressed(cleanupException);
            log.error(
                    "SMS 발송 실패 후 인증 상태 정리에 실패했습니다. senderExceptionType={}, cleanupExceptionType={}",
                    senderException.getClass().getName(),
                    cleanupException.getClass().getName(),
                    cleanupException
            );
        }
    }
}
