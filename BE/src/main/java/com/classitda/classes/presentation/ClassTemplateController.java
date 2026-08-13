package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ClassTemplateCommandService;
import com.classitda.classes.application.ClassTemplateQueryService;
import com.classitda.classes.presentation.dto.ClassTemplateCreateRequest;
import com.classitda.classes.presentation.dto.ClassTemplateResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/class-templates")
@RestController
public class ClassTemplateController implements ClassTemplateControllerApi {

    private final ClassTemplateCommandService classTemplateCommandService;
    private final ClassTemplateQueryService classTemplateQueryService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<Void> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody ClassTemplateCreateRequest request
    ) {
        classTemplateCommandService.save(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping(version = "1")
    public List<ClassTemplateResponse> findAll(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId
    ) {
        return classTemplateQueryService.findAll(memberId, studioId);
    }
}
