package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
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
@Table(name = "class_guest")
@Entity
public class ClassGuest extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phoneNumber;

    @Builder
    private ClassGuest(Studio studio, String name, String phoneNumber) {
        validateStudio(studio);
        validateName(name);
        this.studio = studio;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public boolean belongsTo(Long studioId) {
        return studio.getId().equals(studioId);
    }

    private void validateStudio(Studio studio) {
        if (studio == null) {
            throw new ClassException(ClassErrorCode.CLASS_GUEST_STUDIO_REQUIRED);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_GUEST_NAME);
        }
    }
}
