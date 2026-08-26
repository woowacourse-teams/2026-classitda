package com.classitda.studio.domain;

import com.classitda.common.domain.BaseEntity;
import com.classitda.member.domain.Member;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "studio_membership")
@Entity
public class StudioMembership extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 50;
    private static final String CANONICAL_PHONE_NUMBER_PATTERN = "^010[0-9]{8}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_role_id", nullable = false)
    private StudioRole studioRole;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Builder
    private StudioMembership(
            Studio studio,
            Member member,
            StudioRole studioRole,
            String name,
            String phoneNumber,
            MembershipStatus status,
            LocalDateTime joinedAt
    ) {
        validateName(name);
        validatePhoneNumber(phoneNumber);
        this.studio = studio;
        this.member = member;
        this.studioRole = studioRole;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public boolean isInstructor() {
        return studioRole.isInstructor();
    }

    public boolean belongsTo(Member member) {
        if (this.member == null) {
            return false;
        }
        if (this.member == member) {
            return true;
        }
        return member != null
                && this.member.getId() != null
                && this.member.getId().equals(member.getId());
    }

    public boolean isRegistered() {
        return member != null;
    }

    public boolean isWithdrawn() {
        return status == MembershipStatus.WITHDRAWN;
    }

    public void linkMember(Member member) {
        if (member == null) {
            throw new StudioException(StudioErrorCode.MEMBER_NOT_FOUND);
        }
        this.member = member;
    }

    public void updateProfile(String name, String phoneNumber) {
        validateName(name);
        validatePhoneNumber(phoneNumber);
        if (isRegistered() && !phoneNumber.equals(this.phoneNumber)) {
            throw new StudioException(StudioErrorCode.MEMBERSHIP_PHONE_NUMBER_NOT_EDITABLE);
        }
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void withdraw() {
        status = MembershipStatus.WITHDRAWN;
    }

    public void revive(String name, String phoneNumber) {
        validateName(name);
        validatePhoneNumber(phoneNumber);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = MembershipStatus.ACTIVE;
    }

    public void clearPersonalInformation() {
        name = Member.WITHDRAWN_MEMBER_NAME;
        phoneNumber = null;
        status = MembershipStatus.WITHDRAWN;
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches(CANONICAL_PHONE_NUMBER_PATTERN)) {
            throw new StudioException(StudioErrorCode.INVALID_MEMBERSHIP_PHONE_NUMBER);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new StudioException(StudioErrorCode.INVALID_MEMBERSHIP_NAME);
        }
    }

    public boolean isStudent() {
        return studioRole.getSystemRole() == SystemRole.STUDENT;
    }
}
