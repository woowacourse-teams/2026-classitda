package com.classitda.passproduct.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.passproduct.application.PassProductService;
import com.classitda.passproduct.presentation.dto.PassProductCreateRequest;
import com.classitda.passproduct.presentation.dto.PassProductResponse;
import com.classitda.passproduct.presentation.dto.PassProductUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/pass-products")
@RestController
public class PassProductController implements PassProductControllerApi {

    private final PassProductService passProductService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<PassProductResponse> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody PassProductCreateRequest request
    ) {
        PassProductResponse response = passProductService.save(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping(version = "1")
    public List<PassProductResponse> findAll(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId
    ) {
        return passProductService.findAll(memberId, studioId);
    }

    @Override
    @PutMapping(path = "/{passProductId}", version = "1")
    public PassProductResponse update(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long passProductId,
            @Valid @RequestBody PassProductUpdateRequest request
    ) {
        return passProductService.update(memberId, studioId, passProductId, request);
    }
}
