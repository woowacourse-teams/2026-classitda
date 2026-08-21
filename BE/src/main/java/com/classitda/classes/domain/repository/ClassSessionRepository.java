package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    @Query("select classSession from ClassSession classSession "
            + "join fetch classSession.instructorMembership "
            + "where classSession.id = :classSessionId "
            + "and classSession.studioId = :studioId")
    Optional<ClassSession> findWithInstructorByIdAndStudioId(
            @Param("classSessionId") Long classSessionId,
            @Param("studioId") Long studioId
    );
}
