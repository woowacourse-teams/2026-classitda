package com.classitda.authentication.application.session;

import java.util.Optional;

public interface SignupSessionStore {

    void save(String signupJti, SignupSession session);

    boolean hasActiveSession(String signupJti);

    Optional<SignupSession> findBySignupJti(String signupJti);

    void deleteBySignupJti(String signupJti);
}
