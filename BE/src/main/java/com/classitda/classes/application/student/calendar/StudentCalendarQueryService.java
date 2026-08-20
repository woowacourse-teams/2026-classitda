package com.classitda.classes.application.student.calendar;

import com.classitda.classes.application.ClassSessionQueryRange;
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
public class StudentCalendarQueryService {

    private final StudentSessionAccessReader accessReader;
    private final StudentOwnedPassesReader ownedPassesReader;
    private final StudentCalendarSummaryReader summaryReader;
    private final Clock clock;

    public List<StudentCalendarSummary> findAll(
            Long memberId,
            Long studioId,
            LocalDate from,
            LocalDate to
    ) {
        ClassSessionQueryRange range = ClassSessionQueryRange.calendar(from, to);

        Long membershipId = accessReader.readMembershipId(memberId, studioId);
        StudentOwnedPasses ownedPasses = ownedPassesReader.read(membershipId, studioId);
        LocalDateTime now = LocalDateTime.now(clock);

        return summaryReader.read(
                studioId,
                membershipId,
                range,
                ownedPasses,
                now
        );
    }
}
