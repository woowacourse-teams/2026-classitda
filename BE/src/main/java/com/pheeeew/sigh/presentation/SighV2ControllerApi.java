package com.pheeeew.sigh.presentation;

import com.pheeeew.common.exception.ErrorResponse;
import com.pheeeew.sigh.presentation.dto.SighCreateV2Request;
import com.pheeeew.sigh.presentation.dto.SighFeature;
import com.pheeeew.sigh.presentation.dto.SighV2Properties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "한숨 v2", description = "메모와 익명 닉네임을 포함한 한숨 등록 API")
public interface SighV2ControllerApi {

    @Operation(
            summary = "메모와 익명 닉네임을 포함한 한숨 등록",
            description = """
                    ### 메모

                    - `memo`는 생략할 수 있습니다.
                    - 앞뒤 공백은 제거하며 빈 문자열과 공백만 있는 값은 `null`로 저장합니다.
                    - 정규화된 메모는 최대 200자입니다.

                    ### 익명 닉네임

                    - 닉네임은 클라이언트가 전달하지 않고 서버가 최초 등록 시 생성합니다.
                    - 닉네임 중복은 허용합니다.

                    ### 중복 요청

                    - 한 번의 등록 시도마다 새로운 `requestId`를 사용합니다.
                    - 같은 `requestId`로 재시도하면 좌표와 메모가 달라도 최초 등록 결과를 반환합니다.

                    ### 위치

                    - `latitude`와 `longitude`에는 클라이언트가 계산한 300m 격자 중심을 전달합니다.
                    - 서버는 격자 안에서 최종 표시 위치를 최초 한 번 생성하고 영구 저장합니다.
                    - 응답 좌표는 `[longitude, latitude]` 순서입니다.
                    - 상세 조회 API가 아직 없으므로 `Location` 헤더는 제공하지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "한숨 최초 등록 성공",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "이미 등록된 requestId의 최초 한숨 반환",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 본문, UUID, 좌표 범위 또는 메모 길이가 올바르지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "한숨을 저장하지 못했거나 처리하지 못한 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<SighFeature<SighV2Properties>> save(
            @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SighCreateV2Request.class),
                            examples = {
                                    @ExampleObject(
                                            name = "메모 포함",
                                            value = """
                                                    {
                                                      "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                                                      "latitude": 37.5664,
                                                      "longitude": 126.9780,
                                                      "memo": "오늘은 조금 지쳤다"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "메모 없음",
                                            value = """
                                                    {
                                                      "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                                                      "latitude": 37.5664,
                                                      "longitude": 126.9780
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            SighCreateV2Request request
    );
}
