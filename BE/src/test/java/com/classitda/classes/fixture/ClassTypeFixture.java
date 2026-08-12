package com.classitda.classes.fixture;

import com.classitda.classes.domain.ClassType;
import com.classitda.classes.presentation.dto.ClassTypeCreateRequest;
import com.classitda.studio.domain.Studio;

public class ClassTypeFixture {

    public static ClassTypeCreateRequest 기본_수업_종류_생성_요청() {
        return 이름이_다른_수업_종류_생성_요청("일반 요가");
    }

    public static ClassTypeCreateRequest 이름이_다른_수업_종류_생성_요청(String name) {
        return ClassTypeCreateRequest.from(name);
    }

    public static ClassType 기본_수업_종류(Studio studio) {
        return 이름이_다른_수업_종류(studio, "일반 요가");
    }

    public static ClassType 이름이_다른_수업_종류(Studio studio, String name) {
        return ClassType.builder()
                .studio(studio)
                .name(name)
                .build();
    }
}
