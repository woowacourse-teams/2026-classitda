package com.classitda.classes.application.student.calendar;

import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.StudentCalendarSummaryProjection;
import com.classitda.passproduct.domain.PassProduct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentCalendarSummaryReader {

    private final ClassSessionRepository classSessionRepository;

    List<StudentCalendarSummary> read(
            Long studioId,
            Long membershipId,
            LocalDate from,
            LocalDate to,
            PassProduct passProduct,
            LocalDateTime now
    ) {
        List<Long> classTypeIds = passProduct.getClassTypes().stream()
                .map(ClassType::getId)
                .toList();
        if (classTypeIds.isEmpty()) {
            return List.of();
        }

        return classSessionRepository.findCalendarSummaryForStudent(
                        studioId,
                        membershipId,
                        from.atStartOfDay(),
                        to.plusDays(1).atStartOfDay(),
                        passProduct.getClassForm().name(),
                        classTypeIds,
                        now
                ).stream()
                .map(this::toSummary)
                .toList();
    }

    private StudentCalendarSummary toSummary(StudentCalendarSummaryProjection projection) {
        return new StudentCalendarSummary(
                projection.getDate(),
                projection.getAttended() > 0,
                projection.getReserved() > 0,
                projection.getWaiting() > 0
        );
    }
}
