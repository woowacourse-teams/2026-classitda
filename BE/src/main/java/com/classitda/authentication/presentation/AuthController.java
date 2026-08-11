package com.classitda.authentication.presentation;

import static org.springframework.http.HttpStatus.CREATED;

import com.classitda.authentication.application.SocialLoginService;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import com.classitda.authentication.presentation.dto.PhoneVerificationConfirmRequest;
import com.classitda.authentication.presentation.dto.PhoneVerificationResponse;
import com.classitda.authentication.presentation.dto.PhoneVerificationSendRequest;
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

    @Override
    @PostMapping(value = "/google", version = "1")
    public LoginResponse loginWithGoogle(
            @RequestBody GoogleLoginRequest request
    ) {
        return socialLoginService.loginWithGoogle(request);
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
}
