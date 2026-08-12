package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {

    List<ClassType> findAllByStudioIdOrderByIdAsc(Long studioId);
}
