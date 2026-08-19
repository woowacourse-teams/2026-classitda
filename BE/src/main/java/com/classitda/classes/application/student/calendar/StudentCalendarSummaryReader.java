package com.classitda.classes.application.student.calendar;

import com.classitda.classes.application.student.pass.StudentOwnedPasses;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.domain.repository.ReservationRepository;
import com.classitda.classes.domain.repository.WaitingRepository;
import com.classitda.classes.domain.repository.projection.StudentReservationCalendarEventProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentCalendarSummaryReader {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    List<StudentCalendarSummary> read(
            Long studioId,
            Long membershipId,
            LocalDate from,
            LocalDate to,
            StudentOwnedPasses ownedPasses,
            LocalDateTime now
    ) {
        LocalDateTime rangeStart = from.atStartOfDay();
        LocalDateTime rangeEnd = to.plusDays(1).atStartOfDay();
        Map<LocalDate, StudentCalendarSummary> summaries = new TreeMap<>();

        reservationRepository.findCalendarEventsForStudent(
                        studioId,
                        membershipId,
                        rangeStart,
                        rangeEnd
                ).stream()
                .filter(event -> isCovered(
                        ownedPasses,
                        event.getClassForm(),
                        event.getClassTypeId(),
                        event.getStartAt()
                ))
                .forEach(event -> addReservationSummary(summaries, event, now));

        waitingRepository.findCalendarEventsForStudent(
                        studioId,
                        membershipId,
                        rangeStart,
                        rangeEnd
                ).stream()
                .filter(event -> isCovered(
                        ownedPasses,
                        event.getClassForm(),
                        event.getClassTypeId(),
                        event.getStartAt()
                ))
                .filter(event -> now.isBefore(event.getStartAt()))
                .forEach(event -> merge(
                        summaries,
                        new StudentCalendarSummary(event.getStartAt().toLocalDate(), false, false, true)
                ));

        return List.copyOf(summaries.values());
    }

    private boolean isCovered(
            StudentOwnedPasses ownedPasses,
            ClassForm classForm,
            Long classTypeId,
            LocalDateTime startAt
    ) {
        return ownedPasses.covers(classForm, classTypeId, startAt.toLocalDate());
    }

    private void addReservationSummary(
            Map<LocalDate, StudentCalendarSummary> summaries,
            StudentReservationCalendarEventProjection event,
            LocalDateTime now
    ) {
        LocalDate date = event.getStartAt().toLocalDate();
        if (!now.isBefore(event.getStartAt())) {
            merge(summaries, new StudentCalendarSummary(date, true, false, false));
            return;
        }
        if (event.getReservationStatus() == ReservationStatus.RESERVED) {
            merge(summaries, new StudentCalendarSummary(date, false, true, false));
        }
    }

    private void merge(
            Map<LocalDate, StudentCalendarSummary> summaries,
            StudentCalendarSummary added
    ) {
        summaries.merge(added.date(), added, (existing, next) -> new StudentCalendarSummary(
                existing.date(),
                existing.pastReservation() || next.pastReservation(),
                existing.reserved() || next.reserved(),
                existing.waiting() || next.waiting()
        ));
    }
}
