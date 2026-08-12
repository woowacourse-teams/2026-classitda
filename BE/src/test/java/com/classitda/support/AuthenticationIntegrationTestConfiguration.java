package com.classitda.support;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Import(MySqlTestContainerConfiguration.class)
@TestConfiguration(proxyBeanMethods = false)
public class AuthenticationIntegrationTestConfiguration {

    public static final String FIXED_OTP = "864209";

    private static final String KEY_MATERIAL = encodedBytes(32);
    private static final KeyPair JWT_KEY_PAIR = createKeyPair();

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
                .withExposedPorts(6379);
    }

    @Bean
    DynamicPropertyRegistrar authenticationProperties() {
        return registry -> {
            registry.add("auth.sms.local.fixed-otp", () -> FIXED_OTP);
            registry.add("auth.phone.key-hmac-secret-base64", () -> KEY_MATERIAL);
            registry.add("auth.google.web-client-id", () -> "test-client");
            registry.add("auth.jwt.private-key-base64", () -> Base64.getEncoder()
                    .encodeToString(JWT_KEY_PAIR.getPrivate().getEncoded()));
            registry.add("auth.jwt.public-key-base64", () -> Base64.getEncoder()
                    .encodeToString(JWT_KEY_PAIR.getPublic().getEncoded()));
            registry.add(
                    "spring.sql.init.data-locations",
                    () -> "optional:classpath:/test-data.sql"
            );
        };
    }

    private static String encodedBytes(int length) {
        byte[] bytes = new byte[length];
        for (int index = 0; index < length; index++) {
            bytes[index] = (byte) (index + 11);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static KeyPair createKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("테스트 JWT 키를 생성할 수 없습니다.", exception);
        }
    }
}
