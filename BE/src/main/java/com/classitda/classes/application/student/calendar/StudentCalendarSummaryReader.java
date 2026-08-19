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
        LocalDate today = now.toLocalDate();
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
                .forEach(event -> addReservationSummary(summaries, event, now, today));

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
                .filter(event -> !event.getStartAt().toLocalDate().isBefore(today))
                .filter(event -> now.isBefore(event.getEndAt()))
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
            LocalDateTime now,
            LocalDate today
    ) {
        LocalDate date = event.getStartAt().toLocalDate();
        if (event.getReservationStatus() == ReservationStatus.ATTENDED
                && !now.isBefore(event.getEndAt())) {
            merge(summaries, new StudentCalendarSummary(date, true, false, false));
            return;
        }
        if (event.getReservationStatus() == ReservationStatus.RESERVED
                && !date.isBefore(today)
                && now.isBefore(event.getEndAt())) {
            merge(summaries, new StudentCalendarSummary(date, false, true, false));
        }
    }

    private void merge(
            Map<LocalDate, StudentCalendarSummary> summaries,
            StudentCalendarSummary added
    ) {
        summaries.merge(added.date(), added, (existing, next) -> new StudentCalendarSummary(
                existing.date(),
                existing.attended() || next.attended(),
                existing.reserved() || next.reserved(),
                existing.waiting() || next.waiting()
        ));
    }
}
