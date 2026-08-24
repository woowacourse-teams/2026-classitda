package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.session.ClassSessionClassType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassSessionClassTypeRepository extends JpaRepository<ClassSessionClassType, Long> {

    Optional<ClassSessionClassType> findByClassSessionId(Long classSessionId);
}
