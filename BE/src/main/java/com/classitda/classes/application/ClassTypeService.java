package com.classitda.classes.application;

import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassTypeErrorCode;
import com.classitda.classes.exception.ClassTypeException;
import com.classitda.classes.presentation.dto.ClassTypeCreateRequest;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
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

    private final ClassTypeRepository classTypeRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    public ClassTypeResponse save(Long memberId, Long studioId, ClassTypeCreateRequest request) {
        Studio studio = getManageableStudio(memberId, studioId);
        ClassType classType = request.toEntity(studio);

        try {
            ClassType savedClassType = classTypeRepository.saveAndFlush(classType);
            return ClassTypeResponse.of(savedClassType.getId(), savedClassType.getName());
        } catch (DataIntegrityViolationException exception) {
            throw new ClassTypeException(ClassTypeErrorCode.CLASS_TYPE_NAME_DUPLICATED);
        }
    }

    @Transactional(readOnly = true)
    public List<ClassTypeResponse> findAll(Long studioId) {
        if (!studioRepository.existsById(studioId)) {
            throw new StudioException(StudioErrorCode.NOT_FOUND);
        }

        return classTypeRepository.findAllByStudioIdOrderByIdAsc(studioId).stream()
                .map(classType -> ClassTypeResponse.of(classType.getId(), classType.getName()))
                .toList();
    }

    @Transactional
    public void delete(Long memberId, Long studioId, Long classTypeId) {
        ClassType classType = getManageableClassType(memberId, studioId, classTypeId);
        classTypeRepository.delete(classType);
    }

    private ClassType getManageableClassType(Long memberId, Long studioId, Long classTypeId) {
        getManageableStudio(memberId, studioId);

        return classTypeRepository.findByIdAndStudioId(classTypeId, studioId)
                .orElseThrow(() -> new ClassTypeException(ClassTypeErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    private Studio getManageableStudio(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        studioPermissionService.validate(studio, memberId, PermissionCode.CLASS_TYPE_MANAGE);

        return studio;
    }
}
