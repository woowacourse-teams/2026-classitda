package com.classitda.authentication.presentation;

import static org.springframework.http.HttpStatus.CREATED;

import com.classitda.authentication.application.LogoutService;
import com.classitda.authentication.application.RefreshTokenService;
import com.classitda.authentication.application.SignupService;
import com.classitda.authentication.application.SocialLoginService;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.authentication.presentation.dto.login.LoginResponse;
import com.classitda.authentication.presentation.dto.login.SocialLoginRequest;
import com.classitda.authentication.presentation.dto.logout.LogoutRequest;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationConfirmRequest;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationResponse;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationSendRequest;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.signup.SignupResponse;
import com.classitda.authentication.presentation.dto.token.RefreshTokenRequest;
import com.classitda.authentication.presentation.dto.token.LoginTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController implements AuthControllerApi {

    private final SocialLoginService socialLoginService;
    private final PhoneVerificationService phoneVerificationService;
    private final SignupService signupService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;

    @Override
    @PostMapping(value = "/{provider:google|apple}", version = "1")
    public LoginResponse loginWithSocial(
            @PathVariable String provider,
            @Valid @RequestBody SocialLoginRequest request
    ) {
        return socialLoginService.loginWithSocial(resolveProvider(provider), request.idToken());
    }

    private OauthProvider resolveProvider(String provider) {
        return switch (provider) {
            case "google" -> OauthProvider.GOOGLE;
            case "apple" -> OauthProvider.APPLE;
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        };
    }

    @Override
    @PostMapping(value = "/tokens/refresh", version = "1")
    public LoginTokenResponse refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return refreshTokenService.refresh(request);
    }

    @Override
    @PostMapping(value = "/logout", version = "1")
    public ResponseEntity<Void> logout(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody LogoutRequest request
    ) {
        logoutService.logout(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping(value = "/phone-verifications", version = "1")
    public ResponseEntity<PhoneVerificationResponse> sendPhoneVerification(
            @AuthenticationPrincipal Jwt signupJwt,
            @RequestBody PhoneVerificationSendRequest request
    ) {
        PhoneVerificationResponse response = phoneVerificationService.send(signupJwt.getId(), request.phoneNumber());
        return ResponseEntity.status(CREATED).body(response);
    }

    @Override
    @PostMapping(value = "/phone-verifications/{verificationId}/confirm", version = "1")
    public ResponseEntity<Void> confirmPhoneVerification(
            @AuthenticationPrincipal Jwt signupJwt,
            @PathVariable String verificationId,
            @RequestBody PhoneVerificationConfirmRequest request
    ) {
        phoneVerificationService.confirm(signupJwt.getId(), verificationId, request.otp());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping(value = "/signup", version = "1")
    public ResponseEntity<SignupResponse> completeSignup(
            @AuthenticationPrincipal Jwt signupJwt,
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = signupService.complete(signupJwt.getId(), request);
        return ResponseEntity.status(CREATED).body(response);
    }
}
