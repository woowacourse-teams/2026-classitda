package com.classitda.studio.fixture;

import com.classitda.studio.presentation.dto.RoomCreateRequest;
import com.classitda.studio.presentation.dto.RoomUpdateRequest;

public class RoomFixture {

    public static RoomCreateRequest 기본_룸_생성_요청() {
        return 이름이_다른_룸_생성_요청("A룸");
    }

    public static RoomCreateRequest 이름이_다른_룸_생성_요청(String name) {
        return new RoomCreateRequest(name);
    }

    public static RoomUpdateRequest 이름만_바꾸는_수정_요청(String name) {
        return new RoomUpdateRequest(name);
    }
}
