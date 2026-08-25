package com.classitda.member.infra.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.classitda.member.application.MemberCleanupService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

@ExtendWith(MockitoExtension.class)
class MemberCleanupSchedulerTest {

    private static final int BATCH_SIZE = 100;

    @Mock
    private MemberCleanupService memberCleanupService;

    private MemberCleanupScheduler memberCleanupScheduler;

    @BeforeEach
    void setUp() {
        memberCleanupScheduler = new MemberCleanupScheduler(memberCleanupService);
    }

    @Test
    void 배치가_가득_찬_동안_반복하고_남은_대상까지_정리한다() {
        // given
        given(memberCleanupService.cleanupDueMembers(BATCH_SIZE))
                .willReturn(BATCH_SIZE, BATCH_SIZE, 23);

        // when
        memberCleanupScheduler.cleanupDueMembers();

        // then
        verify(memberCleanupService, times(3)).cleanupDueMembers(BATCH_SIZE);
    }

    @Test
    void 정리_대상이_없으면_한_번만_조회하고_종료한다() {
        // given
        given(memberCleanupService.cleanupDueMembers(BATCH_SIZE)).willReturn(0);

        // when
        memberCleanupScheduler.cleanupDueMembers();

        // then
        verify(memberCleanupService).cleanupDueMembers(BATCH_SIZE);
    }

    @Test
    void 매시간_서울_시간대에_실행하도록_설정한다() throws NoSuchMethodException {
        // given
        Method cleanupMethod = MemberCleanupScheduler.class.getMethod("cleanupDueMembers");

        // when
        Scheduled scheduled = cleanupMethod.getAnnotation(Scheduled.class);

        // then
        assertThat(scheduled.cron()).isEqualTo("0 10 * * * *");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
    }
}
