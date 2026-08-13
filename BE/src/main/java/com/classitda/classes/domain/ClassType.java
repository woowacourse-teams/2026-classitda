package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassTypeErrorCode;
import com.classitda.classes.exception.ClassTypeException;
import com.classitda.common.domain.BaseEntity;
import com.classitda.studio.domain.Studio;
import jakarta.persistence.Column;
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
@Table(name = "class_type")
@Entity
public class ClassType extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Builder
    private ClassType(Studio studio, String name) {
        validateName(name);
        this.studio = studio;
        this.name = name;
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new ClassTypeException(ClassTypeErrorCode.INVALID_NAME);
        }
    }
}
