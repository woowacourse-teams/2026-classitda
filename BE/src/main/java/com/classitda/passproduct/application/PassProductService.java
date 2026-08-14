package com.classitda.passproduct.application;

import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.repository.PassProductRepository;
import com.classitda.passproduct.exception.PassProductErrorCode;
import com.classitda.passproduct.exception.PassProductException;
import com.classitda.passproduct.presentation.dto.PassProductCreateRequest;
import com.classitda.passproduct.presentation.dto.PassProductResponse;
import com.classitda.passproduct.presentation.dto.PassProductUpdateRequest;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PassProductService {

    private final PassProductRepository passProductRepository;
    private final ClassTypeRepository classTypeRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    @Transactional
    public PassProductResponse save(Long memberId, Long studioId, PassProductCreateRequest request) {
        Studio studio = getManageableStudio(memberId, studioId);
        List<ClassType> classTypes = getClassTypes(studioId, request.classTypeIdsOrEmpty());
        PassProduct passProduct = passProductRepository.save(request.toEntity(studio, classTypes));

        return PassProductResponse.of(passProduct, classTypes);
    }

    public List<PassProductResponse> findAll(Long memberId, Long studioId) {
        getManageableStudio(memberId, studioId);

        return passProductRepository.findAllWithClassTypesByStudioId(studioId).stream()
                .map(passProduct -> PassProductResponse.of(passProduct, passProduct.getClassTypes()))
                .toList();
    }

    @Transactional
    public PassProductResponse update(Long memberId, Long studioId, Long passProductId, PassProductUpdateRequest request
    ) {
        getManageableStudio(memberId, studioId);
        PassProduct passProduct = getPassProduct(studioId, passProductId);
        List<ClassType> classTypes = getClassTypes(studioId, request.classTypeIdsOrEmpty());
        passProduct.update(
                request.name(),
                request.classKind(),
                request.totalCount(),
                request.validPeriodAmount(),
                request.validPeriodUnit(),
                request.totalHoldDays(),
                request.active()
        );
        passProduct.updateClassTypes(classTypes);

        return PassProductResponse.of(passProduct, classTypes);
    }

    private Studio getManageableStudio(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        studioPermissionService.validate(studio, memberId, PermissionCode.PASS_PRODUCT_MANAGE);

        return studio;
    }

    private List<ClassType> getClassTypes(Long studioId, List<Long> classTypeIds) {
        if (classTypeIds.isEmpty()) {
            return List.of();
        }

        List<Long> distinctClassTypeIds = classTypeIds.stream()
                .distinct()
                .toList();
        List<ClassType> classTypes = classTypeRepository.findAllByIdInAndStudioIdOrderByIdAsc(
                distinctClassTypeIds, studioId);
        if (classTypes.size() != distinctClassTypeIds.size()) {
            throw new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND);
        }

        return classTypes;
    }

    private PassProduct getPassProduct(Long studioId, Long passProductId) {
        return passProductRepository.findByIdAndStudioId(passProductId, studioId)
                .orElseThrow(() -> new PassProductException(PassProductErrorCode.PASS_PRODUCT_NOT_FOUND));
    }
}
