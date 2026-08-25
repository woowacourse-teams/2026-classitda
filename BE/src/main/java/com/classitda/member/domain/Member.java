package com.classitda.member.domain;

import com.classitda.common.domain.BaseEntity;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
@Entity
public class Member extends BaseEntity {

    public static final String WITHDRAWN_MEMBER_NAME = "탈퇴한 회원";

    private static final String CANONICAL_PHONE_NUMBER_PATTERN = "^010[0-9]{8}$";
    private static final int WITHDRAWAL_RETENTION_DAYS = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String profileImageUrl;

    private LocalDateTime withdrawalRequestedAt;

    private LocalDateTime cleanupScheduledAt;

    private LocalDateTime cleanedUpAt;

    @Builder
    private Member(String name, String phoneNumber, String profileImageUrl) {
        validateName(name);
        validatePhoneNumber(phoneNumber);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }

    public void withdraw(LocalDateTime requestedAt) {
        if (requestedAt == null) {
            throw new MemberException(MemberErrorCode.MEMBER_WITHDRAWAL_REQUESTED_AT_REQUIRED);
        }
        if (withdrawalRequestedAt != null) {
            return;
        }

        withdrawalRequestedAt = requestedAt;
        cleanupScheduledAt = requestedAt.plusDays(WITHDRAWAL_RETENTION_DAYS);
    }

    public void clearPersonalInformation(LocalDateTime occurredAt) {
        if (occurredAt == null) {
            throw new MemberException(MemberErrorCode.MEMBER_CLEANUP_OCCURRED_AT_REQUIRED);
        }
        if (withdrawalRequestedAt == null) {
            throw new MemberException(MemberErrorCode.MEMBER_WITHDRAWAL_REQUIRED);
        }
        if (cleanedUpAt != null) {
            return;
        }
        if (occurredAt.isBefore(cleanupScheduledAt)) {
            throw new MemberException(MemberErrorCode.MEMBER_CLEANUP_NOT_DUE);
        }

        name = WITHDRAWN_MEMBER_NAME;
        phoneNumber = null;
        profileImageUrl = null;
        cleanedUpAt = occurredAt;
    }

    public boolean isWithdrawalPending() {
        return withdrawalRequestedAt != null && cleanedUpAt == null;
    }

    public boolean isCleanedUp() {
        return cleanedUpAt != null;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new MemberException(MemberErrorCode.MEMBER_NAME_REQUIRED);
        }
        if (name.length() > 50) {
            throw new MemberException(MemberErrorCode.MEMBER_NAME_TOO_LONG);
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches(CANONICAL_PHONE_NUMBER_PATTERN)) {
            throw new MemberException(MemberErrorCode.MEMBER_PHONE_NUMBER_INVALID);
        }
    }
}
