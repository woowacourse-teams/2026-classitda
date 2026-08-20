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

    public List<StudentDailySessionView> findAll(Long memberId, Long studioId, LocalDate date) {
        Long membershipId = accessReader.readMembershipId(memberId, studioId);
        StudentOwnedPasses ownedPasses = ownedPassesReader.read(membershipId, studioId);
        List<Long> classTypeIds = ownedPasses.coveredClassTypeIdsOn(date);

        if (classTypeIds.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now(clock);
        boolean enrollmentHistoryOnly = date.isBefore(now.toLocalDate());
        StudentDailySchedule schedule = scheduleReader.read(
                studioId,
                membershipId,
                date,
                classTypeIds,
                ownedPasses,
                enrollmentHistoryOnly
        );
        if (schedule.isEmpty()) {
            return List.of();
        }

        return schedule.classSessions().stream()
                .map(classSession -> assembler.assemble(
                        classSession,
                        schedule.reservationCloseMinutesBefore(),
                        now
                ))
                .toList();
    }
}
