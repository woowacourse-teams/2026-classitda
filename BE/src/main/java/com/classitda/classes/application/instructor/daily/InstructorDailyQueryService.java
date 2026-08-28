package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.repository.projection.InstructorDailySessionProjection;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.pagination.CursorResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class InstructorDailyQueryService {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final InstructorSessionAccessReader accessReader;
    private final InstructorScheduleReader scheduleReader;
    private final Clock clock;

    public List<InstructorDailySessionView> findAll(Long memberId, Long studioId, LocalDate date) {
        Long requesterMembershipId = accessReader.readSessionAccess(memberId, studioId)
                .requesterMembershipId();
        InstructorDailySchedule schedule = scheduleReader.read(studioId, date);

        if (schedule.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now(clock);
        return schedule.classSessions().stream()
                .map(classSession -> assembleSession(
                        classSession,
                        schedule.reservationCloseMinutesBefore(),
                        requesterMembershipId,
                        now
                ))
                .toList();
    }

    public CursorResponse<InstructorDailySessionView> findWithCursor(
            Long memberId,
            Long studioId,
            String cursor,
            int size,
            ClassForm classForm,
            Long classTypeId
    ) {
        validateSize(size);
        Long requesterMembershipId = accessReader.readSessionAccess(memberId, studioId)
                .requesterMembershipId();
        InstructorSessionCursor decodedCursor = InstructorSessionCursor.decode(cursor);

        var slice = scheduleReader.readWithCursor(
                studioId,
                decodedCursor,
                size,
                classForm,
                classTypeId
        );
        if (slice.isEmpty()) {
            return CursorResponse.of(List.of(), false, null);
        }

        int reservationCloseMinutesBefore = scheduleReader.readReservationCloseMinutesBefore(studioId);
        LocalDateTime now = LocalDateTime.now(clock);
        List<InstructorDailySessionView> items = slice.getContent().stream()
                .map(classSession -> assembleSession(
                        classSession,
                        reservationCloseMinutesBefore,
                        requesterMembershipId,
                        now
                ))
                .toList();

        return CursorResponse.of(items, slice.hasNext(), toNextCursor(slice));
    }

    private void validateSize(int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private InstructorDailySessionView assembleSession(
            InstructorDailySessionProjection classSession,
            int reservationCloseMinutesBefore,
            Long requesterMembershipId,
            LocalDateTime now
    ) {
        ClassSession session = classSession.getSession();
        InstructorSessionStatus status = InstructorSessionStatus.from(
                session.phaseAt(now),
                session.bookingWindowAt(now, reservationCloseMinutesBefore)
        );

        return InstructorDailySessionView.of(
                classSession,
                status,
                requesterMembershipId.equals(session.getInstructorMembership().getId())
        );
    }

    private String toNextCursor(Slice<InstructorDailySessionProjection> slice) {
        if (!slice.hasNext()) {
            return null;
        }

        ClassSession lastSession = slice.getContent().getLast().getSession();
        return new InstructorSessionCursor(lastSession.getStartAt(), lastSession.getId()).encode();
    }
}
