package com.classitda.studio.domain;

import com.classitda.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "studio_role",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_studio_name",
                columnNames = {"studio_id", "name"}
        )
)
@Entity
public class StudioRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @Column(nullable = false)
    private boolean impliesInstructor;
}
