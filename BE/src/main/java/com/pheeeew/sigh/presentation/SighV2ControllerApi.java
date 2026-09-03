package com.pheeeew.sigh.presentation;

import com.pheeeew.common.exception.ErrorResponse;
import com.pheeeew.sigh.presentation.dto.SighCreateV2Request;
import com.pheeeew.sigh.presentation.dto.SighFeature;
import com.pheeeew.sigh.presentation.dto.SighV2Properties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "한숨 v2", description = "메모와 익명 닉네임을 포함한 한숨 등록과 상세 조회 API")
public interface SighV2ControllerApi {

    @Operation(
            summary = "한숨 상세 조회",
            description = """
                    - 경로의 `id`로 삭제되지 않은 한숨을 조회합니다.
                    - 등록 시 저장된 최종 표시 위치, 생성 시각, 메모와 익명 닉네임을 반환합니다.
                    - 조회할 때 위치나 닉네임을 다시 생성하지 않습니다.
                    - 메모가 없는 경우 `memo`는 `null`입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "한숨 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/geo+json",
                            schema = @Schema(
                                    allOf = SighFeature.class,
                                    properties = @StringToClassMapItem(
                                            key = "properties",
                                            value = SighV2Properties.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "한숨 ID 형식이 올바르지 않거나 1보다 작음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "한숨을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"SIGH-002","message":"한숨을 찾을 수 없습니다."}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "한숨 상세 조회 처리 중 서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SighFeature<SighV2Properties>> findById(
            @Parameter(
                    description = "조회할 한숨 ID",
                    example = "42",
                    schema = @Schema(minimum = "1")
            )
            @Min(value = 1, message = "한숨 ID는 1 이상이어야 합니다.")
            Long id
    );

    @Operation(
            summary = "메모와 익명 닉네임을 포함한 한숨 등록",
            description = """
                    ### 메모

                    - `memo`는 생략할 수 있습니다.
                    - 앞뒤 공백은 제거하며 빈 문자열과 공백만 있는 값은 `null`로 저장합니다.
                    - 정규화된 메모는 최대 50자입니다.

                    ### 익명 닉네임

                    - 닉네임은 클라이언트가 전달하지 않고 서버가 최초 등록 시 생성합니다.
                    - 닉네임 중복은 허용합니다.

                    ### 중복 요청

                    - 한 번의 등록 시도마다 새로운 `requestId`를 사용합니다.
                    - 같은 `requestId`로 재시도하면 좌표와 메모가 달라도 최초 등록 결과를 반환합니다.

                    ### 위치

                    - `latitude`와 `longitude`에는 클라이언트가 계산한 300m 격자 중심을 전달합니다.
                    - WGS84 위치를 EPSG:5179로 변환한 뒤 각 축에 `floor(value / 300) * 300 + 150`을 적용하고 WGS84로 되돌립니다.
                    - 클라이언트는 무작위 오프셋을 적용하거나 정확한 위치를 서버로 보내지 않습니다.
                    - 서버는 격자 안에서 최종 표시 위치를 최초 한 번 생성하고 영구 저장합니다.
                    - 응답 좌표는 `[longitude, latitude]` 순서입니다.
                    - 상세 조회 API가 아직 없으므로 `Location` 헤더는 제공하지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "한숨 최초 등록 성공",
                    content = @Content(
                            mediaType = "application/geo+json",
                            schema = @Schema(
                                    allOf = SighFeature.class,
                                    properties = @StringToClassMapItem(
                                            key = "properties",
                                            value = SighV2Properties.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "이미 등록된 requestId의 최초 한숨 반환",
                    content = @Content(
                            mediaType = "application/geo+json",
                            schema = @Schema(
                                    allOf = SighFeature.class,
                                    properties = @StringToClassMapItem(
                                            key = "properties",
                                            value = SighV2Properties.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 본문, UUID, 좌표 범위 또는 메모 길이가 올바르지 않음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "한숨을 저장하지 못했거나 처리하지 못한 서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
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
                                                      "latitude": 37.5657576255,
                                                      "longitude": 126.9774258201,
                                                      "memo": "오늘은 조금 지쳤다"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "메모 없음",
                                            value = """
                                                    {
                                                      "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                                                      "latitude": 37.5657576255,
                                                      "longitude": 126.9774258201
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid
            SighCreateV2Request request
    );
}
