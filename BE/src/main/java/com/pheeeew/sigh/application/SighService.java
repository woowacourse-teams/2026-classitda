package com.pheeeew.sigh.application;

import static com.pheeeew.sigh.exception.SighErrorCode.SIGH_SAVE_FAILED;
import static com.pheeeew.sigh.exception.SighErrorCode.SIGH_NOT_FOUND;

import com.pheeeew.sigh.domain.Sigh;
import com.pheeeew.sigh.domain.repository.SighRepository;
import com.pheeeew.sigh.domain.repository.projection.SighMapProjection;
import com.pheeeew.sigh.exception.SighException;
import java.util.List;
import java.util.Optional;
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
    private final SighNicknameGenerator sighNicknameGenerator;

    public SighSaveResult save(UUID requestId, double longitude, double latitude) {
        return save(requestId, longitude, latitude, null);
    }

    public SighSaveResult save(UUID requestId, double longitude, double latitude, String memo) {
        Optional<Sigh> existingSigh = sighRepository.findByRequestId(requestId);

        if (existingSigh.isPresent()) {
            return createSaveResult(existingSigh.get(), false);
        }

        return saveNewSigh(requestId, longitude, latitude, memo);
    }

    public SighDetailResult findById(Long id) {
        return sighRepository.findByIdAndDeletedAtIsNull(id)
                .map(SighDetailResult::from)
                .orElseThrow(() -> new SighException(SIGH_NOT_FOUND));
    }

    public SighMapResult findAllWithinBounds(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude) {
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

    private SighSaveResult createSaveResult(Sigh sigh, boolean created) {
        return SighSaveResult.of(sigh, created);
    }

    private SighSaveResult saveNewSigh(UUID requestId, double longitude, double latitude, String memo) {
        Point location = sighLocationGenerator.generate(longitude, latitude);
        Sigh sigh = Sigh.builder()
                .requestId(requestId)
                .location(location)
                .memo(memo)
                .nickname(sighNicknameGenerator.generate())
                .build();

        try {
            return createSaveResult(sighRepository.saveAndFlush(sigh), true);
        } catch (DataIntegrityViolationException cause) {
            return findExistingSigh(requestId, cause);
        }
    }

    private SighSaveResult findExistingSigh(UUID requestId, DataIntegrityViolationException cause) {
        return sighRepository.findByRequestId(requestId)
                .map(sigh -> createSaveResult(sigh, false))
                .orElseThrow(() -> new SighException(SIGH_SAVE_FAILED, cause));
    }
}
