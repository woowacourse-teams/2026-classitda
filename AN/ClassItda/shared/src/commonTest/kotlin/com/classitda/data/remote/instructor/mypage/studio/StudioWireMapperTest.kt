package com.classitda.data.remote.instructor.mypage.studio

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.model.instructor.mypage.UploadedStudioImage
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudioWireMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `zoneCode는 zonecode로 직렬화하고 주소 다섯 값을 보존한다`() {
        val address = validAddress()

        val result = assertIs<InstructorMyPageResult.Success<AddressRequestDto>>(address.toAddressRequestDto())
        val encoded = json.encodeToString(result.value)

        assertTrue(encoded.contains("\"zonecode\":\"13494\""))
        assertFalse(encoded.contains("zoneCode"))
        assertEquals(address.zoneCode, result.value.zoneCode)
        assertEquals(address.roadAddress, result.value.roadAddress)
        assertEquals(address.jibunAddress, result.value.jibunAddress)
        assertEquals(address.buildingName, result.value.buildingName)
        assertEquals(address.detailAddress, result.value.detailAddress)
    }

    @Test
    fun `주소 요청은 필수값 형식과 최대 길이를 검증한다`() {
        val invalidAddresses =
            listOf(
                validAddress().copy(zoneCode = "1234"),
                validAddress().copy(zoneCode = "12A45"),
                validAddress().copy(roadAddress = " "),
                validAddress().copy(roadAddress = "a".repeat(256)),
                validAddress().copy(jibunAddress = "a".repeat(256)),
                validAddress().copy(buildingName = "a".repeat(101)),
                validAddress().copy(detailAddress = "a".repeat(101)),
            )

        invalidAddresses.forEach { address ->
            val failure = assertIs<InstructorMyPageResult.Failure>(address.toAddressRequestDto())
            assertEquals(InstructorMyPageFailureReason.INVALID_REQUEST, failure.reason)
        }
    }

    @Test
    fun `생성 JSON은 필수 필드와 업로드 objectKey를 사용한다`() {
        val localImage = localImage()
        val draft = validDraft(image = localImage)

        val result =
            assertIs<InstructorMyPageResult.Success<StudioCreateRequestDto>>(
                draft.toStudioCreateRequestDto(UploadedStudioImage("studios/images/object-key.jpg")),
            )
        val encoded = json.encodeToString(result.value)

        assertTrue(encoded.contains("\"name\":\"클래스잇다 스튜디오\""))
        assertTrue(encoded.contains("\"address\""))
        assertTrue(encoded.contains("\"phoneNumber\":\"031-123-4567\""))
        assertTrue(encoded.contains("\"openTime\":\"09:00\""))
        assertTrue(encoded.contains("\"closeTime\":\"22:00\""))
        assertTrue(encoded.contains("\"image\":\"studios/images/object-key.jpg\""))
        assertFalse(encoded.contains(localImage.previewReference))
    }

    @Test
    fun `로컬 이미지가 있는데 업로드 결과가 없으면 생성 요청을 만들지 않는다`() {
        val result = validDraft(image = localImage()).toStudioCreateRequestDto()

        val failure = assertIs<InstructorMyPageResult.Failure>(result)
        assertEquals(InstructorMyPageFailureReason.INVALID_REQUEST, failure.reason)
    }

    @Test
    fun `생성 요청은 시간 형식과 종료 시간이 시작 시간보다 늦은지 검증한다`() {
        val invalidDrafts =
            listOf(
                validDraft().copy(openingTime = "9:00"),
                validDraft().copy(closingTime = "25:00"),
                validDraft().copy(openingTime = "22:00", closingTime = "09:00"),
                validDraft().copy(openingTime = "09:00", closingTime = "09:00"),
            )

        invalidDrafts.forEach { draft ->
            val failure = assertIs<InstructorMyPageResult.Failure>(draft.toStudioCreateRequestDto())
            assertEquals(InstructorMyPageFailureReason.INVALID_REQUEST, failure.reason)
        }
    }

    @Test
    fun `수정 JSON은 지정한 선택 필드만 포함한다`() {
        val request = StudioUpdateRequestDto(name = "변경된 시설", description = "변경 설명")

        val encoded = json.encodeToString(request)

        assertTrue(encoded.contains("\"name\":\"변경된 시설\""))
        assertTrue(encoded.contains("\"description\":\"변경 설명\""))
        assertFalse(encoded.contains("address"))
        assertFalse(encoded.contains("image"))
    }

    @Test
    fun `시설 수정 mapper는 변경된 일반 필드만 포함한다`() {
        val original = validManagedStudio()
        val draft = validDraft(image = original.image).copy(name = "변경된 시설")

        val result =
            assertIs<InstructorMyPageResult.Success<StudioUpdateRequestDto>>(
                original.toStudioUpdateRequestDto(draft, StudioImageMutation.Unchanged),
            )
        val encoded = json.encodeToString(result.value)

        assertEquals("변경된 시설", result.value.name)
        assertNull(result.value.address)
        assertNull(result.value.phoneNumber)
        assertNull(result.value.openTime)
        assertNull(result.value.closeTime)
        assertNull(result.value.description)
        assertNull(result.value.image)
        assertTrue(encoded.contains("\"name\":\"변경된 시설\""))
        assertFalse(encoded.contains("address"))
    }

    @Test
    fun `주소가 변경되면 주소 다섯 값을 모두 포함한다`() {
        val original = validManagedStudio()
        val changedAddress =
            validAddress().copy(
                zoneCode = "13529",
                roadAddress = "서울특별시 강남구 테헤란로 1",
                jibunAddress = "서울특별시 강남구 역삼동 1",
                buildingName = "새 빌딩",
                detailAddress = "2층",
            )
        val draft = validDraft(image = original.image).copy(address = changedAddress)

        val request =
            assertIs<InstructorMyPageResult.Success<StudioUpdateRequestDto>>(
                original.toStudioUpdateRequestDto(draft, StudioImageMutation.Unchanged),
            ).value

        assertEquals(changedAddress.zoneCode, request.address?.zoneCode)
        assertEquals(changedAddress.roadAddress, request.address?.roadAddress)
        assertEquals(changedAddress.jibunAddress, request.address?.jibunAddress)
        assertEquals(changedAddress.buildingName, request.address?.buildingName)
        assertEquals(changedAddress.detailAddress, request.address?.detailAddress)
    }

    @Test
    fun `이미지 교체는 objectKey를 사용하고 제거는 image를 보내지 않는다`() {
        val original = validManagedStudio()
        val replacement = localImage()
        val replacementDraft = validDraft(image = replacement)
        val replacementRequest =
            assertIs<InstructorMyPageResult.Success<StudioUpdateRequestDto>>(
                original.toStudioUpdateRequestDto(
                    replacementDraft,
                    StudioImageMutation.Replace(replacement),
                    UploadedStudioImage("studio-images/new.jpg"),
                ),
            ).value
        assertEquals("studio-images/new.jpg", replacementRequest.image)
        assertFalse(replacementRequest.image!!.contains(replacement.handle))

        val removeDraft = validDraft(image = null)
        val removeRequest =
            assertIs<InstructorMyPageResult.Success<StudioUpdateRequestDto>>(
                original.toStudioUpdateRequestDto(removeDraft, StudioImageMutation.Remove),
            ).value
        assertNull(removeRequest.image)
        assertFalse(json.encodeToString(removeRequest).contains("image"))
    }

    @Test
    fun `업로드 URL 요청 JSON은 확장자와 실제 파일 크기를 사용한다`() {
        val request = ImageUploadUrlRequestDto(extension = "webp", size = 5L * 1024L * 1024L)

        val encoded = json.encodeToString(request)

        assertTrue(encoded.contains("\"extension\":\"webp\""))
        assertTrue(encoded.contains("\"size\":5242880"))
    }

    @Test
    fun `nullable 응답을 Domain으로 매핑하고 원격 이미지를 구분한다`() {
        val response = validResponse()

        val result = assertIs<InstructorMyPageResult.Success<*>>(response.toDomain())
        val studio = assertIs<com.classitda.domain.model.instructor.mypage.ManagedStudio>(result.value)
        val remote = assertIs<StudioImageSelection.Remote>(studio.image)

        assertEquals(InstructorStudioId("42"), studio.id)
        assertEquals("13494", studio.address.zoneCode)
        assertEquals("경기 성남시 분당구 판교역로 166", studio.address.roadAddress)
        assertEquals("https://cdn.classitda.com/studio.jpg", remote.imageUrl)
        assertEquals("09:00", studio.openingTime)
        assertEquals("22:00", studio.closingTime)
    }

    @Test
    fun `응답의 Domain 필수값 누락은 CONTRACT 오류다`() {
        val invalidResponses =
            listOf(
                validResponse().copy(id = null),
                validResponse().copy(name = null),
                validResponse().copy(address = null),
                validResponse().copy(phoneNumber = null),
                validResponse().copy(openTime = null),
                validResponse().copy(closeTime = null),
                validResponse().copy(image = " "),
            )

        invalidResponses.forEach { response ->
            val failure = assertIs<InstructorMyPageResult.Failure>(response.toDomain())
            assertEquals(InstructorMyPageFailureReason.CONTRACT, failure.reason)
        }
    }

    @Test
    fun `응답 주소의 선택 필드가 누락되어도 스튜디오를 매핑한다`() {
        val response =
            validResponse().copy(
                address =
                    AddressResponseDto(
                        zoneCode = null,
                        roadAddress = null,
                        jibunAddress = "지번 주소",
                    ),
            )

        val result = assertIs<InstructorMyPageResult.Success<*>>(response.toDomain())
        val studio = assertIs<com.classitda.domain.model.instructor.mypage.ManagedStudio>(result.value)

        assertEquals("", studio.address.zoneCode)
        assertEquals("", studio.address.roadAddress)
        assertEquals("지번 주소", studio.address.jibunAddress)
        assertEquals("", studio.address.buildingName)
        assertEquals("", studio.address.detailAddress)
    }

    @Test
    fun `Long ID는 Domain 문자열 ID로 바꾸고 잘못된 Domain ID는 거부한다`() {
        assertEquals(InstructorStudioId("9223372036854775807"), Long.MAX_VALUE.toInstructorStudioId())

        val valid = assertIs<InstructorMyPageResult.Success<Long>>(InstructorStudioId("42").toWireId())
        assertEquals(42L, valid.value)

        val invalid = assertIs<InstructorMyPageResult.Failure>(InstructorStudioId("studio-42").toWireId())
        assertEquals(InstructorMyPageFailureReason.INVALID_REQUEST, invalid.reason)
    }

    @Test
    fun `업로드 objectKey와 원격 표시 URL은 서로 다른 계약이다`() {
        val uploaded = UploadedStudioImage("studios/images/object-key.webp")
        val remote = StudioImageSelection.Remote("https://cdn.classitda.com/object-key.webp")

        assertEquals("studios/images/object-key.webp", uploaded.objectKey)
        assertEquals("https://cdn.classitda.com/object-key.webp", remote.imageUrl)
    }

    @Test
    fun `이미지 수정은 유지 교체 제거를 명시적으로 구분한다`() {
        val local = localImage()

        assertIs<StudioImageMutation.Unchanged>(StudioImageMutation.Unchanged)
        assertEquals(local, assertIs<StudioImageMutation.Replace>(StudioImageMutation.Replace(local)).image)
        assertIs<StudioImageMutation.Remove>(StudioImageMutation.Remove)
    }

    @Test
    fun `알 수 없는 응답 필드는 무시하고 optional 값은 nullable로 받는다`() {
        val decoded =
            json.decodeFromString<StudioResponseDto>(
                """
                {
                  "id":42,
                  "name":"클래스잇다 스튜디오",
                  "address":{"zonecode":"13494","roadAddress":"판교역로 166","future":"value"},
                  "phoneNumber":"031-123-4567",
                  "openTime":"09:00:00",
                  "closeTime":"22:00:00",
                  "futureField":true
                }
                """.trimIndent(),
            )
        val uploadResponse = json.decodeFromString<ImageUploadUrlResponseDto>("{}")

        assertEquals(42L, decoded.id)
        assertEquals("13494", decoded.address?.zoneCode)
        assertNull(decoded.image)
        assertNull(decoded.description)
        assertNull(uploadResponse.objectKey)
        assertNull(uploadResponse.uploadUrl)
        assertNull(uploadResponse.contentType)
    }

    private fun validAddress() =
        StudioAddress(
            zoneCode = "13494",
            roadAddress = "경기 성남시 분당구 판교역로 166",
            jibunAddress = "경기 성남시 분당구 백현동 532",
            buildingName = "카카오 판교 아지트",
            detailAddress = "3층",
        )

    private fun validDraft(image: StudioImageSelection? = null) =
        StudioRegistrationDraft(
            image = image,
            name = "클래스잇다 스튜디오",
            address = validAddress(),
            phoneNumber = "031-123-4567",
            description = "시설 설명",
            openingTime = "09:00",
            closingTime = "22:00",
        )

    private fun localImage() =
        StudioImageSelection.Local(
            handle = "opaque-handle",
            previewReference = "preview-reference",
            mimeType = "image/jpeg",
            fileName = "studio.jpg",
            sizeBytes = 1024,
        )

    private fun validResponse() =
        StudioResponseDto(
            id = 42,
            name = "클래스잇다 스튜디오",
            address =
                AddressResponseDto(
                    zoneCode = "13494",
                    roadAddress = "경기 성남시 분당구 판교역로 166",
                    jibunAddress = "경기 성남시 분당구 백현동 532",
                    buildingName = "카카오 판교 아지트",
                    detailAddress = "3층",
                ),
            phoneNumber = "031-123-4567",
            openTime = "09:00:00",
            closeTime = "22:00:00",
            image = "https://cdn.classitda.com/studio.jpg",
            description = "시설 설명",
        )

    private fun validManagedStudio() =
        com.classitda.domain.model.instructor.mypage.ManagedStudio(
            id = InstructorStudioId("42"),
            name = "클래스잇다 스튜디오",
            address = validAddress(),
            image = StudioImageSelection.Remote("https://cdn.classitda.com/studio.jpg"),
            phoneNumber = "031-123-4567",
            description = "시설 설명",
            openingTime = "09:00",
            closingTime = "22:00",
        )
}
