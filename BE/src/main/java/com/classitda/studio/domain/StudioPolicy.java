package com.classitda.studio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
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
        name = "studio_policy",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_policy_studio",
                columnNames = "studio_id"
        )
)
@Entity
public class StudioPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false)
    private int reservationOpenMinutesBefore;

    @Column(nullable = false)
    private int reservationCloseMinutesBefore;

    @Column(nullable = false)
    private int freeCancelMinutesBefore;

    @Column(nullable = false)
    private boolean waitingEnabled;

    @Column(nullable = false)
    private int waitingOfferResponseMinutes;

    private LocalDateTime updatedAt;
}
