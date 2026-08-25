package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudioMembershipRepository extends JpaRepository<StudioMembership, Long> {

    Optional<StudioMembership> findByIdAndStudioId(Long membershipId, Long studioId);

    Optional<StudioMembership> findByStudioIdAndMemberId(Long studioId, Long memberId);

    boolean existsByStudioIdAndMemberId(Long studioId, Long memberId);

    List<StudioMembership> findAllByMemberIdIn(Collection<Long> memberIds);

    @EntityGraph(attributePaths = "member")
    @Query("""
            SELECT membership
            FROM StudioMembership membership
            WHERE membership.studio.id = :studioId
              AND membership.status = com.classitda.studio.domain.MembershipStatus.ACTIVE
              AND membership.studioRole.systemRole = com.classitda.studio.domain.SystemRole.STUDENT
            ORDER BY membership.id ASC
            """)
    List<StudioMembership> findActiveStudents(@Param("studioId") Long studioId);

    @Query("select studioMembership from StudioMembership studioMembership "
            + "join fetch studioMembership.member "
            + "join fetch studioMembership.studioRole studioRole "
            + "where studioMembership.studio.id = :studioId "
            + "and studioRole.instructor = :instructor "
            + "and studioMembership.id > :cursorId "
            + "order by studioMembership.id asc")
    Slice<StudioMembership> findWithCursorByStudioIdAndInstructor(
            @Param("studioId") Long studioId,
            @Param("instructor") boolean instructor,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("select studioMembership.studio from StudioMembership studioMembership "
            + "where studioMembership.member.id = :memberId "
            + "order by studioMembership.studio.id asc")
    List<Studio> findAllStudiosByMemberId(@Param("memberId") Long memberId);

    @Query("select studioMembership from StudioMembership studioMembership "
            + "join fetch studioMembership.member "
            + "join fetch studioMembership.studioRole "
            + "where studioMembership.id = :membershipId "
            + "and studioMembership.studio.id = :studioId")
    Optional<StudioMembership> findWithMemberByIdAndStudioId(
            @Param("membershipId") Long membershipId,
            @Param("studioId") Long studioId
    );
}
