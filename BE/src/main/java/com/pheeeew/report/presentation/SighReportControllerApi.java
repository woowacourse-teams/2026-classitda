package com.pheeeew.report.presentation;

import com.pheeeew.common.exception.ErrorResponse;
import com.pheeeew.report.presentation.dto.SighReportCreateRequest;
import com.pheeeew.report.presentation.dto.SighReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "신고", description = "한숨 신고 API")
public interface SighReportControllerApi {

    @Operation(
            summary = "한숨 신고",
            description = """
                    ### 신고자 구분

                    - `deviceId`는 앱 설치마다 한 번 생성해 기기에 보관하는 UUID입니다.
                    - 앱 자체를 가리키는 값이 아니므로 사용자마다 값이 다릅니다.
                    - 이 값으로만 신고자를 구분하므로 고정된 값을 재사용하면 안 됩니다.

                    ### 중복 신고

                    - 같은 기기는 같은 한숨을 한 번만 신고할 수 있습니다.
                    - 이미 신고한 한숨을 다시 신고하면 새로 저장하지 않고 최초 신고를 그대로 반환합니다.
                    - 이때 응답의 `reason`은 요청에 담긴 값이 아니라 최초 신고에 저장된 사유입니다.

                    ### 신고 사유

                    - `reason`은 정해진 항목 없이 자유롭게 입력하며 200자까지 허용합니다.
                    - 앞뒤 공백은 서버가 제거하고 저장합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "신고 최초 등록 성공",
                    content = @Content(schema = @Schema(implementation = SighReportResponse.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "이미 신고한 한숨이라 최초 신고를 반환",
                    content = @Content(schema = @Schema(implementation = SighReportResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "한숨 식별자, 기기 식별자나 신고 사유가 올바르지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "신고 대상 한숨이 존재하지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"SIGH-002","message":"한숨을 찾을 수 없습니다."}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "신고를 저장하지 못했거나 처리하지 못한 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<SighReportResponse> save(SighReportCreateRequest request);
}
