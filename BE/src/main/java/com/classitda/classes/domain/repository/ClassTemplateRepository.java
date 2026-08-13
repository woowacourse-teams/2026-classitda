package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassTemplateRepository extends JpaRepository<ClassTemplate, Long> {

    List<ClassTemplate> findAllByStudioIdOrderByIdAsc(Long studioId);

    @EntityGraph(attributePaths = "recurringDays")
    List<ClassTemplate> findAllByIdInOrderByIdAsc(List<Long> classTemplateIds);

    Optional<ClassTemplate> findByIdAndStudioId(Long id, Long studioId);

    /**
     * 삭제에 엔티티 상태나 도메인 생명주기가 필요하지 않아 {@code delete(entity)}를 사용하지 않는다.
     * ID와 시설 소유 조건을 한 SQL에서 확인하며 물리 삭제하고, 영향 행 수로 없음·다른 시설을 007로 구분한다.
     * 반복 요일과 수업 종류 연결은 기존 DB {@code ON DELETE CASCADE}에 맡긴다.
     */
    @Modifying
    @Query(
            value = "DELETE FROM class_template WHERE id = :classTemplateId AND studio_id = :studioId",
            nativeQuery = true
    )
    int deleteByIdAndStudioId(
            @Param("classTemplateId") Long classTemplateId,
            @Param("studioId") Long studioId
    );
}
