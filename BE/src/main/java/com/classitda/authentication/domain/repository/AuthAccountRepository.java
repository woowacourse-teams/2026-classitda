package com.classitda.authentication.domain.repository;

import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {

    Optional<AuthAccount> findByProviderAndProviderSubject(
            OauthProvider provider,
            String providerSubject
    );

    Optional<AuthAccount> findByMemberIdAndProvider(Long memberId, OauthProvider provider);

    boolean existsByMemberId(Long memberId);

    @Modifying
    @Query("delete from AuthAccount authAccount where authAccount.memberId in :memberIds")
    int deleteByMemberIdIn(@Param("memberIds") Collection<Long> memberIds);

    @Query("select authAccount.memberId from AuthAccount authAccount where authAccount.memberId in :memberIds")
    List<Long> findMemberIdsByMemberIdIn(@Param("memberIds") Collection<Long> memberIds);
}
