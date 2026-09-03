package com.pheeeew.domain.model.location

/**
 * 사용자의 현재 위치 조회 상태를 나타낸다.
 * 위치 조회 과정에서 상태는 다음 세 가지 중 하나만 가질 수 있다.
 *
 * - [Loading]: 위치를 조회 중인 상태
 * - [Available]: 유효한 현재 위치를 획득한 상태
 * - [Unavailable]: 위치를 획득하지 못한 상태
 */
sealed interface UserLocationState {
    /**
     * 현재 위치를 조회하고 있는 상태.
     */
    data object Loading : UserLocationState

    /**
     * 유효한 현재 위치를 획득한 상태.
     *
     * @property location GPS로 획득한 현재 위치 정보
     */
    data class Available(
        val location: CurrentLocation,
    ) : UserLocationState

    /**
     * 현재 위치를 획득하지 못한 상태.
     *
     * 위치 조회에 실패했더라도 지도 자체는 기본 위치를 중심으로
     * 표시할 수 있다. 단, 기본 위치를 실제 현재 위치로 사용해서는 안 된다.
     *
     * @property reason 위치를 획득하지 못한 원인
     */
    data class Unavailable(
        val reason: LocationError,
    ) : UserLocationState
}
