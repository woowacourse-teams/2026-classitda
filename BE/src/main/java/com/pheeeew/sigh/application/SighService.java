package com.pheeeew.sigh.application;

import static com.pheeeew.sigh.exception.SighErrorCode.SIGH_SAVE_FAILED;

import com.pheeeew.sigh.domain.Sigh;
import com.pheeeew.sigh.domain.repository.SighRepository;
import com.pheeeew.sigh.domain.repository.projection.SighMapProjection;
import com.pheeeew.sigh.exception.SighException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SighService {

    private static final int MAX_FIND_COUNT = 500;

    private final SighRepository sighRepository;
    private final SighLocationGenerator sighLocationGenerator;

    public SighSaveResult save(UUID requestId, double longitude, double latitude) {
        return sighRepository.findByRequestId(requestId)
                .map(sigh -> SighSaveResult.of(sigh, false))
                .orElseGet(() -> saveNewSigh(requestId, longitude, latitude));
    }

    public SighMapResult findAllWithinBounds(
            double minLongitude,
            double minLatitude,
            double maxLongitude,
            double maxLatitude
    ) {
        List<SighMapProjection> projections = sighRepository.findAllWithinBounds(
                minLongitude,
                minLatitude,
                maxLongitude,
                maxLatitude,
                MAX_FIND_COUNT + 1
        );

        boolean truncated = projections.size() > MAX_FIND_COUNT;
        if (truncated) {
            projections = projections.subList(0, MAX_FIND_COUNT);
        }

        List<SighMapItem> sighs = projections.stream()
                .map(projection -> SighMapItem.of(
                        projection.getId(),
                        projection.getLongitude(),
                        projection.getLatitude(),
                        projection.getCreatedAt()
                ))
                .toList();

        return SighMapResult.of(sighs, truncated);
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
