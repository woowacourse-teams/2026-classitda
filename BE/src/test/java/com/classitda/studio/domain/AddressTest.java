package com.classitda.studio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import org.junit.jupiter.api.Test;

class AddressTest {

    private static final String ZONECODE = "06234";
    private static final String ROAD_ADDRESS = "서울 강남구 테헤란로 1";

    @Test
    void 우편번호와_도로명_주소로_주소를_만든다() {
        // given / when
        Address address = Address.of(ZONECODE, ROAD_ADDRESS, "서울 강남구 역삼동 823", "빌딩", "3층");

        // then
        assertThat(address.getZonecode()).isEqualTo(ZONECODE);
        assertThat(address.getRoadAddress()).isEqualTo(ROAD_ADDRESS);
    }

    @Test
    void 빈_지번과_건물명은_null로_저장한다() {
        // given / when
        Address address = Address.of(ZONECODE, ROAD_ADDRESS, "  ", "", null);

        // then
        assertThat(address.getJibunAddress()).isNull();
        assertThat(address.getBuildingName()).isNull();
        assertThat(address.getDetailAddress()).isNull();
    }

    @Test
    void 우편번호가_5자리_숫자가_아니면_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> Address.of("1234", ROAD_ADDRESS, null, null, null))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.INVALID_ZONECODE.getMessage());
    }

    @Test
    void 우편번호에_문자가_섞이면_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> Address.of("0623A", ROAD_ADDRESS, null, null, null))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.INVALID_ZONECODE.getMessage());
    }

    @Test
    void 도로명_주소가_비어_있으면_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> Address.of(ZONECODE, "  ", null, null, null))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.INVALID_ROAD_ADDRESS.getMessage());
    }

    @Test
    void 상세_주소가_100자를_넘으면_예외가_발생한다() {
        // given
        String tooLong = "가".repeat(101);

        // when / then
        assertThatThrownBy(() -> Address.of(ZONECODE, ROAD_ADDRESS, null, null, tooLong))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.INVALID_ADDRESS_LENGTH.getMessage());
    }

    @Test
    void 같은_값이면_같은_주소다() {
        // given
        Address first = Address.of(ZONECODE, ROAD_ADDRESS, null, null, "3층");
        Address second = Address.of(ZONECODE, ROAD_ADDRESS, null, null, "3층");

        // when / then
        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }
}
