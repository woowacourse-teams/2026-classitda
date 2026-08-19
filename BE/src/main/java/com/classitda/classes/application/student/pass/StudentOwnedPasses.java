package com.classitda.classes.application.student.pass;

import com.classitda.passproduct.domain.repository.projection.MemberPassProductClassTypeProjection;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StudentOwnedPasses {

    private final List<StudentOwnedPass> passes;

    private StudentOwnedPasses(List<StudentOwnedPass> passes) {
        this.passes = passes;
    }

    static StudentOwnedPasses from(List<MemberPassProductClassTypeProjection> passClassTypes) {
        Map<Long, List<MemberPassProductClassTypeProjection>> classTypesByPassId = passClassTypes.stream()
                .collect(Collectors.groupingBy(
                        MemberPassProductClassTypeProjection::getMemberPassProductId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return new StudentOwnedPasses(classTypesByPassId.values().stream()
                .map(StudentOwnedPass::from)
                .toList());
    }

    public List<Long> coveredClassTypeIdsOn(LocalDate date) {
        return passes.stream()
                .filter(pass -> pass.isVisibleOn(date))
                .flatMap(pass -> pass.classTypeIds().stream())
                .distinct()
                .sorted()
                .toList();
    }

    public boolean covers(Long classTypeId, LocalDate date) {
        return passes.stream()
                .anyMatch(pass -> pass.covers(classTypeId, date));
    }
}
