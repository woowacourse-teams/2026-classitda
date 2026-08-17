package com.classitda.passproduct.domain.repository;

import com.classitda.passproduct.domain.MemberPassProduct;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberPassProductRepository extends JpaRepository<MemberPassProduct, Long> {

    @Query("""
            SELECT DISTINCT memberPassProduct
            FROM MemberPassProduct memberPassProduct
            JOIN FETCH memberPassProduct.passProduct passProduct
            JOIN FETCH passProduct.passProductClassTypes passProductClassType
            JOIN FETCH passProductClassType.classType
            WHERE memberPassProduct.id = :memberPassProductId
              AND memberPassProduct.membership.id = :membershipId
              AND passProduct.studio.id = :studioId
            """)
    Optional<MemberPassProduct> findOwnedWithProductAndClassTypes(
            @Param("memberPassProductId") Long memberPassProductId,
            @Param("membershipId") Long membershipId,
            @Param("studioId") Long studioId
    );
}
