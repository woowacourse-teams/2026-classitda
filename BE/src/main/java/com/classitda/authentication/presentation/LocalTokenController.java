package com.classitda.authentication.presentation;

import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.presentation.dto.token.LoginTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: 실제 운영 전환 전에 이 개발용 Controller와 SecurityConfig의 permitAll 규칙을 함께 제거한다.
@RequiredArgsConstructor
@RequestMapping("/api/auth/local")
@RestController
public class LocalTokenController implements LocalTokenControllerApi {

    private final LoginTokenIssuer loginTokenIssuer;

    @Override
    @PostMapping(value = "/members/{memberId}/tokens", version = "1")
    public LoginTokenResponse issueTokens(
            @PathVariable Long memberId
    ) {
        IssuedLoginTokens issuedTokens = loginTokenIssuer.issueLoginTokens(memberId);
        return new LoginTokenResponse(
                issuedTokens.accessToken(),
                issuedTokens.accessTokenExpiresIn(),
                issuedTokens.refreshToken(),
                issuedTokens.refreshTokenExpiresIn()
        );
    }
}
