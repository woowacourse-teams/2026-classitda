package com.classitda.member.domain.repository;

import com.classitda.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByIdAndWithdrawalRequestedAtIsNull(Long memberId);

    Optional<Member> findByPhoneNumber(String phoneNumber);
}
