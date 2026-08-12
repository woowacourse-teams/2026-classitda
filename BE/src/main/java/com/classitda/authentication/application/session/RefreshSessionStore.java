package com.classitda.authentication.application.session;

import java.util.Optional;

public interface RefreshSessionStore {

    enum RotateOutcome {
        ROTATED,
        OLD_SESSION_MISMATCH,
        NEW_SESSION_CONFLICT
    }

    void save(String sessionId, RefreshSession session, long ttlSeconds);

    Optional<RefreshSession> findBySessionId(String sessionId);

    RotateOutcome rotate(
            String oldSessionId,
            RefreshSession expectedOldSession,
            String newSessionId,
            RefreshSession newSession,
            long ttlSeconds
    );
}
