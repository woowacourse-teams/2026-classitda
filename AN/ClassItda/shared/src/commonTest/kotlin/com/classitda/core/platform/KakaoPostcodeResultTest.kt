package com.classitda.core.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class KakaoPostcodeResultTest {
    @Test
    fun `카카오 결과 JSON은 네 주소 값을 보존한다`() {
        val result =
            KakaoPostcodeResult
                .parse(
                    """
                    {
                      "zonecode": "13494",
                      "roadAddress": "경기 성남시 분당구 판교역로 166",
                      "jibunAddress": "경기 성남시 분당구 백현동 532",
                      "buildingName": "카카오 판교 아지트",
                      "unusedField": "ignored"
                    }
                    """.trimIndent(),
                ).getOrThrow()

        assertEquals("13494", result.zoneCode)
        assertEquals("경기 성남시 분당구 판교역로 166", result.roadAddress)
        assertEquals("경기 성남시 분당구 백현동 532", result.jibunAddress)
        assertEquals("카카오 판교 아지트", result.buildingName)
    }

    @Test
    fun `도로명 또는 지번 주소 하나가 비어 있어도 결과를 허용한다`() {
        val roadOnly =
            KakaoPostcodeResult
                .parse(
                    """{"zonecode":"63240","roadAddress":"제주특별자치도 제주시 아란서길 164","jibunAddress":"","buildingName":""}""",
                ).getOrThrow()
        val jibunOnly =
            KakaoPostcodeResult
                .parse(
                    """{"zonecode":"63243","roadAddress":"","jibunAddress":"제주특별자치도 제주시 아라일동 1","buildingName":"제주대학교"}""",
                ).getOrThrow()

        assertEquals("", roadOnly.jibunAddress)
        assertEquals("", jibunOnly.roadAddress)
    }

    @Test
    fun `잘못된 JSON과 주소 없는 payload는 실패하고 wrapper 오류는 구분된다`() {
        assertFalse(KakaoPostcodeResult.parse("not-json").isSuccess)
        assertFalse(
            KakaoPostcodeResult
                .parse(
                    """{"zonecode":"13494","roadAddress":"","jibunAddress":"","buildingName":""}""",
                ).isSuccess,
        )

        val error =
            assertIs<KakaoPostcodeBridgeMessage.Error>(
                parseKakaoPostcodeBridgeMessage("""{"error":"NETWORK"}""").getOrThrow(),
            )
        assertEquals(KakaoPostcodeError.NETWORK, error.reason)
        assertEquals(
            KakaoPostcodeError.INVALID_PAYLOAD,
            assertIs<KakaoPostcodeSearchState.Error>(
                KakaoPostcodeSearchState.Error(KakaoPostcodeError.INVALID_PAYLOAD),
            ).reason,
        )
    }
}
