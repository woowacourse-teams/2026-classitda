package com.classitda.classes.domain.enrollment;

import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.common.domain.BaseEntity;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.studio.domain.StudioMembership;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "class_session_enrollment")
@Entity
public class ClassSessionEnrollment extends BaseEntity {

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

    @Embedded
    private EnrollmentState state;

    @Embedded
    private Attendance attendance;

    private ClassSessionEnrollment(
            StudioMembership membership,
            ClassSession classSession,
            MemberPassProduct memberPassProduct,
            EnrollmentState state
    ) {
        requireMembership(membership);
        requireClassSession(classSession);

        this.membership = membership;
        this.classSession = classSession;
        this.memberPassProduct = memberPassProduct;
        this.state = state;
        this.attendance = Attendance.notRecorded();
    }

    public static ClassSessionEnrollment waiting(
            StudioMembership membership,
            ClassSession classSession,
            LocalDateTime occurredAt
    ) {
        requireMembership(membership);
        requireClassSession(classSession);
        return new ClassSessionEnrollment(
                membership,
                classSession,
                null,
                EnrollmentState.waiting(occurredAt)
        );
    }

    public static ClassSessionEnrollment reserved(
            StudioMembership membership,
            ClassSession classSession,
            MemberPassProduct memberPassProduct,
            LocalDateTime occurredAt
    ) {
        requireMemberPassProduct(memberPassProduct);
        requireMembership(membership);
        requireClassSession(classSession);
        return new ClassSessionEnrollment(
                membership,
                classSession,
                memberPassProduct,
                EnrollmentState.reserved(occurredAt)
        );
    }

    /**
     * MVP 단계에서 수강권 없이 등록 가능하다.
     */
    public static ClassSessionEnrollment reservedWithoutPassProduct(
            StudioMembership membership,
            ClassSession classSession,
            LocalDateTime occurredAt
    ) {
        requireMembership(membership);
        requireClassSession(classSession);
        return new ClassSessionEnrollment(
                membership,
                classSession,
                null,
                EnrollmentState.reserved(occurredAt)
        );
    }

    public void offer(LocalDateTime occurredAt, LocalDateTime expiresAt) {
        state = state.offer(occurredAt, expiresAt);
    }

    public void acceptOffer(
            MemberPassProduct memberPassProduct,
            LocalDateTime occurredAt
    ) {
        requireMemberPassProduct(memberPassProduct);
        EnrollmentState acceptedState = state.acceptOffer(occurredAt);

        this.memberPassProduct = memberPassProduct;
        state = acceptedState;
    }

    public void cancelWaiting(LocalDateTime occurredAt) {
        state = state.cancelWaiting(occurredAt);
    }

    public void cancelReservation(LocalDateTime occurredAt) {
        EnrollmentState canceledState = state.cancelReservation(occurredAt);
        if (attendance.isRecorded()) {
            throw new ClassException(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION);
        }

        state = canceledState;
    }

    public void expire(LocalDateTime occurredAt) {
        state = state.expire(occurredAt);
    }

    public void markAttended(LocalDateTime occurredAt) {
        state.requireReserved();
        attendance.markAttended(occurredAt);
    }

    public void markAbsent(LocalDateTime occurredAt) {
        state.requireReserved();
        attendance.markAbsent(occurredAt);
    }

    public EnrollmentStatus getEnrollmentStatus() {
        return state.getStatus();
    }

    public LocalDateTime getEnrollmentStatusChangedAt() {
        return state.getStatusChangedAt();
    }

    public LocalDateTime getOfferExpiresAt() {
        return state.getOfferExpiresAt();
    }

    private static void requireMembership(StudioMembership membership) {
        if (membership == null) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_MEMBERSHIP_REQUIRED);
        }
    }

    private static void requireClassSession(ClassSession classSession) {
        if (classSession == null) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_CLASS_SESSION_REQUIRED);
        }
    }

    private static void requireMemberPassProduct(
            MemberPassProduct memberPassProduct
    ) {
        if (memberPassProduct == null) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_MEMBER_PASS_PRODUCT_REQUIRED);
        }
    }
}
