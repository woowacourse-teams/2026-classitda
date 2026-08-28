package com.classitda.passproduct.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.member.domain.Member;
import com.classitda.passproduct.exception.PassProductErrorCode;
import com.classitda.passproduct.exception.PassProductException;
import com.classitda.passproduct.fixture.PassProductFixture;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.fixture.StudioFixture;
import java.util.List;
import org.junit.jupiter.api.Test;

class PassProductTest {

    @Test
    void 수강권을_생성하면_판매중_상태로_시작한다() {
        // given
        Studio studio = 기본_시설();

        // when
        PassProduct passProduct = PassProductFixture.기본_수강권(studio);

        // then
        assertThat(passProduct.getStudio()).isSameAs(studio);
        assertThat(passProduct.getName()).isEqualTo(PassProductFixture.기본_이름);
        assertThat(passProduct.getClassForm()).isEqualTo(ClassForm.GROUP);
        assertThat(passProduct.getTotalCount()).isEqualTo(PassProductFixture.기본_횟수);
        assertThat(passProduct.getValidPeriodUnit()).isEqualTo(PassProductPeriodUnit.MONTH);
        assertThat(passProduct.isActive()).isTrue();
    }

    @Test
    void 수강권을_생성할_때_지정한_수업_종류가_함께_묶인다() {
        // given
        Studio studio = 기본_시설();
        List<ClassType> classTypes = List.of(
                ClassTypeFixture.이름이_다른_수업_종류(studio, "요가"),
                ClassTypeFixture.이름이_다른_수업_종류(studio, "필라테스")
        );

        // when
        PassProduct passProduct = PassProductFixture.수업_종류를_지정한_수강권(studio, classTypes);

        // then
        assertThat(passProduct.getPassProductClassTypes())
                .extracting(PassProductClassType::getClassType)
                .containsExactlyElementsOf(classTypes);
        assertThat(passProduct.getPassProductClassTypes())
                .allSatisfy(passProductClassType ->
                        assertThat(passProductClassType.getPassProduct()).isSameAs(passProduct));
    }

    @Test
    void 수업_종류를_지정하지_않으면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수업_종류를_지정한_수강권(studio, List.of()))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.CLASS_TYPE_REQUIRED));
    }

    @Test
    void 수업_종류를_비우도록_수정할_수_없다() {
        // given
        Studio studio = 기본_시설();
        PassProduct passProduct = PassProductFixture.기본_수강권(studio);

        // when / then
        assertThatThrownBy(() -> passProduct.updateClassTypes(List.of()))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.CLASS_TYPE_REQUIRED));
        assertThat(passProduct.getPassProductClassTypes()).hasSize(1);
    }

    @Test
    void 이름이_공백이면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.이름이_다른_수강권(studio, "   "))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_NAME));
    }

    @Test
    void 이름이_101자면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.이름이_다른_수강권(studio, "가".repeat(101)))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_NAME));
    }

    @Test
    void 이름이_100자면_수강권을_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();

        // when
        PassProduct passProduct = PassProductFixture.이름이_다른_수강권(studio, "가".repeat(100));

        // then
        assertThat(passProduct.getName()).hasSize(100);
    }

    @Test
    void 수업_형태가_없으면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", null, 20, 3, PassProductPeriodUnit.MONTH, 0))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_CLASS_KIND));
    }

    @Test
    void 유효기간_단위만_지정하면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, 20, null, PassProductPeriodUnit.MONTH, 0))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_VALID_PERIOD));
    }

    @Test
    void 유효기간_값만_지정하면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, 20, 3, null, 0))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_VALID_PERIOD));
    }

    @Test
    void 유효기간이_0이면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, 20, 0, PassProductPeriodUnit.MONTH, 0))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_VALID_PERIOD));
    }

    @Test
    void 수강_가능_횟수가_0이면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, 0, 3, PassProductPeriodUnit.MONTH, 0))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_TOTAL_COUNT));
    }

    @Test
    void 횟수와_기간이_모두_무제한이면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, null, null, null, 0))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.NO_EXPIRATION_CONDITION));
    }

    @Test
    void 횟수만_무제한이면_수강권을_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();

        // when
        PassProduct passProduct = PassProductFixture.수강권(
                studio, "3개월 무제한권", ClassForm.GROUP, null, 3, PassProductPeriodUnit.MONTH, 0);

        // then
        assertThat(passProduct.isUnlimitedCount()).isTrue();
        assertThat(passProduct.isUnlimitedPeriod()).isFalse();
    }

    @Test
    void 기간만_무제한이면_수강권을_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();

        // when
        PassProduct passProduct = PassProductFixture.수강권(
                studio, "기한 없는 20회권", ClassForm.INDIVIDUAL, 20, null, null, 0);

        // then
        assertThat(passProduct.isUnlimitedPeriod()).isTrue();
        assertThat(passProduct.isUnlimitedCount()).isFalse();
    }

    @Test
    void 홀딩_일수가_음수면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, 20, 3, PassProductPeriodUnit.MONTH, -1))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.INVALID_HOLD_DAYS));
    }

    @Test
    void 기간이_무제한인데_홀딩_일수가_있으면_수강권을_생성할_수_없다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatThrownBy(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, 20, null, null, 1))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(PassProductErrorCode.HOLD_DAYS_NOT_ALLOWED));
    }

    @Test
    void 기간이_무제한이어도_홀딩_일수가_0이면_수강권을_생성할_수_있다() {
        // given
        Studio studio = 기본_시설();

        // when / then
        assertThatCode(() -> PassProductFixture.수강권(
                studio, "이름", ClassForm.GROUP, 20, null, null, 0))
                .doesNotThrowAnyException();
    }

    @Test
    void 수강권을_수정하면_모든_필드가_교체된다() {
        // given
        PassProduct passProduct = PassProductFixture.기본_수강권(기본_시설());

        // when
        passProduct.update("개인 10회권", ClassForm.INDIVIDUAL, 10, 30, PassProductPeriodUnit.DAY, 3, false);

        // then
        assertThat(passProduct.getName()).isEqualTo("개인 10회권");
        assertThat(passProduct.getClassForm()).isEqualTo(ClassForm.INDIVIDUAL);
        assertThat(passProduct.getTotalCount()).isEqualTo(10);
        assertThat(passProduct.getValidPeriodAmount()).isEqualTo(30);
        assertThat(passProduct.getValidPeriodUnit()).isEqualTo(PassProductPeriodUnit.DAY);
        assertThat(passProduct.getTotalHoldDays()).isEqualTo(3);
        assertThat(passProduct.isActive()).isFalse();
    }

    @Test
    void 수정_값이_불변식을_위반하면_예외가_발생하고_기존_값이_유지된다() {
        // given
        PassProduct passProduct = PassProductFixture.기본_수강권(기본_시설());

        // when / then
        assertThatThrownBy(() -> passProduct.update(
                "무제한권", ClassForm.GROUP, null, null, null, 0, true))
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(PassProductErrorCode.NO_EXPIRATION_CONDITION));
        assertThat(passProduct.getName()).isEqualTo(PassProductFixture.기본_이름);
        assertThat(passProduct.getTotalCount()).isEqualTo(PassProductFixture.기본_횟수);
    }

    private Studio 기본_시설() {
        Member owner = StudioFixture.기본_소유자();
        return StudioFixture.기본_시설(owner);
    }
}
