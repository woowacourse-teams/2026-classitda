package com.pheeeew.report.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SighReportCreateRequest(
        @NotNull(message = "신고할 한숨 식별자는 필수입니다.")
        @Positive(message = "한숨 식별자는 양수여야 합니다.")
        @Schema(description = "신고할 한숨 ID", minimum = "1", example = "42")
        Long sighId,

        @NotNull(message = "기기 식별자는 필수입니다.")
        @Schema(
                description = """
                        신고자를 구분하는 기기 식별자입니다.

                        앱을 설치할 때 한 번 생성해 기기에 보관하는 UUID이며, 사용자마다 값이 다릅니다.
                        앱 전체를 가리키는 고정값이 아니므로 상수를 재사용하면 안 됩니다.
                        """,
                example = "5d1ad34e-1e20-4f20-a20e-3825a095fe6b"
        )
        UUID deviceId,

        @NotBlank(message = "신고 사유는 필수입니다.")
        @Size(max = 200, message = "신고 사유는 200자 이하여야 합니다.")
        @Schema(
                description = """
                        신고 사유입니다.

                        정해진 항목 없이 자유롭게 입력합니다.
                        길이 제한은 보낸 값 그대로를 기준으로 검사하며, 저장할 때 앞뒤 공백은 제거합니다.
                        """,
                maxLength = 200,
                example = "광고성 게시물입니다"
        )
        String reason
) {
}
