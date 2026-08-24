package com.classitda.classes.domain.template;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.common.domain.BaseEntity;
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
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
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
    private static final int MAX_DURATION_MINUTES = 24 * 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

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
            Long studioId,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            LocalTime startTime,
            Set<DayOfWeek> recurringDays,
            int capacity
    ) {
        validateStudioId(studioId);
        validateDetails(name, classForm, durationMinutes, startTime, capacity);
        Set<DayOfWeek> copiedRecurringDays = copyRecurringDays(recurringDays);
        this.studioId = studioId;
        this.name = name;
        this.description = description;
        this.classForm = classForm;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.recurringDays = copiedRecurringDays;
        this.capacity = capacity;
    }

    private void validateStudioId(Long studioId) {
        if (studioId == null) {
            throw new ClassException(ClassErrorCode.CLASS_TEMPLATE_STUDIO_REQUIRED);
        }
    }

    public void updateDetails(
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            LocalTime startTime,
            Set<DayOfWeek> recurringDays,
            int capacity
    ) {
        validateDetails(name, classForm, durationMinutes, startTime, capacity);
        Set<DayOfWeek> copiedRecurringDays = copyRecurringDays(recurringDays);
        this.name = name;
        this.description = description;
        this.classForm = classForm;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.recurringDays.clear();
        this.recurringDays.addAll(copiedRecurringDays);
        this.capacity = capacity;
    }

    public Set<DayOfWeek> getRecurringDays() {
        return Collections.unmodifiableSet(copyRecurringDays(recurringDays));
    }

    private void validateDetails(
            String name,
            ClassForm classForm,
            int durationMinutes,
            LocalTime startTime,
            int capacity
    ) {
        validateName(name);
        validateClassForm(classForm);
        validateDurationMinutes(durationMinutes);
        validateStartTime(startTime);
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
        if (durationMinutes < 1 || durationMinutes > MAX_DURATION_MINUTES) {
            throw new ClassException(ClassErrorCode.INVALID_DURATION_MINUTES);
        }
    }

    private void validateStartTime(LocalTime startTime) {
        if (startTime == null) {
            throw new ClassException(ClassErrorCode.INVALID_START_TIME);
        }
    }

    private Set<DayOfWeek> copyRecurringDays(Set<DayOfWeek> source) {
        if (source == null || source.isEmpty()) {
            return EnumSet.noneOf(DayOfWeek.class);
        }
        if (source.stream().anyMatch(Objects::isNull)) {
            throw new ClassException(ClassErrorCode.INVALID_RECURRING_DAY);
        }
        return EnumSet.copyOf(source);
    }

    private void validateCapacity(int capacity) {
        if (capacity < 1) {
            throw new ClassException(ClassErrorCode.INVALID_CAPACITY);
        }
    }
}
