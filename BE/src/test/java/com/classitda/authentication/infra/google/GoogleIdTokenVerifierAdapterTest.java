package com.classitda.authentication.infra.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.application.identity.SocialIdentityVerificationResult;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class GoogleIdTokenVerifierAdapterTest {

    private static final String WEB_CLIENT_ID = "test-web-client-id";
    private static final String IOS_CLIENT_ID = "test-ios-client-id";
    private static final String NONCE_CLAIM = "a".repeat(64);

    private final GoogleIdTokenVerifierAdapter verifier =
            new GoogleIdTokenVerifierAdapter(WEB_CLIENT_ID, IOS_CLIENT_ID);

    @Test
    void Google_제공자를_지원한다() {
        // given / when / then
        assertThat(verifier.provider()).isEqualTo(OauthProvider.GOOGLE);
    }

    @Test
    void Web과_iOS_Client_ID를_Google_ID_토큰_audience로_허용한다() {
        // given / when / then
        assertThat(verifier)
                .extracting("verifier")
                .isInstanceOfSatisfying(GoogleIdTokenVerifier.class, googleVerifier ->
                        assertThat(googleVerifier.getAudience())
                                .containsExactly(WEB_CLIENT_ID, IOS_CLIENT_ID));
    }

    @Test
    void 빈_iOS_Client_ID는_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> new GoogleIdTokenVerifierAdapter(WEB_CLIENT_ID, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Google OAuth iOS Client ID는 필수입니다.");
    }

    @Test
    void 등록되지_않은_audience의_Google_ID_토큰은_AUTH_006으로_거부한다() {
        // given
        String idToken = idTokenWithAudience("unregistered-client-id");

        // when / then
        assertThatThrownBy(() -> verifier.verify(idToken))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
    }

    @Test
    void 빈_Google_ID_토큰은_AUTH_006으로_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> verifier.verify(" "))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
    }

    @Test
    void 형식이_잘못된_Google_ID_토큰은_AUTH_006으로_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> verifier.verify("malformed-token"))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
    }

    @Test
    void Google_ID_토큰_payload에서_사용자_정보와_nonce_Claim을_반환한다() {
        // given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
                .setSubject("provider-subject")
                .setEmail("member@example.com")
                .setNonce(NONCE_CLAIM);

        // when
        SocialIdentityVerificationResult result = verifier.toVerificationResult(payload);

        // then
        assertThat(result.identity().provider()).isEqualTo(OauthProvider.GOOGLE);
        assertThat(result.identity().providerSubject()).isEqualTo("provider-subject");
        assertThat(result.identity().providerEmail()).isEqualTo("member@example.com");
        assertThat(result.nonceClaim()).isEqualTo(NONCE_CLAIM);
    }

    @Test
    void nonce_Claim이_없는_Google_ID_토큰은_AUTH_006으로_거부한다() {
        // given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
                .setSubject("provider-subject")
                .setEmail("member@example.com");

        // when / then
        assertInvalidIdentity(payload);
    }

    @Test
    void provider_subject가_255자를_초과하면_AUTH_006과_401로_거부한다() {
        // given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
                .setSubject("a".repeat(256))
                .setEmail("member@example.com")
                .setNonce(NONCE_CLAIM);

        // when / then
        assertInvalidIdentity(payload);
    }

    @Test
    void 이메일이_없으면_AUTH_006과_401로_거부한다() {
        // given
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
                .setSubject("provider-subject")
                .setNonce(NONCE_CLAIM);

        // when / then
        assertInvalidIdentity(payload);
    }

    private String idTokenWithAudience(String audience) {
        long issuedAt = Instant.now().getEpochSecond();
        String header = """
                {"alg":"RS256","kid":"test-key"}
                """;
        String payload = """
                {"iss":"accounts.google.com","aud":"%s","sub":"test-subject",\
                "email":"test@example.com","email_verified":true,"nonce":"%s","iat":%d,"exp":%d}
                """.formatted(audience, NONCE_CLAIM, issuedAt, issuedAt + 3_600L);

        return encode(header) + "." + encode(payload) + "." + encode("invalid-signature");
    }

    private String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private void assertInvalidIdentity(GoogleIdToken.Payload payload) {
        assertThatThrownBy(() -> verifier.toVerificationResult(payload))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthErrorCode errorCode = (AuthErrorCode) ((AuthException) exception).getErrorCode();
                    assertThat(errorCode).isEqualTo(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
                    assertThat(errorCode.getStatus().value()).isEqualTo(401);
                });
    }
}
