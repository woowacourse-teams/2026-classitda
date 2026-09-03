@file:Suppress("NonAsciiCharacters")

package com.pheeeew.feature.map

import com.pheeeew.feature.map.map.MapError
import kotlin.test.Test
import kotlin.test.assertNull

class MapErrorStateTest {
    @Test
    fun `지도 정상 복구 시 기존 오류를 초기화한다`() {
        val failedState = MapErrorState().onError(MapError.StyleLoadFailed)

        assertNull(failedState.onRecovered().error)
    }
}
