package com.pheeeew.data.remote.sigh.dto

import kotlinx.serialization.Serializable

/**
 * GeoJSON Feature에 포함된 부가 정보 DTO
 *
 * 현재 서버에서는 한숨 생성 시각을 제공합니다.
 * 응답에 따라 properties가 없을 수 있으므로 nullable로 처리합니다.
 */
@Serializable
data class SighPropertiesDto(
    val createdAt: String? = null,
)
