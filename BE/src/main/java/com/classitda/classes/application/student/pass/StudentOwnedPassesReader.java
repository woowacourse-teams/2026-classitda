package com.classitda.classes.application.student.pass;

import com.classitda.passproduct.domain.repository.MemberPassProductRepository;
import com.classitda.passproduct.domain.repository.projection.MemberPassProductClassTypeProjection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentOwnedPassesReader {

    private final MemberPassProductRepository memberPassProductRepository;

    public StudentOwnedPasses read(Long membershipId, Long studioId) {
        List<MemberPassProductClassTypeProjection> passClassTypes = memberPassProductRepository
                .findAllOwnedWithClassTypeIds(membershipId, studioId);

        return StudentOwnedPasses.from(passClassTypes);
    }
}
