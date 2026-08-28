package com.classitda.authentication.application.session;

import java.util.Optional;

public interface RefreshSessionStore {

    enum DeleteOutcome {
        DELETED,
        SESSION_MISMATCH
    }

    enum RotateOutcome {
        ROTATED,
        OLD_SESSION_MISMATCH,
        NEW_SESSION_CONFLICT
    }

    void save(String sessionId, RefreshSession session, long ttlSeconds);

    Optional<RefreshSession> findBySessionId(String sessionId);

    DeleteOutcome deleteIfMatches(String sessionId, RefreshSession expectedSession);

    RotateOutcome rotate(
            String oldSessionId,
            RefreshSession expectedOldSession,
            String newSessionId,
            RefreshSession newSession,
            long ttlSeconds
    );
}
