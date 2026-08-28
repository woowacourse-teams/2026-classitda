package com.classitda.member.application;

import com.classitda.member.domain.Term;
import com.classitda.member.domain.repository.TermRepository;
import com.classitda.member.presentation.dto.TermResponse;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TermService {

    private final TermRepository termRepository;

    public List<TermResponse> findAll() {
        return termRepository.findAll().stream()
                .sorted(Comparator.comparing(Term::getCode))
                .map(TermResponse::from)
                .toList();
    }
}
