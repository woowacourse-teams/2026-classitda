package com.classitda.authentication.presentation;

import com.classitda.authentication.application.SocialLoginService;
import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController implements AuthControllerApi {

    private final SocialLoginService socialLoginService;

    @Override
    @PostMapping(value = "/google", version = "1")
    public LoginResponse loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        return socialLoginService.loginWithGoogle(request);
    }
}
