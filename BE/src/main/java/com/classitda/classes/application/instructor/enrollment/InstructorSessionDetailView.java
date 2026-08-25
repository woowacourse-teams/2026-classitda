package com.classitda.classes.application.instructor.enrollment;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.repository.projection.InstructorReservedMemberProjection;
import com.classitda.classes.domain.repository.projection.InstructorSessionDetailProjection;
import com.classitda.classes.domain.session.ClassSession;
import java.time.LocalDateTime;
import java.util.List;

public record InstructorSessionDetailView(
        Long id,
        Long instructorMembershipId,
        String instructorName,
        ClassForm classForm,
        Long classTypeId,
        String classTypeName,
        String className,
        String description,
        int capacity,
        int reservedCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        InstructorSessionStatus status,
        boolean mine,
        List<ReservedMember> reservedMembers
) {

    public static InstructorSessionDetailView of(
            InstructorSessionDetailProjection detail,
            InstructorSessionStatus status,
            boolean mine,
            List<InstructorReservedMemberProjection> reservedMembers
    ) {
        ClassSession session = detail.getSession();
        List<ReservedMember> members = reservedMembers.stream()
                .map(ReservedMember::from)
                .toList();

        return new InstructorSessionDetailView(
                session.getId(),
                detail.getInstructorMembershipId(),
                detail.getInstructorName(),
                session.getClassForm(),
                detail.getClassTypeId(),
                detail.getClassTypeName(),
                session.getName(),
                session.getDescription(),
                session.getCapacity(),
                members.size(),
                session.getStartAt(),
                session.getEndAt(),
                status,
                mine,
                members
        );
    }

    public record ReservedMember(
            Long enrollmentId,
            Long membershipId,
            String name,
            String profileImageUrl
    ) {

        private static ReservedMember from(InstructorReservedMemberProjection member) {
            return new ReservedMember(
                    member.getEnrollmentId(),
                    member.getMembershipId(),
                    member.getMemberName(),
                    member.getProfileImageUrl()
            );
        }
    }
}
