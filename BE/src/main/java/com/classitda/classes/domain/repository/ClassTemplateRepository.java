package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassTemplateRepository extends JpaRepository<ClassTemplate, Long> {

    List<ClassTemplate> findAllByStudioIdOrderByIdAsc(Long studioId);

    @EntityGraph(attributePaths = "recurringDays")
    List<ClassTemplate> findAllByIdInOrderByIdAsc(List<Long> classTemplateIds);

    Optional<ClassTemplate> findByIdAndStudioId(Long id, Long studioId);
}
