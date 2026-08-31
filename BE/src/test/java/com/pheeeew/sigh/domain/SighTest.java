package com.pheeeew.sigh.domain;

import static com.pheeeew.sigh.fixture.SighFixture.기본_한숨_빌더;
import static com.pheeeew.sigh.fixture.SighFixture.서울시청_좌표;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

class SighTest {

    @Test
    void 위치는_WGS84_좌표가_아니면_생성할_수_없다() {
        // given
        int invalidSrid = 0;

        // when
        Throwable throwable = catchThrowable(() -> 기본_한숨_빌더()
                .location(서울시청_좌표(invalidSrid))
                .build()
        );

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("위치는 비어 있지 않은 WGS84(SRID 4326) 점 좌표여야 합니다.");
    }
}
