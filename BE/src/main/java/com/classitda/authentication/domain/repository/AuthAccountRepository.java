package com.classitda.authentication.domain.repository;

import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {

    Optional<AuthAccount> findByProviderAndProviderSubject(
            OauthProvider provider,
            String providerSubject
    );
}
