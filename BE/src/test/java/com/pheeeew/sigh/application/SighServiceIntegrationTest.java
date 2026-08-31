package com.pheeeew.sigh.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.pheeeew.common.config.JpaAuditingConfig;
import com.pheeeew.sigh.domain.repository.SighRepository;
import com.pheeeew.sigh.exception.SighErrorCode;
import com.pheeeew.sigh.exception.SighException;
import com.pheeeew.sigh.infra.PostgisSighLocationGenerator;
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
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, SighService.class, PostgisSighLocationGenerator.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
class SighServiceIntegrationTest {

    private static final double SEOUL_CITY_HALL_LONGITUDE = 126.9780;
    private static final double SEOUL_CITY_HALL_LATITUDE = 37.5664;
    private static final UUID REJECTED_REQUEST_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:17-3.5")
                    .asCompatibleSubstituteFor("postgres")
    );

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
        assertThat(result.sigh().getId()).isPositive();
        assertThat(result.sigh().getCreatedAt()).isNotNull();
        assertThat(result.sigh().getUpdatedAt()).isNotNull();
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
        assertThat(retried.sigh().getId()).isEqualTo(first.sigh().getId());
        assertThat(retried.sigh().getLongitude()).isEqualTo(first.sigh().getLongitude());
        assertThat(retried.sigh().getLatitude()).isEqualTo(first.sigh().getLatitude());
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
                .extracting(result -> result.sigh().getId())
                .containsOnly(results.getFirst().sigh().getId());
        assertThat(results).filteredOn(SighSaveResult::created).hasSize(1);
        assertThat(sighRepository.count()).isOne();
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

    private void removeRejectedRequestIdConstraint() {
        jdbcClient.sql("""
                        ALTER TABLE sighs
                        DROP CONSTRAINT IF EXISTS ck_sighs_reject_test_request
                        """)
                .update();
    }
}
