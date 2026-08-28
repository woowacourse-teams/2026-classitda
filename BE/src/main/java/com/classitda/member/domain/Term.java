package com.classitda.member.domain;

import com.classitda.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "term")
@Entity
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermCode code;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int version;

    @Builder
    private Term(
            TermCode code,
            String title,
            String url,
            boolean required,
            int version
    ) {
        validateCode(code);
        validateTitle(title);
        validateUrl(url);
        validateVersion(version);
        this.code = code;
        this.title = title;
        this.url = url;
        this.required = required;
        this.version = version;
    }

    private void validateCode(TermCode code) {
        if (code == null) {
            throw new IllegalArgumentException("약관 코드는 필수입니다.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("약관 제목은 필수입니다.");
        }
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("약관 URL은 필수입니다.");
        }
    }

    private void validateVersion(int version) {
        if (version < 1) {
            throw new IllegalArgumentException("약관 버전은 1 이상이어야 합니다.");
        }
    }
}
