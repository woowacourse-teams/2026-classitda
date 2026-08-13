package com.classitda.passproduct.domain;

import com.classitda.classes.domain.ClassType;
import com.classitda.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pass_product_class_type")
@Entity
public class PassProductClassType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pass_product_id", nullable = false)
    private PassProduct passProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_type_id", nullable = false)
    private ClassType classType;

    @Builder
    private PassProductClassType(PassProduct passProduct, ClassType classType) {
        this.passProduct = passProduct;
        this.classType = classType;
    }
}
