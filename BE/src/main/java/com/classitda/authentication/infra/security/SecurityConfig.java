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
                        .requestMatchers(HttpMethod.POST, "/api/auth/google", "/api/auth/tokens/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/local/members/*/tokens").permitAll() // TODO : 로컬 개발 끝나면 삭제 예정
                        .requestMatchers(HttpMethod.GET, "/api/terms").hasAuthority(TokenUse.SIGNUP.authority())
                        .requestMatchers(HttpMethod.POST, "/api/auth/phone-verifications", "/api/auth/phone-verifications/*/confirm", "/api/auth/signup").hasAuthority(TokenUse.SIGNUP.authority())
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").hasAuthority(TokenUse.ACCESS.authority())
                        .requestMatchers("/api/**").permitAll() // TODO: 각 API 작업자는 이 임시 규칙을 제거하고 담당 경로의 인증 규칙을 명시적으로 설정한다.
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
