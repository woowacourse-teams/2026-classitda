package com.pheeeew.sigh.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.pheeeew.sigh.domain.Sigh;
import com.pheeeew.sigh.domain.repository.SighRepository;
import com.pheeeew.sigh.exception.SighErrorCode;
import com.pheeeew.sigh.exception.SighException;
import com.pheeeew.support.PostgisDataJpaTest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
class SighServiceIntegrationTest {

    private static final double SEOUL_CITY_HALL_LONGITUDE = 126.9780;
    private static final double SEOUL_CITY_HALL_LATITUDE = 37.5664;
    private static final UUID REJECTED_REQUEST_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private SighService sighService;

    @Autowired
    private SighRepository sighRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @AfterEach
    void tearDown() {
        sighRepository.deleteAll();
    }

    @Test
    void 새로운_requestId로_한숨을_저장한다() {
        // given
        UUID requestId = UUID.randomUUID();

        // when
        SighSaveResult result = sighService.save(
                requestId,
                SEOUL_CITY_HALL_LONGITUDE,
                SEOUL_CITY_HALL_LATITUDE
        );

        // then
        assertThat(result.created()).isTrue();
        assertThat(result.sigh().id()).isPositive();
        assertThat(result.sigh().createdAt()).isNotNull();

        Sigh saved = sighRepository.findById(result.sigh().id()).orElseThrow();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(sighRepository.count()).isOne();
    }

    @Test
    void 같은_requestId는_다른_중심으로_재시도해도_기존_한숨을_반환한다() {
        // given
        UUID requestId = UUID.randomUUID();
        SighSaveResult first = sighService.save(
                requestId,
                SEOUL_CITY_HALL_LONGITUDE,
                SEOUL_CITY_HALL_LATITUDE
        );

        // when
        SighSaveResult retried = sighService.save(requestId, 129.0756, 35.1796);

        // then
        assertThat(retried.created()).isFalse();
        assertThat(retried.sigh().id()).isEqualTo(first.sigh().id());
        assertThat(retried.sigh().longitude()).isEqualTo(first.sigh().longitude());
        assertThat(retried.sigh().latitude()).isEqualTo(first.sigh().latitude());
        assertThat(sighRepository.count()).isOne();
    }

    @Test
    void 같은_requestId가_동시에_요청되어도_한_건만_저장한다() throws Exception {
        // given
        int requestCount = 6;
        UUID requestId = UUID.randomUUID();
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        // when
        List<SighSaveResult> results = executeConcurrently(requestCount, requestId, ready, start);

        // then
        assertThat(results)
                .extracting(result -> result.sigh().id())
                .containsOnly(results.getFirst().sigh().id());
        assertThat(results).filteredOn(SighSaveResult::created).hasSize(1);
        assertThat(sighRepository.count()).isOne();
    }

    @Test
    void 지도_영역_안과_경계의_한숨만_최신순으로_조회한다() {
        // given
        Long insideId = insertSigh(126.9780, 37.5664, "2026-08-31T10:30:00Z");
        insertSigh(127.2000, 37.5664, "2026-08-31T10:31:00Z");
        Long boundaryId = insertSigh(127.1000, 37.6000, "2026-08-31T10:32:00Z");

        // when
        SighMapResult result = sighService.findAllWithinBounds(126.9000, 37.5000, 127.1000, 37.6000);

        // then
        assertThat(result.truncated()).isFalse();
        assertThat(result.sighs())
                .extracting(SighMapItem::id)
                .containsExactly(boundaryId, insideId);
        assertThat(result.sighs().getFirst().longitude()).isEqualTo(127.1000);
        assertThat(result.sighs().getFirst().latitude()).isEqualTo(37.6000);
        assertThat(result.sighs().getFirst().createdAt())
                .isEqualTo(Instant.parse("2026-08-31T10:32:00Z"));
    }

    @Test
    void 지도_영역의_한숨이_500건이면_모두_반환하고_잘리지_않았음을_알린다() {
        // given
        insertSighs(500, 126.9780, 37.5664);

        // when
        SighMapResult result = sighService.findAllWithinBounds(126.9000, 37.5000, 127.1000, 37.6000);

        // then
        assertThat(result.truncated()).isFalse();
        assertThat(result.sighs()).hasSize(500);
    }

