package com.classitda.authentication.application;

import com.classitda.authentication.application.identity.GoogleIdentity;
import com.classitda.authentication.application.identity.GoogleIdentityVerifier;
import com.classitda.authentication.application.token.IssuedLoginTokens;
import com.classitda.authentication.application.token.IssuedSignupToken;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.SignupTokenIssuer;
import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SocialLoginService {

    private final GoogleIdentityVerifier googleIdentityVerifier;
    private final AuthAccountRepository authAccountRepository;
    private final SignupTokenIssuer signupTokenIssuer;
    private final LoginTokenIssuer loginTokenIssuer;

    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdentity identity = googleIdentityVerifier.verify(request.idToken());

        Optional<AuthAccount> authAccount = authAccountRepository.findByProviderAndProviderSubject(
                OauthProvider.GOOGLE,
                identity.providerSubject());

        if (authAccount.isEmpty()) {
            IssuedSignupToken issuedSignupToken = signupTokenIssuer.issueSignupToken(
                    OauthProvider.GOOGLE,
                    identity.providerSubject(),
                    identity.providerEmail());
            return LoginResponse.registrationRequired(issuedSignupToken);
        }

        AuthAccount registeredAccount = authAccount.get();
        if (!Objects.equals(registeredAccount.getProviderEmail(), identity.providerEmail())) {
            registeredAccount.updateProviderEmail(identity.providerEmail());
            // 로그인 전체 흐름에 Google 검증(외부 API) 및 Redis 처리가 포함되어있기 때문에 DB 트랜잭션을 열지 않는다.
            // 따라서 더티 체킹 대신 변경 내용을 명시적으로 저장한다.
            authAccountRepository.save(registeredAccount);
        }

        IssuedLoginTokens issuedLoginTokens = loginTokenIssuer.issueLoginTokens(registeredAccount.getMemberId());
        return LoginResponse.registered(issuedLoginTokens);
    }
}
