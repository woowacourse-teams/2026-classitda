package com.classitda.classes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.member.domain.Member;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.fixture.StudioFixture;
import org.junit.jupiter.api.Test;

class ClassTypeTest {

    @Test
    void 수업_종류를_생성하면_시설과_이름을_가진다() {
        // given
        Studio studio = 기본_시설();

        // when
        ClassType classType = ClassTypeFixture.기본_수업_종류(studio);

        // then
        assertThat(classType.getStudio()).isSameAs(studio);
        assertThat(classType.getName()).isEqualTo("일반 요가");
    }

    @Test
    void 이름이_null이면_수업_종류를_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> ClassTypeFixture.이름이_다른_수업_종류(studio, null))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.INVALID_NAME.getMessage());
    }

    @Test
    void 이름이_공백이면_수업_종류를_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> ClassTypeFixture.이름이_다른_수업_종류(studio, "   "))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.INVALID_NAME.getMessage());
    }

    @Test
    void 이름이_51자면_수업_종류를_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();
        String name = "가".repeat(51);

        // when / then
        assertThatThrownBy(() -> ClassTypeFixture.이름이_다른_수업_종류(studio, name))
                .isInstanceOf(ClassException.class)
                .hasMessage(ClassErrorCode.INVALID_NAME.getMessage());
    }

    @Test
    void 이름이_1자면_수업_종류를_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();

        // when
        ClassType classType = ClassTypeFixture.이름이_다른_수업_종류(studio, "가");

        // then
        assertThat(classType.getName()).hasSize(1);
    }

    @Test
    void 이름이_50자면_수업_종류를_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();
        String name = "가".repeat(50);

        // when
        ClassType classType = ClassTypeFixture.이름이_다른_수업_종류(studio, name);

        // then
        assertThat(classType.getName()).hasSize(50);
    }

    @Test
    void 수업_종류_이름을_수정할_수_있다() {
        // given
        ClassType classType = ClassTypeFixture.기본_수업_종류(기본_시설());

        // when
        classType.updateName("리포머 요가");

        // then
        assertThat(classType.getName()).isEqualTo("리포머 요가");
    }

    @Test
    void 수정할_이름이_null이면_예외가_발생하고_기존_이름이_유지된다() {
        // given
        ClassType classType = ClassTypeFixture.기본_수업_종류(기본_시설());

        // when / then
        assertThatThrownBy(() -> classType.updateName(null))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_NAME));
        assertThat(classType.getName()).isEqualTo("일반 요가");
    }

    @Test
    void 수정할_이름이_공백이면_예외가_발생하고_기존_이름이_유지된다() {
        // given
        ClassType classType = ClassTypeFixture.기본_수업_종류(기본_시설());

        // when / then
        assertThatThrownBy(() -> classType.updateName("   "))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_NAME));
        assertThat(classType.getName()).isEqualTo("일반 요가");
    }

    @Test
    void 수정할_이름이_51자면_예외가_발생하고_기존_이름이_유지된다() {
        // given
        ClassType classType = ClassTypeFixture.기본_수업_종류(기본_시설());

        // when / then
        assertThatThrownBy(() -> classType.updateName("가".repeat(51)))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_NAME));
        assertThat(classType.getName()).isEqualTo("일반 요가");
    }

    @Test
    void 수업_종류_이름을_1자로_수정할_수_있다() {
        // given
        ClassType classType = ClassTypeFixture.기본_수업_종류(기본_시설());

        // when
        classType.updateName("가");

        // then
        assertThat(classType.getName()).hasSize(1);
    }

    @Test
    void 수업_종류_이름을_50자로_수정할_수_있다() {
        // given
        ClassType classType = ClassTypeFixture.기본_수업_종류(기본_시설());

        // when
        classType.updateName("가".repeat(50));

        // then
        assertThat(classType.getName()).hasSize(50);
    }

    private Studio 기본_시설() {
        Member owner = StudioFixture.기본_소유자();
        return StudioFixture.기본_시설(owner);
    }
}
