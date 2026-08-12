package com.classitda.authentication.infra.session;

import com.classitda.authentication.application.session.RefreshSession;
import com.classitda.authentication.application.session.RefreshSessionStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class RedisRefreshSessionStore implements RefreshSessionStore {

    private static final String REFRESH_SESSION_KEY_PREFIX = "auth:refresh:";
    private static final RedisScript<Long> COMPARE_AND_DELETE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/authentication/compare-and-delete-refresh-session.lua"),
            Long.class
    );
    private static final RedisScript<Long> ROTATE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/authentication/rotate-refresh-session.lua"),
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(String sessionId, RefreshSession session, long ttlSeconds) {
        try {
            requirePositiveTtl(ttlSeconds);
            redisTemplate.opsForValue().set(
                    refreshSessionKey(sessionId),
                    serialize(RefreshSessionValue.from(session)),
                    Duration.ofSeconds(ttlSeconds)
            );
        } catch (RuntimeException exception) {
            throw infrastructureFailure();
        }
    }

    @Override
    public Optional<RefreshSession> findBySessionId(String sessionId) {
        try {
            String value = redisTemplate.opsForValue().get(refreshSessionKey(sessionId));
            if (value == null) {
                return Optional.empty();
            }

            return Optional.of(deserialize(value).toSession());
        } catch (RuntimeException exception) {
            throw infrastructureFailure();
        }
    }

    @Override
    public DeleteOutcome deleteIfMatches(String sessionId, RefreshSession expectedSession) {
        try {
            Long result = redisTemplate.execute(
                    COMPARE_AND_DELETE_SCRIPT,
                    List.of(refreshSessionKey(sessionId)),
                    serialize(RefreshSessionValue.from(expectedSession))
            );

            if (Long.valueOf(0L).equals(result)) {
                return DeleteOutcome.DELETED;
            }
            if (Long.valueOf(1L).equals(result)) {
                return DeleteOutcome.SESSION_MISMATCH;
            }
            throw new IllegalStateException("리프레시 세션 삭제 결과가 올바르지 않습니다.");
        } catch (RuntimeException exception) {
            throw infrastructureFailure();
        }
    }

    @Override
    public RotateOutcome rotate(
            String oldSessionId,
            RefreshSession expectedOldSession,
            String newSessionId,
            RefreshSession newSession,
            long ttlSeconds
    ) {
        try {
            requirePositiveTtl(ttlSeconds);
            Long result = redisTemplate.execute(
                    ROTATE_SCRIPT,
                    List.of(refreshSessionKey(oldSessionId), refreshSessionKey(newSessionId)),
                    serialize(RefreshSessionValue.from(expectedOldSession)),
                    serialize(RefreshSessionValue.from(newSession)),
                    String.valueOf(Instant.now().getEpochSecond()),
                    String.valueOf(ttlSeconds)
            );

            if (result == null) {
                throw new IllegalStateException("리프레시 세션 회전 결과가 없습니다.");
            }

            return switch (result.intValue()) {
                case 0 -> RotateOutcome.ROTATED;
                case 1 -> RotateOutcome.OLD_SESSION_MISMATCH;
                case 2 -> RotateOutcome.NEW_SESSION_CONFLICT;
                default -> throw new IllegalStateException("리프레시 세션 회전 결과가 올바르지 않습니다.");
            };
        } catch (RuntimeException exception) {
            throw infrastructureFailure();
        }
    }

    private String refreshSessionKey(String sessionId) {
        return REFRESH_SESSION_KEY_PREFIX + sessionId;
    }

    private String serialize(RefreshSessionValue sessionValue) {
        try {
            return objectMapper.writeValueAsString(sessionValue);
        } catch (JacksonException exception) {
            throw new IllegalStateException("리프레시 세션을 직렬화할 수 없습니다.");
        }
    }

    private RefreshSessionValue deserialize(String value) {
        try {
            JsonNode root = objectMapper.readTree(value);
            if (root == null || !root.isObject()) {
                throw new IllegalStateException("리프레시 세션 형식이 올바르지 않습니다.");
            }

            JsonNode tokenHash = root.get("tokenHash");
            JsonNode memberId = root.get("memberId");
            JsonNode expiresAtEpochSecond = root.get("expiresAtEpochSecond");
            if (tokenHash == null || !tokenHash.isString()
                    || memberId == null || !memberId.isIntegralNumber() || !memberId.canConvertToLong()
                    || expiresAtEpochSecond == null || !expiresAtEpochSecond.isIntegralNumber()
                    || !expiresAtEpochSecond.canConvertToLong()) {
                throw new IllegalStateException("리프레시 세션 필드가 올바르지 않습니다.");
            }

            return new RefreshSessionValue(
                    tokenHash.stringValue(),
                    memberId.longValue(),
                    expiresAtEpochSecond.longValue()
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException("리프레시 세션을 역직렬화할 수 없습니다.");
        }
    }

    private void requirePositiveTtl(long ttlSeconds) {
        if (ttlSeconds < 1L) {
            throw new IllegalArgumentException("리프레시 세션 TTL은 1초 이상이어야 합니다.");
        }
    }

    private IllegalStateException infrastructureFailure() {
        return new IllegalStateException("리프레시 세션 저장소 처리에 실패했습니다.");
    }

    private record RefreshSessionValue(
            String tokenHash,
            Long memberId,
            long expiresAtEpochSecond
    ) {

        private static RefreshSessionValue from(RefreshSession session) {
            return new RefreshSessionValue(
                    session.tokenHash(),
                    session.memberId(),
                    session.expiresAtEpochSecond()
            );
        }

        private RefreshSession toSession() {
            return RefreshSession.of(tokenHash, memberId, expiresAtEpochSecond);
        }
    }
}
