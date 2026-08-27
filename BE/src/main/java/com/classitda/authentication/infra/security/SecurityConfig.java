package com.classitda.authentication.infra.security;

import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.infra.security.jwt.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationErrorHandler authenticationErrorHandler,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationErrorHandler)
                        .accessDeniedHandler(authenticationErrorHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/google", "/api/auth/apple", "/api/auth/tokens/refresh").permitAll()
                        // TODO: 실제 운영 전환 전에 개발용 토큰 발급 Controller와 이 permitAll 규칙을 함께 제거한다.
                        .requestMatchers(HttpMethod.POST, "/api/auth/local/members/*/tokens").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/terms").hasAuthority(TokenUse.SIGNUP.authority())
                        .requestMatchers(HttpMethod.POST, "/api/auth/phone-verifications", "/api/auth/phone-verifications/*/confirm", "/api/auth/signup").hasAuthority(TokenUse.SIGNUP.authority())
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").hasAuthority(TokenUse.ACCESS.authority())
                        .requestMatchers(HttpMethod.GET, "/api/members/me").hasAuthority(TokenUse.ACCESS.authority())
                        .requestMatchers(HttpMethod.PATCH, "/api/members/me/name").hasAuthority(TokenUse.ACCESS.authority())
                        .requestMatchers(HttpMethod.DELETE, "/api/members/me").hasAuthority(TokenUse.ACCESS.authority())
                        .requestMatchers("/api/studios/**").hasAuthority(TokenUse.ACCESS.authority())
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(authenticationErrorHandler)
                        .accessDeniedHandler(authenticationErrorHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }
}
