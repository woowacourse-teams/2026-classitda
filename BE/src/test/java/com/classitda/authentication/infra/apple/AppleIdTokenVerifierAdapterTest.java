package com.classitda.authentication.infra.apple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.classitda.authentication.application.identity.SocialIdentity;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;

class AppleIdTokenVerifierAdapterTest {

    private static final String IOS_CLIENT_ID = "com.classitda.ios";
    private static final String SUBJECT = "apple-provider-subject";
    private static final String EMAIL = "member@example.com";
    private static final String FIRST_KEY_ID = "first-key";
    private static final String SECOND_KEY_ID = "second-key";

    private static final KeyPair FIRST_KEY_PAIR = generateKeyPair();
    private static final KeyPair SECOND_KEY_PAIR = generateKeyPair();

    private MockRestServiceServer server;
    private AppleIdTokenVerifierAdapter verifier;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                .additionalCustomizers(restTemplate ->
                        server = MockRestServiceServer.bindTo(restTemplate).build());
        verifier = new AppleIdTokenVerifierAdapter(IOS_CLIENT_ID, restTemplateBuilder);
    }

    @Test
    void 유효한_Apple_ID_토큰에서_사용자_식별자와_이메일을_반환한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        String idToken = sign(validClaims().build(), FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when
        SocialIdentity identity = verifier.verify(idToken);

        // then
        assertThat(verifier.provider()).isEqualTo(OauthProvider.APPLE);
        assertThat(identity.provider()).isEqualTo(OauthProvider.APPLE);
        assertThat(identity.providerSubject()).isEqualTo(SUBJECT);
        assertThat(identity.providerEmail()).isEqualTo(EMAIL);
        server.verify();
    }

    @Test
    void 이메일이_없는_Apple_ID_토큰도_검증한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        JWTClaimsSet claims = requiredClaims()
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .build();
        String idToken = sign(claims, FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when
        SocialIdentity identity = verifier.verify(idToken);

        // then
        assertThat(identity.providerEmail()).isNull();
        server.verify();
    }

    @Test
    void 빈_Apple_ID_토큰은_AUTH_010으로_거부한다() {
        // given / when / then
        assertInvalidToken(" ");
    }

    @Test
    void 서명이_잘못된_Apple_ID_토큰은_AUTH_010으로_거부한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        String idToken = sign(validClaims().build(), SECOND_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertInvalidToken(idToken);
    }

    @Test
    void RS256이_아닌_Apple_ID_토큰은_AUTH_010으로_거부한다() {
        // given
        String idToken = sign(
                validClaims().build(),
                FIRST_KEY_PAIR,
                FIRST_KEY_ID,
                JWSAlgorithm.RS512);

        // when / then
        assertInvalidToken(idToken);
    }

    @Test
    void issuer가_Apple이_아닌_ID_토큰은_AUTH_010으로_거부한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        JWTClaimsSet claims = validClaims()
                .issuer("https://invalid.example.com")
                .build();
        String idToken = sign(claims, FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertInvalidToken(idToken);
    }

    @Test
    void 등록되지_않은_audience의_Apple_ID_토큰은_AUTH_010으로_거부한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        JWTClaimsSet claims = validClaims()
                .audience("unregistered-client-id")
                .build();
        String idToken = sign(claims, FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertInvalidToken(idToken);
    }

    @Test
    void 만료된_Apple_ID_토큰은_AUTH_010으로_거부한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        JWTClaimsSet claims = validClaims()
                .expirationTime(Date.from(Instant.now().minusSeconds(120)))
                .build();
        String idToken = sign(claims, FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertInvalidToken(idToken);
    }

    @Test
    void exp가_없는_Apple_ID_토큰은_AUTH_010으로_거부한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        JWTClaimsSet claims = requiredClaims()
                .claim("email", EMAIL)
                .build();
        String idToken = sign(claims, FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertInvalidToken(idToken);
    }

    @Test
    void sub가_없는_Apple_ID_토큰은_AUTH_010으로_거부한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(AppleIdTokenVerifierAdapter.ISSUER)
                .audience(IOS_CLIENT_ID)
                .issueTime(Date.from(Instant.now().minusSeconds(30)))
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("email", EMAIL)
                .build();
        String idToken = sign(claims, FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertInvalidToken(idToken);
    }

    @Test
    void 새로운_kid의_토큰을_받으면_Apple_JWKS를_다시_조회한다() {
        // given
        expectJwks(publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID));
        expectJwks(
                publicJwk(FIRST_KEY_PAIR, FIRST_KEY_ID),
                publicJwk(SECOND_KEY_PAIR, SECOND_KEY_ID));
        String firstToken = sign(validClaims().subject("first-subject").build(), FIRST_KEY_PAIR, FIRST_KEY_ID);
        String secondToken = sign(validClaims().subject("second-subject").build(), SECOND_KEY_PAIR, SECOND_KEY_ID);

        // when
        SocialIdentity firstIdentity = verifier.verify(firstToken);
        SocialIdentity secondIdentity = verifier.verify(secondToken);

        // then
        assertThat(firstIdentity.providerSubject()).isEqualTo("first-subject");
        assertThat(secondIdentity.providerSubject()).isEqualTo("second-subject");
        server.verify();
    }

    @Test
    void Apple_JWKS를_조회할_수_없으면_내부_오류로_처리한다() {
        // given
        server.expect(requestTo(AppleIdTokenVerifierAdapter.JWKS_URI))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        String idToken = sign(validClaims().build(), FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertThatThrownBy(() -> verifier.verify(idToken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apple ID 토큰 검증 중 내부 오류가 발생했습니다.");
        server.verify();
    }

    @Test
    void Apple_JWKS_조회가_timeout되면_내부_오류로_처리한다() {
        // given
        server.expect(requestTo(AppleIdTokenVerifierAdapter.JWKS_URI))
                .andExpect(method(GET))
                .andRespond(request -> {
                    throw new ResourceAccessException("Apple JWKS request timed out");
                });
        String idToken = sign(validClaims().build(), FIRST_KEY_PAIR, FIRST_KEY_ID);

        // when / then
        assertThatThrownBy(() -> verifier.verify(idToken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apple ID 토큰 검증 중 내부 오류가 발생했습니다.");
        server.verify();
    }

    @Test
    void 빈_iOS_Client_ID는_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> new AppleIdTokenVerifierAdapter(" ", new RestTemplateBuilder()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Apple OAuth iOS Client ID는 필수입니다.");
    }

    private JWTClaimsSet.Builder requiredClaims() {
        return new JWTClaimsSet.Builder()
                .issuer(AppleIdTokenVerifierAdapter.ISSUER)
                .audience(IOS_CLIENT_ID)
                .subject(SUBJECT)
                .issueTime(Date.from(Instant.now().minusSeconds(30)));
    }

    private JWTClaimsSet.Builder validClaims() {
        return requiredClaims()
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("email", EMAIL);
    }

    private void expectJwks(RSAKey... keys) {
        String response = new JWKSet(List.of(keys)).toString();
        server.expect(requestTo(AppleIdTokenVerifierAdapter.JWKS_URI))
                .andExpect(method(GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }

    private RSAKey publicJwk(KeyPair keyPair, String keyId) {
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID(keyId)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    private String sign(JWTClaimsSet claims, KeyPair keyPair, String keyId) {
        return sign(claims, keyPair, keyId, JWSAlgorithm.RS256);
    }

    private String sign(
            JWTClaimsSet claims,
            KeyPair keyPair,
            String keyId,
            JWSAlgorithm algorithm
    ) {
        try {
            SignedJWT signedJwt = new SignedJWT(
                    new JWSHeader.Builder(algorithm)
                            .keyID(keyId)
                            .build(),
                    claims);
            signedJwt.sign(new RSASSASigner((RSAPrivateKey) keyPair.getPrivate()));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Apple ID 토큰 테스트 서명에 실패했습니다.", exception);
        }
    }

    private void assertInvalidToken(String idToken) {
        assertThatThrownBy(() -> verifier.verify(idToken))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.APPLE_ID_TOKEN_INVALID);
        server.verify();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Apple ID 토큰 테스트 RSA 키 생성에 실패했습니다.", exception);
        }
    }
}
