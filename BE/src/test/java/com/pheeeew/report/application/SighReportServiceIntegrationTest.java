package com.pheeeew.report.application;

import static com.pheeeew.report.fixture.SighReportFixture.기본_신고_사유;
import static com.pheeeew.report.fixture.SighReportFixture.다른_신고자_기기_식별자;
import static com.pheeeew.report.fixture.SighReportFixture.신고자_기기_식별자;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.pheeeew.report.domain.SighReport;
import com.pheeeew.report.domain.repository.SighReportRepository;
import com.pheeeew.report.exception.SighReportErrorCode;
import com.pheeeew.report.exception.SighReportException;
import com.pheeeew.sigh.domain.repository.SighRepository;
import com.pheeeew.sigh.exception.SighErrorCode;
import com.pheeeew.sigh.exception.SighException;
import com.pheeeew.support.PostgisDataJpaTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@PostgisDataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SighReportServiceIntegrationTest {

    private static final double SEOUL_CITY_HALL_LONGITUDE = 126.9780;
    private static final double SEOUL_CITY_HALL_LATITUDE = 37.5664;
    private static final Long NOT_EXISTING_SIGH_ID = Long.MAX_VALUE;
    private static final String REJECTED_REASON = "저장이 거부되는 사유";

    @Autowired
    private SighReportService sighReportService;

    @Autowired
    private SighReportRepository sighReportRepository;

    @Autowired
    private SighRepository sighRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void 삭제된_한숨의_신고_기록은_그대로_남는다() {
        // given
        Long sighId = insertSigh();
        sighReportService.save(sighId, 신고자_기기_식별자(), 기본_신고_사유());

        // when
        jdbcClient.sql("UPDATE sighs SET deleted_at = NOW() WHERE id = :id")
                .param("id", sighId)
                .update();

        // then
        assertThat(sighReportRepository.findBySighIdAndReporterDeviceId(sighId, 신고자_기기_식별자()))
                .isPresent();
        assertThat(sighReportRepository.count()).isOne();
    }

    @AfterEach
    void tearDown() {
        sighReportRepository.deleteAll();
        sighRepository.deleteAll();
    }

    @Test
    void 처음_신고하는_기기의_신고를_저장한다() {
        // given
        Long sighId = insertSigh();

        // when
        SighReportResult result = sighReportService.save(sighId, 신고자_기기_식별자(), "  광고성 게시물입니다  ");

        // then
        assertThat(result.created()).isTrue();
        assertThat(result.id()).isPositive();
        assertThat(result.sighId()).isEqualTo(sighId);
        assertThat(result.reason()).isEqualTo("광고성 게시물입니다");
        assertThat(result.createdAt()).isNotNull();

        SighReport saved = sighReportRepository.findById(result.id()).orElseThrow();
        assertThat(saved.getReporterDeviceId()).isEqualTo(신고자_기기_식별자());
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(sighReportRepository.count()).isOne();
    }

    @Test
    void 같은_기기가_같은_한숨을_다시_신고하면_최초_신고를_반환한다() {
        // given
        Long sighId = insertSigh();
        SighReportResult first = sighReportService.save(sighId, 신고자_기기_식별자(), 기본_신고_사유());

        // when
        SighReportResult retried = sighReportService.save(sighId, 신고자_기기_식별자(), "나중에 바꾼 사유입니다");

        // then
        assertThat(retried.created()).isFalse();
        assertThat(retried.id()).isEqualTo(first.id());
        assertThat(retried.reason()).isEqualTo(기본_신고_사유());
        assertThat(sighReportRepository.count()).isOne();
    }

    @Test
    void 다른_기기가_같은_한숨을_신고하면_신고를_따로_저장한다() {
        // given
        Long sighId = insertSigh();
        SighReportResult first = sighReportService.save(sighId, 신고자_기기_식별자(), 기본_신고_사유());

        // when
        SighReportResult second = sighReportService.save(sighId, 다른_신고자_기기_식별자(), 기본_신고_사유());

        // then
        assertThat(second.created()).isTrue();
        assertThat(second.id()).isNotEqualTo(first.id());
        assertThat(sighReportRepository.count()).isEqualTo(2);
    }

    @Test
    void 같은_기기가_다른_한숨을_신고하면_신고를_따로_저장한다() {
        // given
        Long sighId = insertSigh();
        Long otherSighId = insertSigh();
        SighReportResult first = sighReportService.save(sighId, 신고자_기기_식별자(), 기본_신고_사유());

        // when
        SighReportResult second = sighReportService.save(otherSighId, 신고자_기기_식별자(), 기본_신고_사유());

        // then
        assertThat(second.created()).isTrue();
        assertThat(second.id()).isNotEqualTo(first.id());
        assertThat(sighReportRepository.count()).isEqualTo(2);
    }

    @Test
    void 같은_기기가_같은_한숨을_동시에_신고해도_한_건만_저장한다() throws Exception {
        // given
        int requestCount = 6;
        Long sighId = insertSigh();
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        // when
        List<SighReportResult> results = executeConcurrently(requestCount, sighId, ready, start);

        // then
        assertThat(results)
                .extracting(SighReportResult::id)
                .containsOnly(results.getFirst().id());
        assertThat(results).filteredOn(SighReportResult::created).hasSize(1);
        assertThat(sighReportRepository.count()).isOne();
    }

    @Test
    void 존재하지_않는_한숨을_신고하면_한숨_없음_예외가_발생한다() {
        // given / when
        Throwable throwable = catchThrowable(
                () -> sighReportService.save(NOT_EXISTING_SIGH_ID, 신고자_기기_식별자(), 기본_신고_사유())
        );

        // then
        assertThat(throwable)
                .isInstanceOf(SighException.class)
                .hasMessage("한숨을 찾을 수 없습니다.");
        assertThat(((SighException) throwable).getErrorCode())
                .isEqualTo(SighErrorCode.SIGH_NOT_FOUND);
        assertThat(sighReportRepository.count()).isZero();
    }

    @Test
    void 저장_무결성_오류는_신고_도메인_예외로_변환한다() {
        // given
        Long sighId = insertSigh();
        addRejectedReasonConstraint();

        try {
            // when
            Throwable throwable = catchThrowable(
                    () -> sighReportService.save(sighId, 신고자_기기_식별자(), REJECTED_REASON)
            );

            // then
            assertThat(throwable)
                    .isInstanceOf(SighReportException.class)
                    .hasMessage("신고를 저장하지 못했습니다.")
                    .hasCauseInstanceOf(DataIntegrityViolationException.class);
            assertThat(((SighReportException) throwable).getErrorCode())
                    .isEqualTo(SighReportErrorCode.SIGH_REPORT_SAVE_FAILED);
        } finally {
            removeRejectedReasonConstraint();
        }
    }

    private Long insertSigh() {
        return jdbcClient.sql("""
                        INSERT INTO sighs (request_id, location, nickname, created_at, updated_at)
                        VALUES (
                            :requestId,
                            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326),
                            '외로운 회사원',
                            NOW(),
                            NOW()
                        )
                        RETURNING id
                        """)
                .param("requestId", UUID.randomUUID())
                .param("longitude", SEOUL_CITY_HALL_LONGITUDE)
                .param("latitude", SEOUL_CITY_HALL_LATITUDE)
                .query(Long.class)
                .single();
    }

    private List<SighReportResult> executeConcurrently(
            int requestCount,
            Long sighId,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {
        try (ExecutorService executorService = Executors.newFixedThreadPool(requestCount)) {
            List<Future<SighReportResult>> futures = new ArrayList<>();
            for (int index = 0; index < requestCount; index++) {
                futures.add(executorService.submit(() -> {
                    ready.countDown();
                    start.await();
                    return sighReportService.save(sighId, 신고자_기기_식별자(), 기본_신고_사유());
                }));
            }

            boolean allRequestsReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertThat(allRequestsReady).isTrue();

            List<SighReportResult> results = new ArrayList<>();
            for (Future<SighReportResult> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        }
    }

    private void addRejectedReasonConstraint() {
        jdbcClient.sql("""
                        ALTER TABLE sigh_reports
                        ADD CONSTRAINT ck_sigh_reports_reject_test_reason
                        CHECK (reason <> '저장이 거부되는 사유')
                        """)
                .update();
    }

    private void removeRejectedReasonConstraint() {
        jdbcClient.sql("""
                        ALTER TABLE sigh_reports
                        DROP CONSTRAINT IF EXISTS ck_sigh_reports_reject_test_reason
                        """)
                .update();
    }
}
