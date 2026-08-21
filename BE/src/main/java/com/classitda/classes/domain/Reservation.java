package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.common.domain.BaseEntity;
import com.classitda.passproduct.domain.MemberPassProduct;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservation")
@Entity
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id")
    private StudioMembership membership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_guest_id")
    private ClassGuest classGuest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pass_product_id")
    private MemberPassProduct memberPassProduct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    private LocalDateTime canceledAt;

    @Builder
    private Reservation(
            StudioMembership membership,
            ClassGuest classGuest,
            ClassSession classSession,
            MemberPassProduct memberPassProduct,
            LocalDateTime reservedAt
    ) {
        validateAttendee(membership, classGuest);
        validateClassSession(classSession);
        this.membership = membership;
        this.classGuest = classGuest;
        this.classSession = classSession;
        this.memberPassProduct = memberPassProduct;
        this.status = ReservationStatus.RESERVED;
        this.reservedAt = reservedAt;
    }

    public void cancel(LocalDateTime canceledAt) {
        if (isCanceled()) {
            throw new ClassException(ClassErrorCode.RESERVATION_ALREADY_CANCELED);
        }
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public boolean isCanceled() {
        return status == ReservationStatus.CANCELED;
    }

    public boolean isGuestReservation() {
        return classGuest != null;
    }

    public boolean belongsToSession(Long classSessionId) {
        return classSession.getId().equals(classSessionId);
    }

    private void validateAttendee(StudioMembership membership, ClassGuest classGuest) {
        if (membership == null && classGuest == null) {
            throw new ClassException(ClassErrorCode.RESERVATION_ATTENDEE_REQUIRED);
        }
        if (membership != null && classGuest != null) {
            throw new ClassException(ClassErrorCode.RESERVATION_ATTENDEE_AMBIGUOUS);
        }
    }

    private void validateClassSession(ClassSession classSession) {
        if (classSession == null) {
            throw new ClassException(ClassErrorCode.RESERVATION_SESSION_REQUIRED);
        }
    }
}
