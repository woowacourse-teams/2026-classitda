package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassTemplateClassType;
import com.classitda.classes.domain.repository.projection.TemplateClassTypeProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassTemplateClassTypeRepository extends JpaRepository<ClassTemplateClassType, Long> {

    @Query("""
            SELECT link.classTemplateId AS classTemplateId,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName,
                   classType.studio.id AS studioId
            FROM ClassTemplateClassType link, ClassType classType
            WHERE link.classTypeId = classType.id
              AND link.classTemplateId IN :classTemplateIds
            ORDER BY link.classTemplateId, classType.id
            """)
    List<TemplateClassTypeProjection> findAllByTemplateIds(
            @Param("classTemplateIds") List<Long> classTemplateIds
    );
}
