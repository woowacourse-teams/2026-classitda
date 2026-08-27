package com.classitda.data.remote.studio

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class StudioResponseDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `운영 시설 응답의 주소 객체를 파싱한다`() {
        val studio =
            json.decodeFromString<StudioResponseDto>(
                """
                {
                  "id": 1,
                  "name": "test2 studio",
                  "address": {
                    "zonecode": "06517",
                    "roadAddress": "서울특별시 서초구 강남대로",
                    "jibunAddress": "서울특별시 서초구 서초동",
                    "buildingName": "테스트빌딩",
                    "detailAddress": "101호"
                  },
                  "phoneNumber": "01012345678",
                  "openTime": "09:00:00",
                  "closeTime": "22:00:00"
                }
                """.trimIndent(),
            )

        assertEquals("test2 studio", studio.name)
        assertEquals("06517", studio.address.zonecode)
        assertEquals("서울특별시 서초구 강남대로", studio.address.roadAddress)
    }
}
