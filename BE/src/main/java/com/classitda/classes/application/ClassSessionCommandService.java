package com.classitda.classes.application;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionClassType;
import com.classitda.classes.domain.ClassSessionDatePlan;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.classes.presentation.dto.ClassSessionUpdateRequest;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ClassSessionCommandService {

    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;

    public void save(Long memberId, Long studioId, ClassSessionCreateRequest request) {
        Studio studio = getStudio(studioId);
        StudioMembership requesterMembership = getActiveMembership(memberId, studioId);
        validateManagePermission(studio, requesterMembership, request.instructorMembershipId(), memberId);

        StudioMembership instructorMembership = getInstructorForCreate(studioId, request.instructorMembershipId());

        validateClassType(studioId, request.classTypeId());

        List<LocalDate> sessionDates = ClassSessionDatePlan.of(
                request.recurring(),
                request.classDate(),
                request.recurringDays(),
                request.repeatStartDate(),
                request.repeatEndDate()
        ).dates();
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

    public void update(Long memberId, Long studioId, Long classSessionId, ClassSessionUpdateRequest request) {
        Studio studio = getStudio(studioId);
        StudioMembership requesterMembership = getActiveMembership(memberId, studioId);
        ClassSession classSession = classSessionRepository
                .findByIdAndStudioId(classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));

        validateManagePermission(
                studio,
                requesterMembership,
                classSession.getInstructorMembership().getId(),
                memberId
        );

        updateClassSessionDetails(classSession, request);
        if (request.classTypeId() != null) {
            updateClassSessionClassType(studioId, classSessionId, request.classTypeId());
        }
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
    }

    private StudioMembership getActiveMembership(Long memberId, Long studioId) {
        StudioMembership membership = studioMembershipRepository
                .findByStudioIdAndMemberId(studioId, memberId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_MEMBERSHIP));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new StudioException(StudioErrorCode.MEMBERSHIP_INACTIVE);
        }

        return membership;
    }

    private StudioMembership getInstructorForCreate(Long studioId, Long instructorMembershipId) {
        return studioMembershipRepository.findByIdAndStudioId(instructorMembershipId, studioId)
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .filter(StudioMembership::isInstructor)
                .orElseThrow(() -> new ClassException(
                        ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND
                ));
    }

    private void validateManagePermission(
            Studio studio,
            StudioMembership requesterMembership,
            Long targetInstructorMembershipId,
            Long memberId
    ) {
        if (studio.isOwner(memberId)) {
            return;
        }

        Long studioRoleId = requesterMembership.getStudioRole().getId();
        List<PermissionCode> manageablePermissions = List.of(
                PermissionCode.CLASS_SESSION_MANAGE_ALL
        );
        if (requesterMembership.getId().equals(targetInstructorMembershipId)) {
            manageablePermissions = List.of(
                    PermissionCode.CLASS_SESSION_MANAGE_ALL,
                    PermissionCode.CLASS_SESSION_MANAGE_OWN
            );
        }

        if (!studioRolePermissionRepository.existsByStudioRoleIdAndPermissionCodeIn(
                studioRoleId,
                manageablePermissions
        )) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }
    }

    private void validateClassType(Long studioId, Long classTypeId) {
        classTypeRepository.findByIdAndStudioId(classTypeId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    private void updateClassSessionDetails(
            ClassSession classSession,
            ClassSessionUpdateRequest request
    ) {
        String name = resolve(request.className(), classSession.getName());
        String description = resolve(request.description(), classSession.getDescription());
        ClassForm classForm = resolve(request.classForm(), classSession.getClassForm());
        int durationMinutes = resolve(
                request.durationMinutes(), classSession.getDurationMinutes());
        int capacity = resolve(request.capacity(), classSession.getCapacity());
        LocalDateTime startAt = resolve(request.startAt(), classSession.getStartAt());

        if (request.startAt() != null || request.durationMinutes() != null) {
            LocalDateTime endAt = calculateEndAt(startAt, durationMinutes);
            validateNoInstructorTimeConflictExcluding(classSession, startAt, endAt);
        }

        classSession.updateDetails(
                name,
                description,
                classForm,
                durationMinutes,
                capacity,
                startAt
        );
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
                        .name(request.className())
                        .description(request.description())
                        .classForm(request.classForm())
                        .durationMinutes(request.durationMinutes())
                        .capacity(request.capacity())
                        .startAt(LocalDateTime.of(sessionDate, request.startTime()))
                        .build())
                .toList();
    }

    private void validateNoInstructorTimeConflicts(Long instructorMembershipId, List<ClassSession> classSessions) {
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

    private void validateNoInstructorTimeConflictExcluding(
            ClassSession classSession,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        if (classSessionRepository.existsActiveOverlapExcluding(
                classSession.getInstructorMembership().getId(),
                classSession.getId(),
                startAt,
                endAt
        )) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_TIME_CONFLICT);
        }
    }

    private void saveClassSessionClassTypes(List<ClassSession> classSessions, Long classTypeId) {
        List<ClassSessionClassType> classSessionClassTypes = classSessions.stream()
                .map(classSession -> ClassSessionClassType.builder()
                        .classSessionId(classSession.getId())
                        .classTypeId(classTypeId)
                        .build())
                .toList();

        classSessionClassTypeRepository.saveAll(classSessionClassTypes);
    }

    private void updateClassSessionClassType(Long studioId, Long classSessionId, Long classTypeId) {
        validateClassType(studioId, classTypeId);

        ClassSessionClassType classSessionClassType = classSessionClassTypeRepository
                .findByClassSessionId(classSessionId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));

        classSessionClassType.updateClassTypeId(classTypeId);
    }

    private LocalDateTime calculateEndAt(LocalDateTime startAt, int durationMinutes) {
        if (startAt == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_START_AT);
        }
        if (durationMinutes < 1 || durationMinutes > 24 * 60) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_DURATION_MINUTES);
        }

        try {
            return startAt.plusMinutes(durationMinutes);
        } catch (DateTimeException exception) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_START_AT);
        }
    }

    private <T> T resolve(T requested, T current) {
        return requested != null ? requested : current;
    }
}
