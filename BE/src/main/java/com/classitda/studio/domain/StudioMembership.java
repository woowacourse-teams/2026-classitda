package com.classitda.studio.domain;

import com.classitda.common.domain.BaseEntity;
import com.classitda.member.domain.Member;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_role_id", nullable = false)
    private StudioRole studioRole;

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
            MembershipStatus status,
            LocalDateTime joinedAt
    ) {
        this.studio = studio;
        this.member = member;
        this.studioRole = studioRole;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public boolean isInstructor() {
        return studioRole.isInstructor();
    }
}
