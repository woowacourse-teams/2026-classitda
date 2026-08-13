package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.common.domain.BaseEntity;
import com.classitda.studio.domain.Room;
import com.classitda.studio.domain.Studio;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "class_template")
@Entity
public class ClassTemplate extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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
    private LocalTime startTime;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "class_template_recurring_day", joinColumns = @JoinColumn(name = "class_template_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private Set<DayOfWeek> recurringDays = EnumSet.noneOf(DayOfWeek.class);

    @Column(nullable = false)
    private int capacity;

    @Builder
    private ClassTemplate(
            Studio studio,
            Room room,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            LocalTime startTime,
            Set<DayOfWeek> recurringDays,
            int capacity
    ) {
        validateDetails(name, classForm, durationMinutes, startTime, recurringDays, capacity);
        this.studio = studio;
        this.room = room;
        this.name = name;
        this.description = description;
        this.classForm = classForm;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.recurringDays = EnumSet.copyOf(recurringDays);
        this.capacity = capacity;
    }

    public void updateDetails(
            Room room,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            LocalTime startTime,
            Set<DayOfWeek> recurringDays,
            int capacity
    ) {
        validateDetails(name, classForm, durationMinutes, startTime, recurringDays, capacity);
        this.room = room;
        this.name = name;
        this.description = description;
        this.classForm = classForm;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.recurringDays = EnumSet.copyOf(recurringDays);
        this.capacity = capacity;
    }

    public Set<DayOfWeek> getRecurringDays() {
        return Collections.unmodifiableSet(EnumSet.copyOf(recurringDays));
    }

    private void validateDetails(
            String name,
            ClassForm classForm,
            int durationMinutes,
            LocalTime startTime,
            Set<DayOfWeek> recurringDays,
            int capacity
    ) {
        validateName(name);
        validateClassForm(classForm);
        validateDurationMinutes(durationMinutes);
        validateStartTime(startTime);
        validateRecurringDays(recurringDays);
        validateCapacity(capacity);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_TEMPLATE_NAME);
        }
    }

    private void validateClassForm(ClassForm classForm) {
        if (classForm == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_FORM);
        }
    }

    private void validateDurationMinutes(int durationMinutes) {
        if (durationMinutes < 1) {
            throw new ClassException(ClassErrorCode.INVALID_DURATION_MINUTES);
        }
    }

    private void validateStartTime(LocalTime startTime) {
        if (startTime == null) {
            throw new ClassException(ClassErrorCode.INVALID_START_TIME);
        }
    }

    private void validateRecurringDays(Set<DayOfWeek> recurringDays) {
        if (recurringDays == null || recurringDays.isEmpty()) {
            throw new ClassException(ClassErrorCode.RECURRING_DAYS_REQUIRED);
        }
        if (recurringDays.contains(null)) {
            throw new ClassException(ClassErrorCode.INVALID_RECURRING_DAY);
        }
    }

    private void validateCapacity(int capacity) {
        if (capacity < 1) {
            throw new ClassException(ClassErrorCode.INVALID_CAPACITY);
        }
    }
}
