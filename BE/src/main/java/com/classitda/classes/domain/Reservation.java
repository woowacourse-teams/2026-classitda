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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "reservation")
@Entity
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private StudioMembership membership;

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

    private LocalDateTime attendedAt;

    private LocalDateTime absentAt;

    public void cancel(LocalDateTime occurredAt) {
        if (occurredAt == null) {
            throw new ClassException(ClassErrorCode.RESERVATION_CANCEL_OCCURRED_AT_REQUIRED);
        }
        requireReserved();

        status = ReservationStatus.CANCELED;
        canceledAt = occurredAt;
    }

    public void markAttended(LocalDateTime occurredAt) {
        if (occurredAt == null) {
            throw new ClassException(
                    ClassErrorCode.RESERVATION_ATTENDANCE_OCCURRED_AT_REQUIRED
            );
        }
        requireReserved();

        status = ReservationStatus.ATTENDED;
        attendedAt = occurredAt;
    }

    public void markAbsent(LocalDateTime occurredAt) {
        if (occurredAt == null) {
            throw new ClassException(
                    ClassErrorCode.RESERVATION_ABSENCE_OCCURRED_AT_REQUIRED
            );
        }
        requireAbsentMarkable();

        status = ReservationStatus.ABSENT;
        attendedAt = null;
        absentAt = occurredAt;
    }

    private void requireReserved() {
        if (status != ReservationStatus.RESERVED) {
            throw new ClassException(ClassErrorCode.INVALID_RESERVATION_TRANSITION);
        }
    }

    private void requireAbsentMarkable() {
        if (status != ReservationStatus.RESERVED
                && status != ReservationStatus.ATTENDED) {
            throw new ClassException(ClassErrorCode.INVALID_RESERVATION_TRANSITION);
        }
    }
}
