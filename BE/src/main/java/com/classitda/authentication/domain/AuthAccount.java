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

    @Builder
    private AuthAccount(Long memberId, OauthProvider provider, String providerSubject) {
        validateMemberId(memberId);
        validateProvider(provider);
        validateProviderSubject(providerSubject);
        this.memberId = memberId;
        this.provider = provider;
        this.providerSubject = providerSubject;
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
    }
}
