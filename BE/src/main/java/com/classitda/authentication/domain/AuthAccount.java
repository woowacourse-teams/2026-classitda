package com.classitda.authentication.domain;

import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_account")
@Entity
public class AuthAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OauthProvider provider;

    @Column(nullable = false, length = 255)
    private String providerSubject;

    @Column(length = 254)
    private String providerEmail;

    @Builder
    private AuthAccount(
            Long memberId,
            OauthProvider provider,
            String providerSubject,
            String providerEmail
    ) {
        validateMemberId(memberId);
        validateProvider(provider);
        validateProviderSubject(providerSubject);
        validateProviderEmail(providerEmail);
        this.memberId = memberId;
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.providerEmail = providerEmail;
    }

    public void updateProviderEmail(String providerEmail) {
        validateProviderEmail(providerEmail);
        this.providerEmail = providerEmail;
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null || memberId < 1) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_MEMBER_ID_INVALID);
        }
    }

    private void validateProvider(OauthProvider provider) {
        if (provider == null) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_REQUIRED);
        }
    }

    private void validateProviderSubject(String providerSubject) {
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_SUBJECT_REQUIRED);
        }
        if (providerSubject.length() > 255) {
            throw new IllegalArgumentException("OAuth 제공자 사용자 식별자는 255자 이하여야 합니다.");
        }
    }

    private void validateProviderEmail(String providerEmail) {
        if (providerEmail != null && (providerEmail.isBlank() || providerEmail.length() > 254)) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_EMAIL_INVALID);
        }
    }
}
