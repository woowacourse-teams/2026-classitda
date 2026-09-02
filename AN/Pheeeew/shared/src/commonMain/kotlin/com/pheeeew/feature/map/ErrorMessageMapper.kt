package com.pheeeew.feature.map

import com.pheeeew.domain.exception.ApiException
import com.pheeeew.feature.map.map.MapError

internal fun ApiException.toUserMessage(): String =
    when (this) {
        is ApiException.Network -> "네트워크 연결이 불안정해요. 인터넷 연결을 확인한 후 다시 시도해 주세요."
        else -> message
    }

internal fun MapError.toUserMessage(): String =
    when (this) {
        MapError.RendererUnavailable -> "지도를 불러오지 못했어요. 잠시 후 다시 시도해 주세요."
        MapError.StyleLoadFailed -> "지도 화면을 불러오지 못했어요. 인터넷 연결 상태를 확인해 주세요."
    }
