package com.classitda.studio.application;

import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.passproduct.domain.repository.MemberPassProductRepository;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class StudioMembershipTerminationService {

    private final StudioMembershipRepository studioMembershipRepository;
    private final ClassSessionEnrollmentRepository classSessionEnrollmentRepository;
    private final MemberPassProductRepository memberPassProductRepository;
    private final ClassSessionRepository classSessionRepository;
    private final Clock clock;

    public void terminate(StudioMembership membership) {
        terminateAll(List.of(membership));
    }

    public void terminateByMemberId(Long memberId) {
        terminateAll(studioMembershipRepository.findAllByMemberIdIn(List.of(memberId)));
    }

    private void terminateAll(List<StudioMembership> memberships) {
        if (memberships.isEmpty()) {
            return;
        }

        cancelUpcomingEnrollments(memberships);
        memberships.forEach(this::withdrawOrDelete);
    }

    private void cancelUpcomingEnrollments(List<StudioMembership> memberships) {
        List<Long> membershipIds = memberships.stream()
                .map(StudioMembership::getId)
                .toList();
        LocalDateTime now = LocalDateTime.now(clock);
        List<ClassSessionEnrollment> upcoming =
                classSessionEnrollmentRepository.findActiveUpcomingByMembershipIds(membershipIds, now);

        upcoming.forEach(enrollment -> enrollment.cancelByMembershipTermination(now));
        classSessionEnrollmentRepository.flush();
    }

    private void withdrawOrDelete(StudioMembership membership) {
        if (hasHistory(membership.getId())) {
            membership.withdraw();
            return;
        }

        studioMembershipRepository.delete(membership);
    }

    private boolean hasHistory(Long membershipId) {
        return classSessionEnrollmentRepository.existsByMembershipId(membershipId)
                || memberPassProductRepository.existsByMembershipId(membershipId)
                || classSessionRepository.existsByInstructorMembershipId(membershipId);
    }
}
