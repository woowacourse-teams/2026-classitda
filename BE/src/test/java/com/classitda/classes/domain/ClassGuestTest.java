package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ReservationFixture;
import com.classitda.studio.domain.Studio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ClassGuestTest {

    @Test
    void 이름과_연락처로_비회원을_만든다() {
        // when
        ClassGuest classGuest = ClassGuest.builder()
                .studio(시설())
                .name("현장 방문 손님")
                .phoneNumber("01012345678")
                .build();

        // then
        assertThat(classGuest.getName()).isEqualTo("현장 방문 손님");
        assertThat(classGuest.getPhoneNumber()).isEqualTo("01012345678");
    }

    @Test
    void 연락처_없이도_비회원을_만들_수_있다() {
        // when
        ClassGuest classGuest = ClassGuest.builder()
                .studio(시설())
                .name("이름만 아는 손님")
                .build();

        // then
        assertThat(classGuest.getPhoneNumber()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void 이름이_비어_있으면_비회원을_만들_수_없다(String name) {
        // when / then
        assertThatThrownBy(() -> ClassGuest.builder()
                .studio(시설())
                .name(name)
                .build())
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_CLASS_GUEST_NAME));
    }

    @Test
    void 이름이_오십자를_넘으면_비회원을_만들_수_없다() {
        // given
        String tooLongName = "가".repeat(51);

        // when / then
        assertThatThrownBy(() -> ClassGuest.builder()
                .studio(시설())
                .name(tooLongName)
                .build())
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_CLASS_GUEST_NAME));
    }

    @Test
    void 시설_없이_비회원을_만들_수_없다() {
        // when / then
        assertThatThrownBy(() -> ClassGuest.builder()
                .name("손님")
                .build())
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.CLASS_GUEST_STUDIO_REQUIRED));
    }

    private Studio 시설() {
        return ReservationFixture.기본_시설();
    }
}
