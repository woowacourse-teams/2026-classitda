package com.classitda.classes.domain.session;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
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
@Table(name = "class_session_class_type")
@Entity
public class ClassSessionClassType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_session_id", nullable = false)
    private Long classSessionId;

    @Column(name = "class_type_id", nullable = false)
    private Long classTypeId;

    @Builder
    private ClassSessionClassType(Long classSessionId, Long classTypeId) {
        validateClassTypeId(classTypeId);
        this.classSessionId = classSessionId;
        this.classTypeId = classTypeId;
    }

    public void updateClassTypeId(Long classTypeId) {
        validateClassTypeId(classTypeId);
        this.classTypeId = classTypeId;
    }

    private void validateClassTypeId(Long classTypeId) {
        if (classTypeId == null || classTypeId < 1) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_CLASS_TYPE_ID);
        }
    }
}
