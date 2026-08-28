package com.classitda.authentication.support;

import com.classitda.authentication.infra.security.jwt.JwtConfig;
import com.classitda.authentication.infra.security.jwt.SignupSessionJwtValidator;
import com.classitda.authentication.infra.security.properties.JwtProperties;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

public final class JwtTestSupport {

    private final JwtProperties jwtProperties;

    private JwtTestSupport(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public static JwtTestSupport create() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            return new JwtTestSupport(new JwtProperties(
                    Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()),
                    Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("테스트 RSA 키를 생성할 수 없습니다.", exception);
        }
    }

    public JwtEncoder encoder() {
        return new JwtConfig().jwtEncoder(jwtProperties);
    }

    public JwtDecoder decoder(SignupSessionJwtValidator signupSessionJwtValidator) {
        return new JwtConfig().jwtDecoder(jwtProperties, signupSessionJwtValidator);
    }
}
