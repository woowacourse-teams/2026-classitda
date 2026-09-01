package com.pheeeew.sigh.domain;

import static com.pheeeew.sigh.fixture.SighFixture.기본_한숨_빌더;
import static com.pheeeew.sigh.fixture.SighFixture.서울시청_좌표;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SighTest {

    @Test
    void 메모의_앞뒤_공백을_제거한다() {
        // given
        String memo = "  오늘은 힘들었다  ";

        // when
        Sigh sigh = 기본_한숨_빌더()
                .memo(memo)
                .build();

        // then
        assertThat(sigh.getMemo()).isEqualTo("오늘은 힘들었다");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void 빈_문자열과_공백으로만_이루어진_메모는_null로_변환한다(String memo) {
        // given
        String requestedMemo = memo;

        // when
        Sigh sigh = 기본_한숨_빌더()
                .memo(requestedMemo)
                .build();

        // then
        assertThat(sigh.getMemo()).isNull();
    }

    @Test
    void 메모는_50자를_초과하면_생성할_수_없다() {
        // given
        String memo = "가".repeat(51);

        // when
        Throwable throwable = catchThrowable(() -> 기본_한숨_빌더()
                .memo(memo)
                .build()
        );

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메모는 50자를 초과할 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void 닉네임은_null이거나_비어_있으면_생성할_수_없다(String nickname) {
        // given
        String requestedNickname = nickname;

        // when
        Throwable throwable = catchThrowable(() -> 기본_한숨_빌더()
                .nickname(requestedNickname)
                .build()
        );

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 비어 있을 수 없습니다.");
    }

    @Test
    void 닉네임은_50자를_초과하면_생성할_수_없다() {
        // given
        String nickname = "가".repeat(51);

        // when
        Throwable throwable = catchThrowable(() -> 기본_한숨_빌더()
                .nickname(nickname)
                .build()
        );

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 50자를 초과할 수 없습니다.");
    }

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
