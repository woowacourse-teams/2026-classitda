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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
@Entity
public class Member extends BaseEntity {

    private static final String CANONICAL_PHONE_NUMBER_PATTERN = "^\\+8210[0-9]{8}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String profileImageUrl;

    @Builder
    private Member(String name, String phoneNumber, String profileImageUrl) {
        validateName(name);
        validatePhoneNumber(phoneNumber);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
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
