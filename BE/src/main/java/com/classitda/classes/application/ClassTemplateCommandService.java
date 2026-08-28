package com.classitda.classes.application;

import com.classitda.classes.domain.template.ClassTemplate;
import com.classitda.classes.domain.template.ClassTemplateClassType;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTemplateClassTypeRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassTemplateCreateRequest;
import com.classitda.classes.presentation.dto.ClassTemplateUpdateRequest;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ClassTemplateCommandService {

    private final ClassTemplateRepository classTemplateRepository;
    private final ClassTemplateClassTypeRepository templateClassTypeRepository;
    private final ClassTypeRepository classTypeRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    public void save(Long memberId, Long studioId, ClassTemplateCreateRequest request) {
        Studio studio = getManageableStudio(memberId, studioId);
        ClassType classType = getClassType(studioId, request.classTypeId());

        ClassTemplate classTemplate = classTemplateRepository.save(createClassTemplate(studio, request));
        saveTemplateClassType(classTemplate.getId(), classType);
    }

    public void update(
            Long memberId,
            Long studioId,
            Long classTemplateId,
            ClassTemplateUpdateRequest request
    ) {
        getManageableStudio(memberId, studioId);
        ClassTemplate classTemplate = getClassTemplateForUpdate(studioId, classTemplateId);
        ClassType classType = getClassType(studioId, request.classTypeId());

        classTemplate.updateDetails(
                request.name(),
                request.description(),
                request.classForm(),
                request.durationMinutes(),
                request.startTime(),
                request.recurringDays(),
                request.capacity()
        );

        updateTemplateClassType(classTemplateId, classType);
    }

    public void delete(Long memberId, Long studioId, Long classTemplateId) {
        getManageableStudio(memberId, studioId);

        int deletedCount = classTemplateRepository.deleteByIdAndStudioId(classTemplateId, studioId);
        if (deletedCount == 0) {
            throw new ClassException(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND);
        }
    }

    private Studio getManageableStudio(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        studioPermissionService.validate(studio, memberId, PermissionCode.CLASS_TEMPLATE_MANAGE);

        return studio;
    }

    private ClassTemplate getClassTemplateForUpdate(Long studioId, Long classTemplateId) {
        return classTemplateRepository.findByIdAndStudioId(classTemplateId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND));
    }

    private ClassType getClassType(Long studioId, Long classTypeId) {
        if (classTypeId == null || classTypeId < 1) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }

        return classTypeRepository.findByIdAndStudioId(classTypeId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
    }

    private void updateTemplateClassType(Long classTemplateId, ClassType requestedClassType) {
        Long existingClassTypeId = templateClassTypeRepository
                .findClassTypeIdByTemplateId(classTemplateId)
                .orElse(null);

        if (requestedClassType.getId().equals(existingClassTypeId)) {
            return;
        }
        if (existingClassTypeId != null) {
            templateClassTypeRepository.deleteByTemplateIdAndClassTypeId(
                    classTemplateId,
                    existingClassTypeId
            );
        }

        saveTemplateClassType(classTemplateId, requestedClassType);
    }

    private ClassTemplate createClassTemplate(Studio studio, ClassTemplateCreateRequest request) {
        return ClassTemplate.builder()
                .studioId(studio.getId())
                .name(request.name())
                .description(request.description())
                .classForm(request.classForm())
                .durationMinutes(request.durationMinutes())
                .startTime(request.startTime())
                .recurringDays(request.recurringDays())
                .capacity(request.capacity())
                .build();
    }

    private void saveTemplateClassType(Long classTemplateId, ClassType classType) {
        templateClassTypeRepository.save(ClassTemplateClassType.builder()
                .classTemplateId(classTemplateId)
                .classTypeId(classType.getId())
                .build());
    }
}
