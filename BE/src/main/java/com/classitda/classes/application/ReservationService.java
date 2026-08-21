package com.classitda.classes.application;

import com.classitda.classes.domain.ClassGuest;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.domain.repository.ClassGuestRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ReservationRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ReservationCreateRequest;
import com.classitda.classes.presentation.dto.ReservationResponse;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassGuestRepository classGuestRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;
    private final StudioPermissionService studioPermissionService;
    private final Clock clock;

    @Transactional
    public ReservationResponse save(
            Long memberId,
            Long studioId,
            Long classSessionId,
            ReservationCreateRequest request
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        ClassSession classSession = getManageableClassSession(memberId, studioId, classSessionId, now);
        validateCapacity(classSession);

        Reservation reservation = createReservation(studioId, classSession, request, now);

        return ReservationResponse.from(saveReservation(reservation));
    }

    @Transactional
    public void cancel(
            Long memberId,
            Long studioId,
            Long classSessionId,
            Long reservationId
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        getManageableClassSession(memberId, studioId, classSessionId, now);
        Reservation reservation = reservationRepository.findByIdAndClassSessionId(reservationId, classSessionId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.RESERVATION_NOT_FOUND));

        reservation.cancel(now);
    }

    private ClassSession getManageableClassSession(
            Long memberId,
            Long studioId,
            Long classSessionId,
            LocalDateTime now
    ) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
        studioPermissionService.validate(studio, memberId, PermissionCode.RESERVATION_MANAGE);

        ClassSession classSession = classSessionRepository
                .findWithInstructorByIdAndStudioId(classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
        validateInCharge(memberId, studioId, classSession);
        validateOpened(classSession, now);

        return classSession;
    }

    private void validateInCharge(Long memberId, Long studioId, ClassSession classSession) {
        StudioMembership requester = studioMembershipRepository.findByStudioIdAndMemberId(studioId, memberId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.MEMBERSHIP_NOT_FOUND));
        if (!classSession.isInstructedBy(requester.getId())) {
            throw new ClassException(ClassErrorCode.RESERVATION_SESSION_NOT_MANAGEABLE);
        }
    }

    private void validateOpened(ClassSession classSession, LocalDateTime now) {
        if (classSession.isCanceled() || classSession.hasEnded(now)) {
            throw new ClassException(ClassErrorCode.RESERVATION_SESSION_CLOSED);
        }
    }

    private void validateCapacity(ClassSession classSession) {
        int reserved = reservationRepository.countByClassSessionIdAndStatusNot(
                classSession.getId(), ReservationStatus.CANCELED);
        if (reserved >= classSession.getCapacity()) {
            throw new ClassException(ClassErrorCode.RESERVATION_CAPACITY_EXCEEDED);
        }
    }

    private Reservation createReservation(
            Long studioId,
            ClassSession classSession,
            ReservationCreateRequest request,
            LocalDateTime now
    ) {
        if (request.isGuestReservation()) {
            return Reservation.builder()
                    .classGuest(saveClassGuest(studioId, request))
                    .classSession(classSession)
                    .reservedAt(now)
                    .build();
        }

        StudioMembership membership = getStudentMembership(studioId, request.membershipId());
        validateNotDuplicated(classSession, membership);
        validateNotOverlapped(classSession, membership);

        return Reservation.builder()
                .membership(membership)
                .classSession(classSession)
                .reservedAt(now)
                .build();
    }

    private ClassGuest saveClassGuest(Long studioId, ReservationCreateRequest request) {
        Studio studio = studioRepository.getReferenceById(studioId);

        return classGuestRepository.save(ClassGuest.builder()
                .studio(studio)
                .name(request.guestName())
                .phoneNumber(request.guestPhoneNumber())
                .build());
    }

    private StudioMembership getStudentMembership(Long studioId, Long membershipId) {
        StudioMembership membership = studioMembershipRepository
                .findWithMemberByIdAndStudioId(membershipId, studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.MEMBERSHIP_NOT_FOUND));
        if (membership.isInstructor()) {
            throw new ClassException(ClassErrorCode.RESERVATION_INSTRUCTOR_NOT_ALLOWED);
        }

        return membership;
    }

    private void validateNotDuplicated(ClassSession classSession, StudioMembership membership) {
        boolean duplicated = reservationRepository.existsByClassSessionIdAndMembershipIdAndStatusNot(
                classSession.getId(), membership.getId(), ReservationStatus.CANCELED);
        if (duplicated) {
            throw new ClassException(ClassErrorCode.RESERVATION_DUPLICATED);
        }
    }

    private void validateNotOverlapped(ClassSession classSession, StudioMembership membership) {
        boolean overlapped = reservationRepository.existsOverlappingByMembershipId(
                membership.getId(),
                classSession.getStartAt(),
                classSession.getEndAt(),
                ReservationStatus.CANCELED);
        if (overlapped) {
            throw new ClassException(ClassErrorCode.RESERVATION_TIME_OVERLAPPED);
        }
    }

    private Reservation saveReservation(Reservation reservation) {
        try {
            return reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException exception) {
            throw new ClassException(ClassErrorCode.RESERVATION_DUPLICATED);
        }
    }
}
