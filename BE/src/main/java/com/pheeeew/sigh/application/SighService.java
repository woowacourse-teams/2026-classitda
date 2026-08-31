package com.pheeeew.sigh.application;

import static com.pheeeew.sigh.exception.SighErrorCode.SIGH_SAVE_FAILED;

import com.pheeeew.sigh.domain.Sigh;
import com.pheeeew.sigh.domain.repository.SighRepository;
import com.pheeeew.sigh.exception.SighException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SighService {

    private final SighRepository sighRepository;
    private final SighLocationGenerator sighLocationGenerator;

    public SighSaveResult save(UUID requestId, double longitude, double latitude) {
        return sighRepository.findByRequestId(requestId)
                .map(sigh -> SighSaveResult.of(sigh, false))
                .orElseGet(() -> saveNewSigh(requestId, longitude, latitude));
    }

    private SighSaveResult saveNewSigh(UUID requestId, double longitude, double latitude) {
        Point location = sighLocationGenerator.generate(longitude, latitude);
        Sigh sigh = Sigh.builder()
                .requestId(requestId)
                .location(location)
                .build();

        try {
            return SighSaveResult.of(sighRepository.saveAndFlush(sigh), true);
        } catch (DataIntegrityViolationException exception) {
            return findExistingSigh(requestId, exception);
        }
    }

    private SighSaveResult findExistingSigh(UUID requestId, DataIntegrityViolationException exception) {
        return sighRepository.findByRequestId(requestId)
                .map(sigh -> SighSaveResult.of(sigh, false))
                .orElseThrow(() -> new SighException(SIGH_SAVE_FAILED, exception));
    }
}
