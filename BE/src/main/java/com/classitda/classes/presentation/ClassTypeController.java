package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ClassTypeService;
import com.classitda.classes.presentation.dto.ClassTypeCreateRequest;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/class-types")
@RestController
public class ClassTypeController implements ClassTypeControllerApi {

    private final ClassTypeService classTypeService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<ClassTypeResponse> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody ClassTypeCreateRequest request
    ) {
        ClassTypeResponse response = classTypeService.save(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
