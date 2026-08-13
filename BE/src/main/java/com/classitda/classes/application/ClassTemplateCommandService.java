package com.classitda.classes.application;

import com.classitda.classes.domain.ClassTemplate;
import com.classitda.classes.domain.ClassTemplateClassType;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTemplateClassTypeRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassTemplateCreateRequest;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ClassTemplateCommandService {

    private final ClassTemplateRepository classTemplateRepository;
    private final ClassTemplateClassTypeRepository classTemplateClassTypeRepository;
    private final ClassTypeRepository classTypeRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    public void save(Long memberId, Long studioId, ClassTemplateCreateRequest request) {
        Studio studio = getManageableStudio(memberId, studioId);
        List<ClassType> classTypes = getClassTypes(studioId, request.classTypeIds());

        ClassTemplate classTemplate = classTemplateRepository.save(createClassTemplate(studio, request));
        saveTemplateClassTypes(classTemplate.getId(), classTypes);
    }

    private Studio getManageableStudio(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        studioPermissionService.validate(studio, memberId, PermissionCode.CLASS_TEMPLATE_MANAGE);

        return studio;
    }

    private List<ClassType> getClassTypes(Long studioId, List<Long> classTypeIds) {
        validateClassTypeIds(classTypeIds);
        List<ClassType> classTypes = classTypeRepository.findAllByStudioIdAndIdInOrderByIdAsc(
                studioId,
                classTypeIds
        );

        if (classTypes.size() != classTypeIds.size()) {
            throw new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND);
        }

        return classTypes;
    }

    private void validateClassTypeIds(List<Long> classTypeIds) {
        if (classTypeIds == null || classTypeIds.isEmpty()) {
            throw new ClassException(ClassErrorCode.CLASS_TYPES_REQUIRED);
        }
        if (classTypeIds.stream().anyMatch(classTypeId -> classTypeId == null || classTypeId < 1)) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
        if (new HashSet<>(classTypeIds).size() != classTypeIds.size()) {
            throw new ClassException(ClassErrorCode.CLASS_TYPES_DUPLICATED);
        }
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

    private void saveTemplateClassTypes(Long classTemplateId, List<ClassType> classTypes) {
        List<ClassTemplateClassType> templateClassTypes = classTypes.stream()
                .map(classType -> ClassTemplateClassType.builder()
                        .classTemplateId(classTemplateId)
                        .classTypeId(classType.getId())
                        .build())
                .toList();

        classTemplateClassTypeRepository.saveAll(templateClassTypes);
    }
}
