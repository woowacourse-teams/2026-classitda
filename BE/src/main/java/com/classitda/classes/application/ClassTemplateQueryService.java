package com.classitda.classes.application;

import com.classitda.classes.domain.template.ClassTemplate;
import com.classitda.classes.domain.repository.ClassTemplateClassTypeRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.projection.TemplateClassTypeProjection;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassTemplateResponse;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ClassTemplateQueryService {

    private final ClassTemplateRepository classTemplateRepository;
    private final ClassTemplateClassTypeRepository classTemplateClassTypeRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    public List<ClassTemplateResponse> findAll(Long memberId, Long studioId) {
        validateManagePermission(memberId, studioId);

        List<ClassTemplate> classTemplates = classTemplateRepository.findAllByStudioIdOrderByIdAsc(studioId);
        return toResponses(studioId, classTemplates);
    }

    private void validateManagePermission(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        studioPermissionService.validate(studio, memberId, PermissionCode.CLASS_TEMPLATE_MANAGE);
    }

    private List<ClassTemplateResponse> toResponses(Long studioId, List<ClassTemplate> templateRoots) {
        if (templateRoots.isEmpty()) {
            return List.of();
        }

        List<Long> classTemplateIds = templateRoots.stream()
                .map(ClassTemplate::getId)
                .toList();

        List<ClassTemplate> classTemplates = classTemplateRepository
                .findAllByIdInOrderByIdAsc(classTemplateIds);

        List<TemplateClassTypeProjection> templateClassTypes = classTemplateClassTypeRepository
                .findAllByTemplateIds(classTemplateIds);

        Map<Long, List<ClassTypeResponse>> classTypesByTemplateId = groupClassTypesByTemplateId(
                studioId,
                classTemplateIds,
                templateClassTypes
        );

        return classTemplates.stream()
                .map(classTemplate -> ClassTemplateResponse.of(
                        classTemplate,
                        classTypesByTemplateId.get(classTemplate.getId())
                ))
                .toList();
    }

    private Map<Long, List<ClassTypeResponse>> groupClassTypesByTemplateId(
            Long studioId,
            List<Long> classTemplateIds,
            List<TemplateClassTypeProjection> templateClassTypes
    ) {
        Map<Long, List<ClassTypeResponse>> classTypesByTemplateId = new LinkedHashMap<>();

        classTemplateIds.forEach(
                classTemplateId -> classTypesByTemplateId.put(classTemplateId, new ArrayList<>())
        );

        templateClassTypes.forEach(templateClassType -> {
            if (!studioId.equals(templateClassType.getStudioId())) {
                throw new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND);
            }
            classTypesByTemplateId.get(templateClassType.getClassTemplateId())
                    .add(ClassTypeResponse.of(
                            templateClassType.getClassTypeId(),
                            templateClassType.getClassTypeName()
                    ));
        });

        return classTypesByTemplateId;
    }
}
