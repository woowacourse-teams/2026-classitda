package com.classitda.classes.application.student.pass;

import com.classitda.classes.domain.ClassForm;
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
        // TODO(#46, #68): 홀딩·이용 가능 횟수는 수업 노출 범위에서 제외하지 않는다.
        // 수강권 기능 구현 후 회차별 예약 가능 수강권 존재 여부와 홀딩 수강권 정보를 별도로 제공한다.
        return passes.stream()
                .filter(pass -> pass.isVisibleOn(date))
                .flatMap(pass -> pass.classTypeIds().stream())
                .distinct()
                .sorted()
                .toList();
    }

    public boolean covers(ClassForm classForm, Long classTypeId, LocalDate date) {
        return passes.stream()
                .anyMatch(pass -> pass.covers(classForm, classTypeId, date));
    }
}
