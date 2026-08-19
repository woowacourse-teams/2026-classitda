package com.classitda.classes.application.student.pass;

import com.classitda.classes.domain.ClassForm;
import com.classitda.passproduct.domain.repository.projection.MemberPassProductClassTypeProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

record StudentOwnedPass(
        ClassForm classForm,
        Set<Long> classTypeIds,
        LocalDate startedAt,
        LocalDate expiresAt
) {

    static StudentOwnedPass from(List<MemberPassProductClassTypeProjection> passClassTypes) {
        MemberPassProductClassTypeProjection firstClassType = passClassTypes.getFirst();
        Set<Long> classTypeIds = passClassTypes.stream()
                .map(MemberPassProductClassTypeProjection::getClassTypeId)
                .collect(Collectors.toUnmodifiableSet());

        return new StudentOwnedPass(
                firstClassType.getClassForm(),
                classTypeIds,
                firstClassType.getStartedAt(),
                firstClassType.getExpiresAt()
        );
    }

    boolean covers(ClassForm classForm, Long classTypeId, LocalDate date) {
        return this.classForm == classForm
                && classTypeIds.contains(classTypeId)
                && isVisibleOn(date);
    }

    boolean isVisibleOn(LocalDate date) {
        return !date.isBefore(startedAt) && !date.isAfter(expiresAt);
    }
}
