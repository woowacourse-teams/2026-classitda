package com.classitda.member.fixture;

import com.classitda.member.domain.Term;
import com.classitda.member.domain.TermCode;

public final class TermFixture {

    private TermFixture() {
    }

    public static Term 기본_약관() {
        return 약관(TermCode.SERVICE_TERMS, true, 1);
    }

    public static Term 약관(TermCode code, boolean required, int version) {
        return Term.builder()
                .code(code)
                .title(code.name() + " v" + version)
                .url("https://example.invalid/terms/" + code.name().toLowerCase() + "-v" + version)
                .required(required)
                .version(version)
                .build();
    }
}
