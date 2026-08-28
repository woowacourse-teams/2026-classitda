package com.classitda.member.domain;

import com.classitda.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_term_agreement")
@Entity
public class MemberTermAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(nullable = false)
    private boolean agreed;

    @Builder
    private MemberTermAgreement(
            Member member,
            Term term,
            boolean agreed
    ) {
        validateMember(member);
        validateTerm(term);
        validateAgreed(agreed);
        this.member = member;
        this.term = term;
        this.agreed = agreed;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("약관 동의 회원은 필수입니다.");
        }
    }

    private void validateTerm(Term term) {
        if (term == null) {
            throw new IllegalArgumentException("동의 약관은 필수입니다.");
        }
    }

    private void validateAgreed(boolean agreed) {
        if (!agreed) {
            throw new IllegalArgumentException("동의한 약관만 저장할 수 있습니다.");
        }
    }
}
