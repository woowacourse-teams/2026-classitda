package com.classitda.studio.domain;

import com.classitda.common.domain.BaseEntity;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
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
@Table(name = "room")
@Entity
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false, length = 50)
    private String name;

    @Builder
    private Room(Studio studio, String name) {
        this.studio = studio;
        this.name = name;
    }

    public void validateBelongsTo(Long studioId) {
        if (!studio.getId().equals(studioId)) {
            throw new StudioException(StudioErrorCode.ROOM_NOT_FOUND);
        }
    }

    public void update(String name) {
        this.name = name;
    }
}
