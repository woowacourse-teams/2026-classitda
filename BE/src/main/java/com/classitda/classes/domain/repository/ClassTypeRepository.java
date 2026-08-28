package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {

    List<ClassType> findAllByStudioIdOrderByIdAsc(Long studioId);

    List<ClassType> findAllByIdInAndStudioIdOrderByIdAsc(Collection<Long> classTypeIds, Long studioId);

    Optional<ClassType> findByIdAndStudioId(Long classTypeId, Long studioId);

    List<ClassType> findAllByStudioIdAndIdInOrderByIdAsc(Long studioId, List<Long> classTypeIds);
}
