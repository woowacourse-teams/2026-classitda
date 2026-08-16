package com.classitda.classes.application;

import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionClassType;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ClassSessionCommandService {

    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTemplateRepository classTemplateRepository;
    private final ClassTypeRepository classTypeRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;

    public void save(Long memberId, Long studioId, ClassSessionCreateRequest request) {
        StudioMembership instructorMembership = getInstructorForCreate(memberId, studioId);

        validateTemplate(studioId, request.classTemplateId());
        validateClassType(studioId, request.classTypeId());

        List<LocalDate> sessionDates = resolveSessionDates(request);
        List<ClassSession> classSessions = createClassSessions(
                studioId,
                instructorMembership,
                request,
                sessionDates
        );
        validateNoInstructorTimeConflicts(instructorMembership.getId(), classSessions);

        List<ClassSession> savedClassSessions = classSessionRepository.saveAll(classSessions);
        saveClassSessionClassTypes(savedClassSessions, request.classTypeId());
    }

    private StudioMembership getInstructorForCreate(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        StudioMembership membership = studioMembershipRepository
                .findByStudioIdAndMemberId(studioId, memberId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_MEMBERSHIP));

        validateActiveInstructor(membership);
        validateCreatePermission(studio, membership, memberId);

        return membership;
    }

    private void validateActiveInstructor(StudioMembership membership) {
        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new StudioException(StudioErrorCode.MEMBERSHIP_INACTIVE);
        }

        if (!membership.isInstructor()) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }
    }

    private void validateCreatePermission(
            Studio studio,
            StudioMembership membership,
            Long memberId
    ) {
        if (studio.isOwner(memberId)) {
            return;
        }

        Long studioRoleId = membership.getStudioRole().getId();
        boolean hasAllPermission = studioRolePermissionRepository
                .existsByStudioRoleIdAndPermissionCode(
                        studioRoleId,
                        PermissionCode.CLASS_SESSION_MANAGE_ALL
                );
        if (hasAllPermission) {
            return;
        }

        boolean hasOwnPermission = studioRolePermissionRepository
                .existsByStudioRoleIdAndPermissionCode(
                        studioRoleId,
                        PermissionCode.CLASS_SESSION_MANAGE_OWN
                );

        if (!hasOwnPermission) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }
    }

    private void validateTemplate(Long studioId, Long classTemplateId) {
        if (classTemplateId == null) {
            return;
        }

        classTemplateRepository.findByIdAndStudioId(classTemplateId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND));
    }

    private void validateClassType(Long studioId, Long classTypeId) {
        classTypeRepository.findByIdAndStudioId(classTypeId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    private List<LocalDate> resolveSessionDates(ClassSessionCreateRequest request) {
        if (request.recurring() == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE);
        }

        if (request.recurring()) {
            return resolveRecurringDates(request);
        }

        return resolveSingleDate(request);
    }

    private List<LocalDate> resolveSingleDate(ClassSessionCreateRequest request) {
        if (request.classDate() == null
                || request.recurringDays() != null
                || request.repeatStartDate() != null
                || request.repeatEndDate() != null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE);
        }

        return List.of(request.classDate());
    }

    private List<LocalDate> resolveRecurringDates(ClassSessionCreateRequest request) {
        if (request.classDate() != null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE);
        }

        List<DayOfWeek> recurringDays = request.recurringDays();
        validateRecurringDays(recurringDays);
        validateRepeatPeriod(request.repeatStartDate(), request.repeatEndDate());

        Set<DayOfWeek> selectedDays = new HashSet<>(recurringDays);
        List<LocalDate> sessionDates = new ArrayList<>();
        LocalDate currentDate = request.repeatStartDate();

        while (true) {
            if (selectedDays.contains(currentDate.getDayOfWeek())) {
                sessionDates.add(currentDate);
            }

            if (currentDate.equals(request.repeatEndDate())) {
                break;
            }

            currentDate = currentDate.plusDays(1);
        }

        if (sessionDates.isEmpty()) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_DATES_EMPTY);
        }

        return sessionDates;
    }

    private void validateRecurringDays(List<DayOfWeek> recurringDays) {
        if (recurringDays == null
                || recurringDays.isEmpty()
                || recurringDays.stream().anyMatch(dayOfWeek -> dayOfWeek == null)
                || new HashSet<>(recurringDays).size() != recurringDays.size()) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRING_DAYS);
        }
    }

    private void validateRepeatPeriod(LocalDate repeatStartDate, LocalDate repeatEndDate) {
        if (repeatStartDate == null
                || repeatEndDate == null
                || repeatStartDate.isAfter(repeatEndDate)) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD);
        }
    }

    private List<ClassSession> createClassSessions(
            Long studioId,
            StudioMembership instructorMembership,
            ClassSessionCreateRequest request,
            List<LocalDate> sessionDates
    ) {
        if (request.startTime() == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_START_AT);
        }
        if (request.durationMinutes() == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_DURATION_MINUTES);
        }
        if (request.capacity() == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_CAPACITY);
        }

        return sessionDates.stream()
                .map(sessionDate -> ClassSession.builder()
                        .studioId(studioId)
                        .instructorMembership(instructorMembership)
                        .name(request.name())
                        .description(request.memo())
                        .classForm(request.classForm())
                        .durationMinutes(request.durationMinutes())
                        .capacity(request.capacity())
                        .startAt(LocalDateTime.of(sessionDate, request.startTime()))
                        .status(ClassSessionStatus.OPENED)
                        .build())
                .toList();
    }

    private void validateNoInstructorTimeConflicts(
            Long instructorMembershipId,
            List<ClassSession> classSessions
    ) {
        for (ClassSession classSession : classSessions) {
            if (classSessionRepository.existsActiveOverlap(
                    instructorMembershipId,
                    classSession.getStartAt(),
                    classSession.getEndAt()
            )) {
                throw new ClassException(ClassErrorCode.CLASS_SESSION_TIME_CONFLICT);
            }
        }
    }

    private void saveClassSessionClassTypes(
            List<ClassSession> classSessions,
            Long classTypeId
    ) {
        List<ClassSessionClassType> classSessionClassTypes = classSessions.stream()
                .map(classSession -> ClassSessionClassType.builder()
                        .classSessionId(classSession.getId())
                        .classTypeId(classTypeId)
                        .build())
                .toList();

        classSessionClassTypeRepository.saveAll(classSessionClassTypes);
    }
}
