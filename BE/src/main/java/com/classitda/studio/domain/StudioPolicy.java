package com.classitda.studio.domain;

import com.classitda.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "studio_policy")
@Entity
public class StudioPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false)
    private int reservationCloseMinutesBefore;

    @Column(nullable = false)
    private int freeCancelMinutesBefore;

    @Column(nullable = false)
    private int waitingOfferResponseMinutes;

    @Column(nullable = false)
    private int maxHoldDays;

    @Builder
    private StudioPolicy(
            Studio studio,
            int reservationCloseMinutesBefore,
            int freeCancelMinutesBefore,
            int waitingOfferResponseMinutes,
            int maxHoldDays
    ) {
        this.studio = studio;
        this.reservationCloseMinutesBefore = reservationCloseMinutesBefore;
        this.freeCancelMinutesBefore = freeCancelMinutesBefore;
        this.waitingOfferResponseMinutes = waitingOfferResponseMinutes;
        this.maxHoldDays = maxHoldDays;
    }

    public void update(
            int reservationCloseMinutesBefore,
            int freeCancelMinutesBefore,
            int waitingOfferResponseMinutes,
            int maxHoldDays
    ) {
        this.reservationCloseMinutesBefore = reservationCloseMinutesBefore;
        this.freeCancelMinutesBefore = freeCancelMinutesBefore;
        this.waitingOfferResponseMinutes = waitingOfferResponseMinutes;
        this.maxHoldDays = maxHoldDays;
    }
}
