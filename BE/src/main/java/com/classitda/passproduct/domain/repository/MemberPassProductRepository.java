package com.classitda.passproduct.domain.repository;

import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.passproduct.domain.repository.projection.MemberPassProductClassTypeProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberPassProductRepository extends JpaRepository<MemberPassProduct, Long> {

    @Query("""
            SELECT memberPassProduct.id AS memberPassProductId,
                   passProduct.classForm AS classForm,
                   passProductClassType.classType.id AS classTypeId,
                   memberPassProduct.startedAt AS startedAt,
                   memberPassProduct.expiresAt AS expiresAt
            FROM MemberPassProduct memberPassProduct
            JOIN memberPassProduct.passProduct passProduct
            JOIN passProduct.passProductClassTypes passProductClassType
            WHERE memberPassProduct.membership.id = :membershipId
              AND passProduct.studio.id = :studioId
            ORDER BY memberPassProduct.id, passProductClassType.classType.id
            """)
    List<MemberPassProductClassTypeProjection> findAllOwnedWithClassTypeIds(
            @Param("membershipId") Long membershipId,
            @Param("studioId") Long studioId
    );
}
