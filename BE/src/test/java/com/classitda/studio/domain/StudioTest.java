package com.classitda.studio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.member.domain.Member;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class StudioTest {

    @Test
    void 운영_시작_시간이_종료_시간보다_빠르면_생성된다() {
        // given
        Member owner = StudioFixture.기본_소유자();

        // when
        Studio studio = Studio.builder()
                .owner(owner)
                .name("스튜디오")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();

        // then
        assertThat(studio.getOpenTime()).isBefore(studio.getCloseTime());
    }

    @Test
    void 운영_종료_시간이_시작_시간보다_빠르면_생성할_수_없다() {
        // given
        Member owner = StudioFixture.기본_소유자();

        // when / then
        assertThatThrownBy(() -> Studio.builder()
                .owner(owner)
                .name("스튜디오")
                .openTime(LocalTime.of(22, 0))
                .closeTime(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.INVALID_OPERATING_TIME.getMessage());
    }

    @Test
    void 운영_시작_시간과_종료_시간이_같으면_생성할_수_없다() {
        // given
        Member owner = StudioFixture.기본_소유자();

        // when / then
        assertThatThrownBy(() -> Studio.builder()
                .owner(owner)
                .name("스튜디오")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(StudioException.class);
    }
}
