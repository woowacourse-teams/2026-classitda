package com.classitda.classes.domain.session;

import com.classitda.classes.domain.ClassForm;
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
import java.time.DateTimeException;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "class_session")
@Entity
public class ClassSession extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DURATION_MINUTES = 24 * 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_membership_id", nullable = false)
    private StudioMembership instructorMembership;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassForm classForm;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    private LocalDateTime canceledAt;

    @Builder
    private ClassSession(
            Long studioId,
            StudioMembership instructorMembership,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            int capacity,
            LocalDateTime startAt
    ) {
        validateStudioId(studioId);
        validateInstructorMembership(instructorMembership);
        validateDetails(name, classForm, durationMinutes, capacity, startAt);
        this.studioId = studioId;
        this.instructorMembership = instructorMembership;
        this.name = name;
        this.description = description;
        this.classForm = classForm;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
        this.startAt = startAt;
        this.endAt = calculateEndAt(startAt, durationMinutes);
    }

    public SessionPhase phaseAt(LocalDateTime now) {
        if (now == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_CURRENT_TIME_REQUIRED);
        }
        if (isCanceled()) {
            return SessionPhase.CANCELED;
        }
        if (!now.isBefore(endAt)) {
            return SessionPhase.COMPLETED;
        }
        if (!now.isBefore(startAt)) {
            return SessionPhase.IN_PROGRESS;
        }
        return SessionPhase.SCHEDULED;
    }

    public LocalDateTime bookingCloseAt(int reservationCloseMinutesBefore) {
        return startAt.minusMinutes(reservationCloseMinutesBefore);
    }

    public BookingWindow bookingWindowAt(LocalDateTime now, int reservationCloseMinutesBefore) {
        if (isCanceled()) {
            return BookingWindow.CLOSED;
        }
        return now.isBefore(bookingCloseAt(reservationCloseMinutesBefore)) ? BookingWindow.OPEN : BookingWindow.CLOSED;
    }

    public void cancel(LocalDateTime occurredAt) {
        if (occurredAt == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_CANCEL_OCCURRED_AT_REQUIRED);
        }
        if (isCanceled()) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_ALREADY_CANCELED);
        }
        if (!occurredAt.isBefore(startAt)) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_ALREADY_STARTED);
        }

        canceledAt = occurredAt;
    }

    public boolean isCanceled() {
        return canceledAt != null;
    }

    public void updateDetails(
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            int capacity,
            LocalDateTime startAt
    ) {
        updateDetails(
                instructorMembership,
                name,
                description,
                classForm,
                durationMinutes,
                capacity,
                startAt
        );
    }

    public void updateDetails(
            StudioMembership instructorMembership,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            int capacity,
            LocalDateTime startAt
    ) {
        validateUpdatable();
        validateInstructorMembership(instructorMembership);
        validateDetails(name, classForm, durationMinutes, capacity, startAt);
        LocalDateTime calculatedEndAt = calculateEndAt(startAt, durationMinutes);
        this.instructorMembership = instructorMembership;
        this.name = name;
        this.description = description;
        this.classForm = classForm;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
        this.startAt = startAt;
        this.endAt = calculatedEndAt;
    }

    private void validateStudioId(Long studioId) {
        if (studioId == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_STUDIO_REQUIRED);
        }
    }

    private void validateInstructorMembership(StudioMembership instructorMembership) {
        if (instructorMembership == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_INSTRUCTOR_REQUIRED);
        }
    }

    private void validateDetails(
            String name,
            ClassForm classForm,
            int durationMinutes,
            int capacity,
            LocalDateTime startAt
    ) {
        validateName(name);
        validateClassForm(classForm);
        validateDurationMinutes(durationMinutes);
        validateCapacity(capacity);
        validateStartAt(startAt);
    }

    private void validateUpdatable() {
        if (isCanceled()) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_CANCELED);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_NAME);
        }
    }

    private void validateClassForm(ClassForm classForm) {
        if (classForm == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_FORM);
        }
    }

    private void validateDurationMinutes(int durationMinutes) {
        if (durationMinutes < 1 || durationMinutes > MAX_DURATION_MINUTES) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_DURATION_MINUTES);
        }
    }

    private void validateCapacity(int capacity) {
        if (capacity < 1) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_CAPACITY);
        }
    }

    private void validateStartAt(LocalDateTime startAt) {
        if (startAt == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_START_AT);
        }
    }

    private LocalDateTime calculateEndAt(LocalDateTime startAt, int durationMinutes) {
        try {
            return startAt.plusMinutes(durationMinutes);
        } catch (DateTimeException exception) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_START_AT);
        }
    }
}
