package com.pheeeew.sigh.presentation;

import com.pheeeew.common.exception.ErrorResponse;
import com.pheeeew.sigh.presentation.dto.SighCreateRequest;
import com.pheeeew.sigh.presentation.dto.SighResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "한숨", description = "한숨 등록 API")
public interface SighControllerApi {

    @Operation(
            summary = "한숨 등록",
            description = """
                    ### 중복 요청

                    - 한 번의 등록 시도마다 새로운 `requestId`를 사용합니다.
                    - 같은 `requestId`로 재시도하면 전달 좌표와 무관하게 최초 등록 결과를 반환합니다.

                    ### 위치

                    - `latitude`와 `longitude`에는 클라이언트가 계산한 300m 격자 중심을 전달합니다.
                    - 서버는 격자 안에서 최종 표시 위치를 최초 한 번 생성하고 영구 저장합니다.
                    - 응답 좌표는 GeoJSON과 MapLibre가 사용하는 `[longitude, latitude]` 순서입니다.
                    - 상세 조회 API가 없으므로 `Location` 헤더는 제공하지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "한숨 최초 등록 성공",
                    content = @Content(
                            mediaType = "application/geo+json",
                            schema = @Schema(implementation = SighResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "이미 등록된 requestId의 최초 한숨 반환",
                    content = @Content(
                            mediaType = "application/geo+json",
                            schema = @Schema(implementation = SighResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 본문, UUID 또는 좌표 범위가 올바르지 않음",
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
    ResponseEntity<SighResponse> save(SighCreateRequest request);
}
