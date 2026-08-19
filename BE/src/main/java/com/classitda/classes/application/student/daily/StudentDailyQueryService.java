package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.pass.StudentOwnedPasses;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudentDailyQueryService {

    private final StudentSessionAccessReader accessReader;
    private final StudentOwnedPassesReader ownedPassesReader;
    private final StudentDailyScheduleReader scheduleReader;
    private final StudentDailySessionAssembler assembler;
    private final Clock clock;

    public List<StudentDailySessionView> findAll(
            Long memberId,
            Long studioId,
            LocalDate date
    ) {
        LocalDateTime now = LocalDateTime.now(clock);

        Long membershipId = accessReader.readMembershipId(memberId, studioId);
        StudentOwnedPasses ownedPasses = ownedPassesReader.read(membershipId, studioId);
        List<Long> classTypeIds = ownedPasses.coveredClassTypeIdsOn(date);
        if (classTypeIds.isEmpty()) {
            return List.of();
        }

        boolean attendanceHistoryOnly = date.isBefore(now.toLocalDate());
        StudentDailySchedule schedule = scheduleReader.read(
                studioId,
                membershipId,
                date,
                classTypeIds,
                ownedPasses,
                attendanceHistoryOnly
        );
        if (schedule.isEmpty()) {
            return List.of();
        }

        return assemble(schedule, now);
    }

    private List<StudentDailySessionView> assemble(
            StudentDailySchedule schedule,
            LocalDateTime now
    ) {
        return schedule.classSessions().stream()
                .map(classSession -> assembler.assemble(
                        classSession,
                        schedule.reservationSummary(classSession.getClassSessionId()),
                        schedule.waitingSummary(classSession.getClassSessionId()),
                        schedule.reservationCloseMinutesBefore(),
                        now
                ))
                .toList();
    }
}
