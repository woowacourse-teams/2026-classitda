package com.pheeeew.data.remote.sigh.dto

import kotlinx.serialization.Serializable

/**
 * 한숨 등록 API에 전달하는 요청 DTO
 * [requestId]는 한 번의 등록 시도를 식별하는 값입니다.
 * 요청 좌표는 클라이언트에서 계산한 300m 격자 중심의
 * WGS84 위도(latitude), 경도(longitude)를 전달합니다.
 */
@Serializable
data class SighCreateRequestDto(
    val requestId: String,
    val latitude: Double,
    val longitude: Double,
)
