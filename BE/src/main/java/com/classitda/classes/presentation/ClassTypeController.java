package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ClassTypeService;
import com.classitda.classes.presentation.dto.ClassTypeCreateRequest;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.classes.presentation.dto.ClassTypeUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    @GetMapping(version = "1")
    public List<ClassTypeResponse> findAll(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId
    ) {
        return classTypeService.findAll(memberId, studioId);
    }

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<Void> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody ClassTypeCreateRequest request
    ) {
        classTypeService.save(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PatchMapping(path = "/{classTypeId}", version = "1")
    public ClassTypeResponse update(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classTypeId,
            @Valid @RequestBody ClassTypeUpdateRequest request
    ) {
        return classTypeService.update(memberId, studioId, classTypeId, request);
    }

    @Override
    @DeleteMapping(path = "/{classTypeId}", version = "1")
    public ResponseEntity<Void> delete(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classTypeId
    ) {
        classTypeService.delete(memberId, studioId, classTypeId);
        return ResponseEntity.noContent().build();
    }
}
