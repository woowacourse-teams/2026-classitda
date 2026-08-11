package com.classitda.studio.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.member.domain.Member;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.fixture.StudioPolicyFixture;
import com.classitda.support.MySqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfigureRestTestClient
@Import(MySqlTestContainerConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
class StudioPolicyConcurrencyTest {

    private static final int CONCURRENT_REQUEST_COUNT = 2;

    @Autowired
    private RestTestClient client;

    @Autowired
    private StudioService studioService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 운영_정책을_동시에_등록하면_하나만_성공하고_나머지는_409를_반환한다() throws Exception {
        // given
        Long ownerId = 소유자를_저장한다();
        Long studioId = studioService.save(ownerId, StudioFixture.기본_시설_생성_요청()).id();
        CyclicBarrier barrier = new CyclicBarrier(CONCURRENT_REQUEST_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUEST_COUNT);

        // when
        List<Future<Integer>> futures = executor.invokeAll(List.<Callable<Integer>>of(
                () -> 정책을_등록한다(barrier, ownerId, studioId),
                () -> 정책을_등록한다(barrier, ownerId, studioId)
        ));
        executor.shutdown();

        // then
        assertThat(List.of(futures.get(0).get(), futures.get(1).get()))
                .containsExactlyInAnyOrder(201, 409);
    }

    private int 정책을_등록한다(CyclicBarrier barrier, Long ownerId, Long studioId) throws Exception {
        barrier.await();
        return client.post()
                .uri("/api/studios/{studioId}/policy", studioId)
                .header("X-API-Version", "1")
                .header("X-Member-Id", String.valueOf(ownerId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioPolicyFixture.기본_정책_생성_요청())
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();
    }

    private Long 소유자를_저장한다() {
        return transactionTemplate.execute(status -> {
            Member owner = StudioFixture.아이디가_다른_소유자("policy-concurrency");
            entityManager.persist(owner);
            entityManager.flush();
            return owner.getId();
        });
    }
}
