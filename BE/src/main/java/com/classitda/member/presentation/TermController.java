package com.classitda.member.presentation;

import com.classitda.member.application.TermService;
import com.classitda.member.presentation.dto.TermResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/terms")
@RestController
public class TermController implements TermControllerApi {

    private final TermService termService;

    @Override
    @GetMapping(value = "", version = "1")
    public List<TermResponse> findAll() {
        return termService.findAll();
    }
}
