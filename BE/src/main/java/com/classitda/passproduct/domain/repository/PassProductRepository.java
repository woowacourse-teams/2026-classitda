package com.classitda.passproduct.domain.repository;

import com.classitda.passproduct.domain.PassProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PassProductRepository extends JpaRepository<PassProduct, Long> {

    @Query("select passProduct from PassProduct passProduct "
            + "left join fetch passProduct.passProductClassTypes passProductClassType "
            + "left join fetch passProductClassType.classType classType "
            + "where passProduct.studio.id = :studioId "
            + "order by passProduct.id asc, classType.id asc")
    List<PassProduct> findAllWithClassTypesByStudioId(@Param("studioId") Long studioId);

    Optional<PassProduct> findByIdAndStudioId(Long passProductId, Long studioId);
}
