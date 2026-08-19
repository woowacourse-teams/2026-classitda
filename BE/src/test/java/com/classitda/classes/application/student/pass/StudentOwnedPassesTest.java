package com.classitda.classes.application.student.pass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.classitda.classes.domain.ClassForm;
import com.classitda.passproduct.domain.repository.projection.MemberPassProductClassTypeProjection;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StudentOwnedPassesTest {

    @Test
    void 이용_기간_안이면_조회_날짜와_수업_종류가_맞는_수강권을_포함한다() {
        StudentOwnedPasses ownedPasses = ownedPasses(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                1L
        );

        assertThat(ownedPasses.coveredClassTypeIdsOn(LocalDate.of(2026, 8, 19)))
                .containsExactly(1L);
        assertThat(ownedPasses.covers(ClassForm.GROUP, 1L, LocalDate.of(2026, 8, 19))).isTrue();
        assertThat(ownedPasses.covers(ClassForm.INDIVIDUAL, 1L, LocalDate.of(2026, 8, 19))).isFalse();
        assertThat(ownedPasses.covers(ClassForm.GROUP, 2L, LocalDate.of(2026, 8, 19))).isFalse();
    }

    @Test
    void 이용_기간_밖의_날짜는_포함하지_않는다() {
        StudentOwnedPasses ownedPasses = ownedPasses(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                1L
        );

        assertThat(ownedPasses.covers(ClassForm.GROUP, 1L, LocalDate.of(2026, 7, 31))).isFalse();
        assertThat(ownedPasses.covers(ClassForm.GROUP, 1L, LocalDate.of(2026, 9, 1))).isFalse();
    }

    private StudentOwnedPasses ownedPasses(
            LocalDate startedAt,
            LocalDate expiresAt,
            Long classTypeId
    ) {
        MemberPassProductClassTypeProjection passClassType = mock(MemberPassProductClassTypeProjection.class);
        given(passClassType.getMemberPassProductId()).willReturn(1L);
        given(passClassType.getClassForm()).willReturn(ClassForm.GROUP);
        given(passClassType.getClassTypeId()).willReturn(classTypeId);
        given(passClassType.getStartedAt()).willReturn(startedAt);
        given(passClassType.getExpiresAt()).willReturn(expiresAt);

        return StudentOwnedPasses.from(List.of(passClassType));
    }
}
