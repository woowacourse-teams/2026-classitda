package com.classitda.authentication.application;

import com.classitda.authentication.application.phone.PhoneVerificationStore;
import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.application.session.SignupSessionRegistry;
import com.classitda.authentication.application.token.IssuedLoginTokens;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.signup.SignupResponse;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.member.domain.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * DB 트랜잭션은 {@link SignupAccountCreator}에 한정.
 * 계정 생성의 커밋 또는 롤백이 끝난 뒤에만
 * 로그인 토큰을 발급하고 Redis 가입 상태를 정리하기 위해 이 조정 서비스는 트랜잭션을 열지 않았음.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SignupService {

    private final SignupSessionRegistry signupSessionRegistry;
    private final PhoneVerificationStore phoneVerificationStore;

    private final SignupAccountCreator signupAccountCreator;
    private final AuthAccountRepository authAccountRepository;
    private final MemberRepository memberRepository;

    private final LoginTokenIssuer loginTokenIssuer;

    public SignupResponse complete(String signupJti, SignupRequest request) {
        SignupSession signupSession = findSignupSession(signupJti);

        Optional<Long> existingMemberId = findExistingMemberId(signupSession);
        if (existingMemberId.isPresent()) {
            return issueLoginTokensAndCleanup(existingMemberId.get(), signupJti);
        }

        String verifiedPhoneNumber = findVerifiedPhoneNumber(signupJti);
        Long memberId = createAccount(request, signupSession, verifiedPhoneNumber);
        return issueLoginTokensAndCleanup(memberId, signupJti);
    }

    private SignupSession findSignupSession(String signupJti) {
        Optional<SignupSession> signupSession;
        try {
            signupSession = signupSessionRegistry.findBySignupJti(signupJti);
        } catch (RuntimeException exception) {
            log.error(
                    "가입 세션 조회 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("가입 세션을 확인할 수 없습니다.");
        }

        return signupSession
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTHENTICATION_REQUIRED));
    }

    private Optional<Long> findExistingMemberId(SignupSession signupSession) {
        try {
            return authAccountRepository.findByProviderAndProviderSubject(
                            signupSession.provider(),
                            signupSession.providerSubject()
                    )
                    .map(AuthAccount::getMemberId);
        } catch (RuntimeException exception) {
            log.error(
                    "가입 소셜 계정 조회 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("가입 소셜 계정을 확인할 수 없습니다.");
        }
    }

    private String findVerifiedPhoneNumber(String signupJti) {
        Optional<String> verifiedPhoneNumber;
        try {
            verifiedPhoneNumber = phoneVerificationStore.findVerifiedPhoneNumber(signupJti);
        } catch (RuntimeException exception) {
            log.error(
                    "인증 완료 휴대전화 번호 조회 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("인증 완료 휴대전화 번호를 확인할 수 없습니다.");
        }
        return verifiedPhoneNumber
                .orElseThrow(() -> new AuthException(AuthErrorCode.VERIFIED_PHONE_UNAVAILABLE));
    }

    private Long createAccount(SignupRequest request, SignupSession signupSession, String verifiedPhoneNumber) {
        try {
            return signupAccountCreator.create(request, signupSession, verifiedPhoneNumber);
        } catch (DataIntegrityViolationException exception) {
            return reconcileAfterIntegrityViolation(exception, signupSession, verifiedPhoneNumber);
        } catch (ClassitdaException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error(
                    "회원가입 데이터 저장 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("회원가입 데이터를 저장할 수 없습니다.");
        }
    }

    private Long reconcileAfterIntegrityViolation(
            DataIntegrityViolationException exception,
            SignupSession signupSession,
            String verifiedPhoneNumber
    ) {
        Optional<Long> existingMemberId = findExistingMemberId(signupSession);
        if (existingMemberId.isPresent()) {
            return existingMemberId.get();
        }

        if (isPhoneNumberRegistered(verifiedPhoneNumber)) {
            throw new AuthException(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        }

        log.error(
                "회원가입 데이터 무결성 처리 중 내부 오류가 발생했습니다. exceptionType={}",
                exception.getClass().getName()
        );
        throw new IllegalStateException("회원가입 데이터 무결성 제약을 확인할 수 없습니다.");
    }

    private boolean isPhoneNumberRegistered(String verifiedPhoneNumber) {
        try {
            return memberRepository.existsByPhoneNumber(verifiedPhoneNumber);
        } catch (RuntimeException exception) {
            log.error(
                    "가입 휴대전화 번호 조회 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("가입 휴대전화 번호를 확인할 수 없습니다.");
        }
    }

    private SignupResponse issueLoginTokensAndCleanup(Long memberId, String signupJti) {
        try {
            IssuedLoginTokens issuedLoginTokens = loginTokenIssuer.issueLoginTokens(memberId);
            return SignupResponse.from(issuedLoginTokens);
        } catch (RuntimeException exception) {
            log.error(
                    "회원가입 후 로그인 토큰 발급 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("회원가입 후 로그인 토큰을 발급할 수 없습니다.");
        } finally {
            cleanupSignupState(signupJti);
        }
    }

    private void cleanupSignupState(String signupJti) {
        try {
            signupSessionRegistry.deleteBySignupJti(signupJti);
        } catch (RuntimeException exception) {
            log.error(
                    "가입 완료 후 가입 세션 정리에 실패했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
        }

        try {
            phoneVerificationStore.deleteVerifiedPhoneNumber(signupJti);
        } catch (RuntimeException exception) {
            log.error(
                    "가입 완료 후 인증 휴대전화 상태 정리에 실패했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
        }
    }
}
