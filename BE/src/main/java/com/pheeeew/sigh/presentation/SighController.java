package com.pheeeew.sigh.presentation;

import com.pheeeew.sigh.application.SighMapResult;
import com.pheeeew.sigh.application.SighSaveResult;
import com.pheeeew.sigh.application.SighService;
import com.pheeeew.sigh.presentation.dto.SighCreateRequest;
import com.pheeeew.sigh.presentation.dto.SighMapRequest;
import com.pheeeew.sigh.presentation.dto.SighMapResponse;
import com.pheeeew.sigh.presentation.dto.SighResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/sighs")
@RestController
public class SighController implements SighControllerApi {

    private static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");

    private final SighService sighService;

    @Override
    @GetMapping
    public ResponseEntity<SighMapResponse> findAllWithinBounds(
            @Valid @ModelAttribute SighMapRequest request
    ) {
        SighMapResult result = sighService.findAllWithinBounds(
                request.minLongitude(),
                request.minLatitude(),
                request.maxLongitude(),
                request.maxLatitude()
        );

        return ResponseEntity.ok()
                .contentType(GEO_JSON)
                .body(SighMapResponse.from(result));
    }

    @Override
    @PostMapping
    public ResponseEntity<SighResponse> save(
            @Valid @RequestBody SighCreateRequest request
    ) {
        SighSaveResult result = sighService.save(request.requestId(), request.longitude(), request.latitude());

        HttpStatus status = HttpStatus.OK;
        if (result.created()) {
            status = HttpStatus.CREATED;
        }

        return ResponseEntity.status(status)
                .contentType(GEO_JSON)
                .body(SighResponse.from(result.sigh()));
    }
}
