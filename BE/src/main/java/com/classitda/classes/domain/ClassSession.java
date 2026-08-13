package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.common.domain.BaseEntity;
import com.classitda.studio.domain.Room;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassSessionStatus status;

    @Builder
    private ClassSession(
            Long studioId,
            Room room,
            StudioMembership instructorMembership,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            int capacity,
            LocalDateTime startAt,
            ClassSessionStatus status
    ) {
        validateStudioId(studioId);
        validateRoom(room);
        validateInstructorMembership(instructorMembership);
        validateName(name);
        validateClassForm(classForm);
        validateDurationMinutes(durationMinutes);
        validateCapacity(capacity);
        validateStartAt(startAt);
        validateStatus(status);
        this.studioId = studioId;
        this.room = room;
        this.instructorMembership = instructorMembership;
        this.name = name;
        this.description = description;
        this.classForm = classForm;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
        this.startAt = startAt;
        this.endAt = calculateEndAt(startAt, durationMinutes);
        this.status = status;
    }

    private void validateStudioId(Long studioId) {
        if (studioId == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_STUDIO_REQUIRED);
        }
    }

    private void validateRoom(Room room) {
        if (room == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_ROOM_REQUIRED);
        }
    }

    private void validateInstructorMembership(StudioMembership instructorMembership) {
        if (instructorMembership == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_INSTRUCTOR_REQUIRED);
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
        if (durationMinutes < 1) {
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

    private void validateStatus(ClassSessionStatus status) {
        if (status == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_STATUS_REQUIRED);
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
