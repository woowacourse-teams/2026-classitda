package com.classitda.classes.application;

import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassTypeCreateRequest;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.classes.presentation.dto.ClassTypeUpdateRequest;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ClassTypeService {

    private static final String DEFAULT_YOGA_CLASS_TYPE_NAME = "요가";
    private static final String DEFAULT_PILATES_CLASS_TYPE_NAME = "필라테스";

    private final ClassTypeRepository classTypeRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    public void save(Long memberId, Long studioId, ClassTypeCreateRequest request) {
        Studio studio = getManageableStudio(memberId, studioId);
        ClassType classType = request.toEntity(studio);

        try {
            classTypeRepository.saveAndFlush(classType);
        } catch (DataIntegrityViolationException exception) {
            throw new ClassException(ClassErrorCode.CLASS_TYPE_NAME_DUPLICATED);
        }
    }

    public void saveDefaultClassTypes(Studio studio) {
        List<ClassType> defaultClassTypes = List.of(
                ClassType.builder()
                        .studio(studio)
                        .name(DEFAULT_YOGA_CLASS_TYPE_NAME)
                        .build(),
                ClassType.builder()
                        .studio(studio)
                        .name(DEFAULT_PILATES_CLASS_TYPE_NAME)
                        .build()
        );
        classTypeRepository.saveAll(defaultClassTypes);
    }

    @Transactional(readOnly = true)
    public List<ClassTypeResponse> findAll(Long memberId, Long studioId) {
        getManageableStudio(memberId, studioId);

        return classTypeRepository.findAllByStudioIdOrderByIdAsc(studioId).stream()
                .map(classType -> ClassTypeResponse.of(classType.getId(), classType.getName()))
                .toList();
    }

    public void update(Long memberId, Long studioId, Long classTypeId, ClassTypeUpdateRequest request) {
        ClassType classType = getManageableClassType(memberId, studioId, classTypeId);
        classType.updateName(request.name());

        try {
            classTypeRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ClassException(ClassErrorCode.CLASS_TYPE_NAME_DUPLICATED);
        }
    }

    public void delete(Long memberId, Long studioId, Long classTypeId) {
        ClassType classType = getManageableClassType(memberId, studioId, classTypeId);
        classTypeRepository.delete(classType);
    }

    private ClassType getManageableClassType(Long memberId, Long studioId, Long classTypeId) {
        getManageableStudio(memberId, studioId);

        return classTypeRepository.findByIdAndStudioId(classTypeId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    private Studio getManageableStudio(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        studioPermissionService.validate(studio, memberId, PermissionCode.CLASS_TYPE_MANAGE);

        return studio;
    }
}
