package com.pheeeew.sigh.presentation;

import com.pheeeew.sigh.application.SighSaveResult;
import com.pheeeew.sigh.application.SighService;
import com.pheeeew.sigh.presentation.dto.SighCreateV2Request;
import com.pheeeew.sigh.presentation.dto.SighFeature;
import com.pheeeew.sigh.presentation.dto.SighV2Properties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v2/sighs")
@RestController
public class SighV2Controller implements SighV2ControllerApi {

    private static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");

    private final SighService sighService;

    @Override
    @PostMapping(produces = "application/geo+json")
    public ResponseEntity<SighFeature<SighV2Properties>> save(
            @Valid @RequestBody SighCreateV2Request request
    ) {
        SighSaveResult result = sighService.save(
                request.requestId(),
                request.longitude(),
                request.latitude(),
                request.memo()
        );

        HttpStatus status = HttpStatus.OK;
        if (result.created()) {
            status = HttpStatus.CREATED;
        }

        return ResponseEntity.status(status)
                .contentType(GEO_JSON)
                .body(SighFeature.of(
                        result.id(),
                        result.longitude(),
                        result.latitude(),
                        SighV2Properties.from(result)
                ));
    }
}
