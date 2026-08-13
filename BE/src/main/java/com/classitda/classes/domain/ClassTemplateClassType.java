package com.classitda.classes.domain;

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
@Table(name = "class_template_class_type")
@Entity
public class ClassTemplateClassType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_template_id", nullable = false)
    private ClassTemplate classTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_type_id", nullable = false)
    private ClassType classType;

    @Builder
    private ClassTemplateClassType(ClassTemplate classTemplate, ClassType classType) {
        this.classTemplate = classTemplate;
        this.classType = classType;
    }
}
