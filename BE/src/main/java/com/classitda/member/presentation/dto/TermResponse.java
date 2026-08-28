package com.classitda.member.presentation.dto;

import com.classitda.member.domain.Term;
import com.classitda.member.domain.TermCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record TermResponse(
        @Schema(description = "약관 버전 row ID. 가입 요청의 agreedTermIds로 사용합니다.", example = "1")
        Long id,
        @Schema(description = "약관 종류", example = "SERVICE_TERMS")
        TermCode code,
        @Schema(description = "약관 제목", example = "서비스 이용약관")
        String title,
        @Schema(description = "약관 본문 URL", example = "https://example.invalid/terms/service-v1")
        String url,
        @Schema(description = "필수 동의 여부", example = "true")
        boolean required,
        @Schema(description = "약관 버전", example = "1")
        int version
) {

    public static TermResponse from(Term term) {
        return new TermResponse(
                term.getId(),
                term.getCode(),
                term.getTitle(),
                term.getUrl(),
                term.isRequired(),
                term.getVersion()
        );
    }
}