    @Test
    void 지도_영역의_한숨이_500건을_초과하면_최신_500건과_잘림_여부를_반환한다() {
        // given
        insertSighs(501, 126.9780, 37.5664);
        Long oldestId = jdbcClient.sql("SELECT MIN(id) FROM sighs")
                .query(Long.class)
                .single();

        // when
        SighMapResult result = sighService.findAllWithinBounds(126.9000, 37.5000, 127.1000, 37.6000);

        // then
        assertThat(result.truncated()).isTrue();
        assertThat(result.sighs()).hasSize(500);
        assertThat(result.sighs())
                .extracting(SighMapItem::id)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .doesNotContain(oldestId);
    }

    @Test
    void 저장_무결성_오류는_한숨_도메인_예외로_변환한다() {
        // given
        addRejectedRequestIdConstraint();

        try {
            // when
            Throwable throwable = catchThrowable(() -> sighService.save(
                    REJECTED_REQUEST_ID,
                    SEOUL_CITY_HALL_LONGITUDE,
                    SEOUL_CITY_HALL_LATITUDE
            ));

            // then
            assertThat(throwable)
                    .isInstanceOf(SighException.class)
                    .hasMessage("한숨을 저장하지 못했습니다.")
                    .hasCauseInstanceOf(DataIntegrityViolationException.class);
            assertThat(((SighException) throwable).getErrorCode())
                    .isEqualTo(SighErrorCode.SIGH_SAVE_FAILED);
        } finally {
            removeRejectedRequestIdConstraint();
        }
    }

    private List<SighSaveResult> executeConcurrently(
            int requestCount,
            UUID requestId,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {
        try (ExecutorService executorService = Executors.newFixedThreadPool(requestCount)) {
            List<Future<SighSaveResult>> futures = new ArrayList<>();
            for (int index = 0; index < requestCount; index++) {
                futures.add(executorService.submit(() -> {
                    ready.countDown();
                    start.await();
                    return sighService.save(
                            requestId,
                            SEOUL_CITY_HALL_LONGITUDE,
                            SEOUL_CITY_HALL_LATITUDE
                    );
                }));
            }

            boolean allRequestsReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertThat(allRequestsReady).isTrue();

            List<SighSaveResult> results = new ArrayList<>();
            for (Future<SighSaveResult> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        }
    }

    private void addRejectedRequestIdConstraint() {
        jdbcClient.sql("""
                        ALTER TABLE sighs
                        ADD CONSTRAINT ck_sighs_reject_test_request
                        CHECK (request_id <> '00000000-0000-0000-0000-000000000001'::uuid)
                        """)
                .update();
    }

    private Long insertSigh(double longitude, double latitude, String createdAt) {
        return jdbcClient.sql("""
                        INSERT INTO sighs (request_id, location, created_at, updated_at)
                        VALUES (
                            :requestId,
                            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326),
                            CAST(:createdAt AS TIMESTAMPTZ),
                            CAST(:createdAt AS TIMESTAMPTZ)
                        )
                        RETURNING id
                        """)
                .param("requestId", UUID.randomUUID())
                .param("longitude", longitude)
                .param("latitude", latitude)
                .param("createdAt", createdAt)
                .query(Long.class)
                .single();
    }

    private void insertSighs(int count, double longitude, double latitude) {
        jdbcClient.sql("""
                        INSERT INTO sighs (request_id, location, created_at, updated_at)
                        SELECT
                            (
                                '00000000-0000-0000-0000-'
                                || LPAD(sequence::text, 12, '0')
                            )::uuid,
                            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326),
                            TIMESTAMPTZ '2026-08-31T10:30:00Z'
                                + sequence * INTERVAL '1 microsecond',
                            TIMESTAMPTZ '2026-08-31T10:30:00Z'
                                + sequence * INTERVAL '1 microsecond'
                        FROM generate_series(1, :count) AS sequence
                        """)
                .param("longitude", longitude)
                .param("latitude", latitude)
                .param("count", count)
                .update();
    }

    private void removeRejectedRequestIdConstraint() {
        jdbcClient.sql("""
                        ALTER TABLE sighs
                        DROP CONSTRAINT IF EXISTS ck_sighs_reject_test_request
                        """)
                .update();
    }
}
