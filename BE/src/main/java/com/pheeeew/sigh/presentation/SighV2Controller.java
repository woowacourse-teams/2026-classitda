package com.pheeeew.sigh.presentation;

import com.pheeeew.sigh.application.dto.SighDetailResult;
import com.pheeeew.sigh.application.dto.SighSaveResult;
import com.pheeeew.sigh.application.SighService;
import com.pheeeew.sigh.presentation.dto.SighCreateV2Request;
import com.pheeeew.sigh.presentation.dto.SighFeature;
import com.pheeeew.sigh.presentation.dto.SighV2Properties;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @GetMapping("/{id}")
    public ResponseEntity<SighFeature<SighV2Properties>> findById(
            @PathVariable Long id
    ) {
        SighDetailResult result = sighService.findById(id);

        return ResponseEntity.ok()
                .contentType(GEO_JSON)
                .body(SighFeature.of(result.id(), result.longitude(), result.latitude(), SighV2Properties.from(result)));
    }

    @Override
    @PostMapping
    public ResponseEntity<SighFeature<SighV2Properties>> save(
            @RequestBody SighCreateV2Request request
    ) {
        SighSaveResult result = sighService.save(
                request.requestId(),
                request.longitude(),
                request.latitude(),
                request.memo()
        );

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.created()) {
            response = ResponseEntity.created(URI.create("/api/v2/sighs/" + result.id()));
        }

        return response
                .contentType(GEO_JSON)
                .body(SighFeature.of(
                        result.id(),
                        result.longitude(),
                        result.latitude(),
                        SighV2Properties.from(result)
                ));
    }
}
