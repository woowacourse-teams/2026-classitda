package com.classitda.studio.fixture;

import com.classitda.studio.presentation.dto.StudioMembershipCreateRequest;

public class StudioMembershipFixture {

    public static final String 기본_이름 = "김철수";
    public static final String 기본_전화번호 = "+821012345678";

    public static StudioMembershipCreateRequest 기본_소속_등록_요청() {
        return 소속_등록_요청(기본_이름, 기본_전화번호);
    }

    public static StudioMembershipCreateRequest 전화번호가_다른_소속_등록_요청(String phoneNumber) {
        return 소속_등록_요청(기본_이름, phoneNumber);
    }

    public static StudioMembershipCreateRequest 소속_등록_요청(String name, String phoneNumber) {
        return StudioMembershipCreateRequest.of(name, phoneNumber);
    }
}
