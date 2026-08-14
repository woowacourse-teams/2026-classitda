package com.classitda.classes.domain;

import com.classitda.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "class_template_class_type")
@Entity
public class ClassTemplateClassType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_template_id", nullable = false)
    private Long classTemplateId;

    @Column(name = "class_type_id", nullable = false)
    private Long classTypeId;

    @Builder
    private ClassTemplateClassType(Long classTemplateId, Long classTypeId) {
        this.classTemplateId = classTemplateId;
        this.classTypeId = classTypeId;
    }
}
