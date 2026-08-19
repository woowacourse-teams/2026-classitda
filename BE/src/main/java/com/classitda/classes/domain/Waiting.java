package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.common.domain.BaseEntity;
import com.classitda.studio.domain.StudioMembership;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "waiting")
@Entity
public class Waiting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private StudioMembership membership;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WaitingStatus status;

    private LocalDateTime offeredAt;

    private LocalDateTime offerExpiresAt;

    void offer(LocalDateTime occurredAt, LocalDateTime expiresAt) {
        if (occurredAt == null) {
            throw new ClassException(ClassErrorCode.WAITING_OFFERED_AT_REQUIRED);
        }
        if (expiresAt == null) {
            throw new ClassException(ClassErrorCode.WAITING_OFFER_EXPIRES_AT_REQUIRED);
        }

        requireWaiting();
        if (!expiresAt.isAfter(occurredAt)) {
            throw new ClassException(ClassErrorCode.INVALID_WAITING_OFFER_DEADLINE);
        }

        status = WaitingStatus.OFFERED;
        offeredAt = occurredAt;
        offerExpiresAt = expiresAt;
    }

    private void requireWaiting() {
        if (status != WaitingStatus.WAITING) {
            throw new ClassException(ClassErrorCode.INVALID_WAITING_TRANSITION);
        }
    }
}
