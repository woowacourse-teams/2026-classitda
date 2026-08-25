package com.classitda.classes.application;

import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.session.ClassSessionClassType;
import com.classitda.classes.domain.session.ClassSessionDatePlan;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.classes.presentation.dto.ClassSessionCreateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionCreateV2Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateRequest;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV2Request;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.Clock;
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
    private final Clock clock;

    public void saveV1(Long memberId, Long studioId, ClassSessionCreateV1Request request) {
        Studio studio = getStudio(studioId);
        StudioMembership requesterMembership = getActiveMembership(memberId, studioId);
        validateManagePermission(studio, requesterMembership, requesterMembership.getId());
        validateInstructorForCreate(requesterMembership);

        saveClassSessions(studioId, requesterMembership, request);
    }

    public void saveV2(Long memberId, Long studioId, ClassSessionCreateV2Request request) {
        Studio studio = getStudio(studioId);
        StudioMembership requesterMembership = getActiveMembership(memberId, studioId);
        validateManagePermission(studio, requesterMembership, request.instructorMembershipId());

        StudioMembership instructorMembership = getActiveInstructorMembership(
                studioId, request.instructorMembershipId());

        saveClassSessions(studioId, instructorMembership, request);
    }

    private void saveClassSessions(
            Long studioId,
            StudioMembership instructorMembership,
            ClassSessionCreateRequest request
    ) {
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

    public void updateV1(Long memberId, Long studioId, Long classSessionId, ClassSessionUpdateV1Request request) {
        Studio studio = getStudio(studioId);
        StudioMembership requesterMembership = getActiveMembership(memberId, studioId);
        ClassSession classSession = getClassSession(studioId, classSessionId);

        validateManagePermission(
                studio,
                requesterMembership,
                classSession.getInstructorMembership().getId()
        );

        updateClassSession(studioId, classSession, classSession.getInstructorMembership(), request);
    }

    public void updateV2(Long memberId, Long studioId, Long classSessionId, ClassSessionUpdateV2Request request) {
        Studio studio = getStudio(studioId);
        StudioMembership requesterMembership = getActiveMembership(memberId, studioId);
        ClassSession classSession = getClassSession(studioId, classSessionId);

        Long currentInstructorMembershipId = classSession.getInstructorMembership().getId();
        Long targetInstructorMembershipId = request.instructorMembershipId();

        boolean instructorChanged =
                !currentInstructorMembershipId.equals(targetInstructorMembershipId);
        if (instructorChanged) {
            validateManageAllPermission(studio, requesterMembership);
        } else {
            validateManagePermission(
                    studio,
                    requesterMembership,
                    currentInstructorMembershipId
            );
        }

        StudioMembership instructorMembership
                = getActiveInstructorMembership(studioId, targetInstructorMembershipId);
        updateClassSession(studioId, classSession, instructorMembership, request);
    }

    private void updateClassSession(
            Long studioId,
            ClassSession classSession,
            StudioMembership instructorMembership,
            ClassSessionUpdateRequest request
    ) {
        validateClassType(studioId, request.classTypeId());
        updateClassSessionDetails(classSession, instructorMembership, request);
        updateClassSessionClassType(classSession.getId(), request.classTypeId());
    }

    public void cancel(Long memberId, Long studioId, Long classSessionId) {
        Studio studio = getStudio(studioId);
        StudioMembership requesterMembership = getActiveMembership(memberId, studioId);
        ClassSession classSession = classSessionRepository
                .findByIdAndStudioId(classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));

        validateManagePermission(
                studio,
                requesterMembership,
                classSession.getInstructorMembership().getId()
        );

        classSession.cancel(LocalDateTime.now(clock));
        // TODO: 수업 취소 시 예약 회원의 수강권 횟수를 복구한다.
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

    private ClassSession getClassSession(Long studioId, Long classSessionId) {
        return classSessionRepository.findByIdAndStudioId(classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
    }

    private StudioMembership getActiveInstructorMembership(Long studioId, Long instructorMembershipId) {
        if (instructorMembershipId == null) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND);
        }
        return studioMembershipRepository.findByIdAndStudioId(instructorMembershipId, studioId)
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .filter(StudioMembership::isInstructor)
                .orElseThrow(() -> new ClassException(
                        ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND
                ));
    }

    private void validateInstructorForCreate(StudioMembership membership) {
        if (!membership.isInstructor()) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND);
        }
    }

    private void validateManagePermission(
            Studio studio,
            StudioMembership requesterMembership,
            Long targetInstructorMembershipId
    ) {
        if (studio.isOwner(requesterMembership)) {
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

    private void validateManageAllPermission(Studio studio, StudioMembership requesterMembership) {
        if (studio.isOwner(requesterMembership)) {
            return;
        }

        if (!studioRolePermissionRepository.existsByStudioRoleIdAndPermissionCodeIn(
                requesterMembership.getStudioRole().getId(),
                List.of(PermissionCode.CLASS_SESSION_MANAGE_ALL)
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
            StudioMembership instructorMembership,
            ClassSessionUpdateRequest request
    ) {
        LocalDateTime endAt = calculateEndAt(request.startAt(), request.durationMinutes());
        validateNoInstructorTimeConflictExcluding(
                instructorMembership.getId(), classSession.getId(), request.startAt(), endAt);

        classSession.updateDetails(
                instructorMembership,
                request.className(),
                request.description(),
                request.classForm(),
                request.durationMinutes(),
                request.capacity(),
                request.startAt()
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
            Long instructorMembershipId,
            Long classSessionId,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        if (classSessionRepository.existsActiveOverlapExcluding(
                instructorMembershipId,
                classSessionId,
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

    private void updateClassSessionClassType(Long classSessionId, Long classTypeId) {
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
}
