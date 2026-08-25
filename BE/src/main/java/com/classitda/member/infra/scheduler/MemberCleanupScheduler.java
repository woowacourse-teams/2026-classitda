package com.classitda.member.infra.scheduler;

import com.classitda.member.application.MemberCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberCleanupScheduler {

    private static final int BATCH_SIZE = 100;

    private final MemberCleanupService memberCleanupService;

    @Scheduled(cron = "0 10 * * * *", zone = "Asia/Seoul")
    public void cleanupDueMembers() {
        int totalProcessedCount = 0;
        int processedCount;
        do {
            processedCount = memberCleanupService.cleanupDueMembers(BATCH_SIZE);
            totalProcessedCount += processedCount;
        } while (processedCount == BATCH_SIZE);

        if (totalProcessedCount > 0) {
            log.info("탈퇴 회원 개인정보 정리를 완료했습니다. processedCount={}", totalProcessedCount);
        }
    }
}
