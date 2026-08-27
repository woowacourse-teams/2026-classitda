package com.classitda.authentication.application;

import com.classitda.authentication.application.identity.SocialIdentityVerifier;
import com.classitda.authentication.application.identity.SocialIdentity;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.SignupTokenIssuer;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.application.token.result.IssuedSignupToken;
import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.presentation.dto.login.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.login.LoginResponse;
import com.classitda.member.domain.repository.MemberRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SocialLoginService {

    private final List<SocialIdentityVerifier> socialIdentityVerifiers;
    private final AuthAccountRepository authAccountRepository;
    private final SignupTokenIssuer signupTokenIssuer;
    private final LoginTokenIssuer loginTokenIssuer;
    private final MemberRepository memberRepository;

    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        SocialIdentity identity = verifyIdentity(OauthProvider.GOOGLE, request.idToken());
        return login(identity);
    }

    private SocialIdentity verifyIdentity(OauthProvider provider, String idToken) {
        return socialIdentityVerifiers.stream()
                .filter(verifier -> verifier.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OAuth 제공자 검증기를 찾을 수 없습니다: " + provider))
                .verify(idToken);
    }

    private LoginResponse login(SocialIdentity identity) {
        Optional<AuthAccount> authAccount = authAccountRepository.findByProviderAndProviderSubject(
                identity.provider(),
                identity.providerSubject());

        if (authAccount.isEmpty()) {
            IssuedSignupToken issuedSignupToken = signupTokenIssuer.issueSignupToken(
                    identity.provider(),
                    identity.providerSubject(),
                    identity.providerEmail());
            return LoginResponse.registrationRequired(issuedSignupToken);
        }

        AuthAccount registeredAccount = authAccount.get();
        if (!memberRepository.existsByIdAndWithdrawalRequestedAtIsNull(registeredAccount.getMemberId())) {
            throw new AuthException(AuthErrorCode.MEMBER_WITHDRAWAL_PENDING);
        }

        if (identity.providerEmail() != null
                && !Objects.equals(registeredAccount.getProviderEmail(), identity.providerEmail())) {
            registeredAccount.updateProviderEmail(identity.providerEmail());
            // 로그인 전체 흐름에 제공자 검증(외부 API) 및 Redis 처리가 포함되어있기 때문에 DB 트랜잭션을 열지 않는다.
            // 따라서 더티 체킹 대신 변경 내용을 명시적으로 저장한다.
            authAccountRepository.save(registeredAccount);
        }

        IssuedLoginTokens issuedLoginTokens = loginTokenIssuer.issueLoginTokens(registeredAccount.getMemberId());
        return LoginResponse.registered(issuedLoginTokens);
    }
}
