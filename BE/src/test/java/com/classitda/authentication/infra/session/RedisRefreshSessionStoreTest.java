package com.classitda.authentication.infra.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.classitda.authentication.application.session.RefreshSession;
import com.classitda.authentication.application.session.RefreshSessionStore;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RedisRefreshSessionStoreTest {

    private static final String SESSION_ID = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final RefreshSession EXPECTED_SESSION = RefreshSession.of(
            "a".repeat(64),
            987_654_321L,
            2_000_000_000L
    );

    @Mock
    private StringRedisTemplate redisTemplate;

    private RedisRefreshSessionStore refreshSessionStore;

    @BeforeEach
    void setUp() {
        refreshSessionStore = new RedisRefreshSessionStore(redisTemplate, new ObjectMapper());
    }

    @ParameterizedTest
    @MethodSource("documentedResults")
    void 정확한_문서화_결과만_논리적_삭제_결과로_변환한다(
            Long scriptResult,
            RefreshSessionStore.DeleteOutcome expectedOutcome
    ) {
        // given
        given(redisTemplate.execute(
                any(),
                anyList(),
                any()
        )).willReturn(scriptResult);

        // when
        RefreshSessionStore.DeleteOutcome outcome = refreshSessionStore.deleteIfMatches(
                SESSION_ID,
                EXPECTED_SESSION
        );

        // then
        assertThat(outcome).isEqualTo(expectedOutcome);
    }

    @ParameterizedTest
    @MethodSource("undocumentedResults")
    void null과_문서화되지_않은_결과는_고정된_원인없는_인프라_예외로_변환한다(Long scriptResult) {
        // given
        given(redisTemplate.execute(
                any(),
                anyList(),
                any()
        )).willReturn(scriptResult);

        // when / then
        assertThatThrownBy(() -> refreshSessionStore.deleteIfMatches(SESSION_ID, EXPECTED_SESSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("리프레시 세션 저장소 처리에 실패했습니다.")
                .hasNoCause();
    }

    private static Stream<Arguments> documentedResults() {
        return Stream.of(
                Arguments.of(0L, RefreshSessionStore.DeleteOutcome.DELETED),
                Arguments.of(1L, RefreshSessionStore.DeleteOutcome.SESSION_MISMATCH)
        );
    }

    private static Stream<Long> undocumentedResults() {
        return Stream.of(
                null,
                -1L,
                2L,
                4_294_967_296L,
                4_294_967_297L,
                Long.MIN_VALUE,
                Long.MAX_VALUE
        );
    }
}
