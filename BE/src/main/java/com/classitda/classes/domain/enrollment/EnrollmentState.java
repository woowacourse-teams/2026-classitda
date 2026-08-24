package com.classitda.classes.domain.enrollment;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class EnrollmentState {

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "enrollment_status_changed_at", nullable = false)
    private LocalDateTime statusChangedAt;

    @Column(name = "offer_expires_at")
    private LocalDateTime offerExpiresAt;

    private EnrollmentState(
            EnrollmentStatus status,
            LocalDateTime statusChangedAt,
            LocalDateTime offerExpiresAt
    ) {
        this.status = status;
        this.statusChangedAt = statusChangedAt;
        this.offerExpiresAt = offerExpiresAt;
    }

    public static EnrollmentState waiting(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        return new EnrollmentState(EnrollmentStatus.WAITING, occurredAt, null);
    }

    public static EnrollmentState reserved(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        return new EnrollmentState(EnrollmentStatus.RESERVED, occurredAt, null);
    }

    public EnrollmentState offer(
            LocalDateTime occurredAt,
            LocalDateTime expiresAt
    ) {
        requireOccurredAt(occurredAt);
        if (expiresAt == null) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_OFFER_EXPIRES_AT_REQUIRED);
        }
        requireStatus(EnrollmentStatus.WAITING);
        if (!expiresAt.isAfter(occurredAt)) {
            throw new ClassException(ClassErrorCode.INVALID_ENROLLMENT_OFFER_DEADLINE);
        }

        return new EnrollmentState(EnrollmentStatus.OFFERED, occurredAt, expiresAt);
    }

    public EnrollmentState acceptOffer(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        requireStatus(EnrollmentStatus.OFFERED);
        if (offerExpiresAt == null || !occurredAt.isBefore(offerExpiresAt)) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_OFFER_EXPIRED);
        }

        return reserved(occurredAt);
    }

    public EnrollmentState cancelWaiting(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        requireWaitingOrOffered();
        return new EnrollmentState(EnrollmentStatus.CANCELED, occurredAt, null);
    }

    public EnrollmentState cancelReservation(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        requireStatus(EnrollmentStatus.RESERVED);
        return new EnrollmentState(EnrollmentStatus.CANCELED, occurredAt, null);
    }

    public EnrollmentState expire(LocalDateTime occurredAt) {
        requireOccurredAt(occurredAt);
        requireWaitingOrOffered();
        return new EnrollmentState(EnrollmentStatus.EXPIRED, occurredAt, null);
    }

    public void requireReserved() {
        requireStatus(EnrollmentStatus.RESERVED);
    }

    private void requireWaitingOrOffered() {
        if (status != EnrollmentStatus.WAITING
                && status != EnrollmentStatus.OFFERED) {
            throw new ClassException(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION);
        }
    }

    private void requireStatus(EnrollmentStatus requiredStatus) {
        if (status != requiredStatus) {
            throw new ClassException(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION);
        }
    }

    private static void requireOccurredAt(LocalDateTime occurredAt) {
        if (occurredAt == null) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_OCCURRED_AT_REQUIRED);
        }
    }
}
