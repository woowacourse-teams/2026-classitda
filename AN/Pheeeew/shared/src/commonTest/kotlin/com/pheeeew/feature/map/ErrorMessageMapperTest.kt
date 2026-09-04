@file:Suppress("NonAsciiCharacters")

package com.pheeeew.feature.map

import com.pheeeew.domain.exception.ApiException
import com.pheeeew.feature.map.map.MapError
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessageMapperTest {
    @Test
    fun `네트워크 예외는 사용자 안내 문구로 변환한다`() {
        val exception = ApiException.Network(code = "NETWORK_ERROR", message = "연결 실패")

        assertEquals(
            "네트워크 연결이 불안정해요. 인터넷 연결을 확인한 후 다시 시도해 주세요.",
            exception.toUserMessage(),
        )
    }

    @Test
    fun `네트워크 외 예외는 서버 메시지를 유지한다`() {
        val exception = ApiException.Unknown(code = "UNKNOWN_ERROR", message = "저장에 실패했습니다.")

        assertEquals("저장에 실패했습니다.", exception.toUserMessage())
    }

    @Test
    fun `지도 렌더러 오류는 사용자 안내 문구로 변환한다`() {
        assertEquals(
            "지도를 불러오지 못했어요. 잠시 후 재시도해 주세요.",
            MapError.RendererUnavailable.toUserMessage(),
        )
    }

    @Test
    fun `지도 스타일 오류는 사용자 안내 문구로 변환한다`() {
        assertEquals(
            "지도 화면을 불러오지 못했어요. 인터넷 연결 상태를 확인해 주세요.",
            MapError.StyleLoadFailed.toUserMessage(),
        )
    }
}
