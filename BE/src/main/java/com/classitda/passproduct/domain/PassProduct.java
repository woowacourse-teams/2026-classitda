package com.classitda.passproduct.domain;

import com.classitda.classes.domain.ClassType;
import com.classitda.common.domain.BaseEntity;
import com.classitda.passproduct.exception.PassProductErrorCode;
import com.classitda.passproduct.exception.PassProductException;
import com.classitda.studio.domain.Studio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pass_product")
@Entity
public class PassProduct extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_TOTAL_COUNT = 1;
    private static final int MIN_VALID_PERIOD_AMOUNT = 1;
    private static final int MIN_TOTAL_HOLD_DAYS = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassKind classKind;

    @Column
    private Integer totalCount;

    @Column
    private Integer validPeriodAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PassProductPeriodUnit validPeriodUnit;

    @Column(nullable = false)
    private int totalHoldDays;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "passProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PassProductClassType> passProductClassTypes = new ArrayList<>();

    @Builder
    private PassProduct(
            Studio studio,
            String name,
            ClassKind classKind,
            List<ClassType> classTypes,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            int totalHoldDays
    ) {
        validate(name, classKind, totalCount, validPeriodAmount, validPeriodUnit, totalHoldDays);
        this.studio = studio;
        this.name = name;
        this.classKind = classKind;
        this.totalCount = totalCount;
        this.validPeriodAmount = validPeriodAmount;
        this.validPeriodUnit = validPeriodUnit;
        this.totalHoldDays = totalHoldDays;
        this.active = true;
        validateClassTypes(classTypes);
        addClassTypes(classTypes);
    }

    public void update(
            String name,
            ClassKind classKind,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            int totalHoldDays,
            boolean active
    ) {
        validate(name, classKind, totalCount, validPeriodAmount, validPeriodUnit, totalHoldDays);
        this.name = name;
        this.classKind = classKind;
        this.totalCount = totalCount;
        this.validPeriodAmount = validPeriodAmount;
        this.validPeriodUnit = validPeriodUnit;
        this.totalHoldDays = totalHoldDays;
        this.active = active;
    }

    public boolean isUnlimitedCount() {
        return totalCount == null;
    }

    public boolean isUnlimitedPeriod() {
        return validPeriodAmount == null;
    }

    public boolean belongsTo(Long studioId) {
        return studio.getId().equals(studioId);
    }

    public List<ClassType> getClassTypes() {
        return passProductClassTypes.stream()
                .map(PassProductClassType::getClassType)
                .toList();
    }

    public void updateClassTypes(List<ClassType> classTypes) {
        validateClassTypes(classTypes);
        Set<Long> targetClassTypeIds = classTypes.stream()
                .map(ClassType::getId)
                .collect(Collectors.toSet());
        passProductClassTypes.removeIf(
                passProductClassType -> !targetClassTypeIds.contains(passProductClassType.getClassType().getId()));

        Set<Long> keptClassTypeIds = passProductClassTypes.stream()
                .map(passProductClassType -> passProductClassType.getClassType().getId())
                .collect(Collectors.toSet());
        addClassTypes(classTypes.stream()
                .filter(classType -> !keptClassTypeIds.contains(classType.getId()))
                .toList());
    }

    private void validateClassTypes(List<ClassType> classTypes) {
        if (classTypes == null || classTypes.isEmpty()) {
            throw new PassProductException(PassProductErrorCode.CLASS_TYPE_REQUIRED);
        }
    }

    private void addClassTypes(List<ClassType> classTypes) {
        classTypes.stream()
                .map(classType -> PassProductClassType.builder()
                        .passProduct(this)
                        .classType(classType)
                        .build())
                .forEach(passProductClassTypes::add);
    }

    private void validate(
            String name,
            ClassKind classKind,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            int totalHoldDays
    ) {
        validateName(name);
        validateClassKind(classKind);
        validateTotalCount(totalCount);
        validateValidPeriod(validPeriodAmount, validPeriodUnit);
        validateExpirationCondition(totalCount, validPeriodAmount);
        validateTotalHoldDays(totalHoldDays, validPeriodAmount);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new PassProductException(PassProductErrorCode.INVALID_NAME);
        }
    }

    private void validateClassKind(ClassKind classKind) {
        if (classKind == null) {
            throw new PassProductException(PassProductErrorCode.INVALID_CLASS_KIND);
        }
    }

    private void validateTotalCount(Integer totalCount) {
        if (totalCount != null && totalCount < MIN_TOTAL_COUNT) {
            throw new PassProductException(PassProductErrorCode.INVALID_TOTAL_COUNT);
        }
    }

    private void validateValidPeriod(Integer validPeriodAmount, PassProductPeriodUnit validPeriodUnit) {
        if ((validPeriodAmount == null) != (validPeriodUnit == null)) {
            throw new PassProductException(PassProductErrorCode.INVALID_VALID_PERIOD);
        }
        if (validPeriodAmount != null && validPeriodAmount < MIN_VALID_PERIOD_AMOUNT) {
            throw new PassProductException(PassProductErrorCode.INVALID_VALID_PERIOD);
        }
    }

    private void validateExpirationCondition(Integer totalCount, Integer validPeriodAmount) {
        if (totalCount == null && validPeriodAmount == null) {
            throw new PassProductException(PassProductErrorCode.NO_EXPIRATION_CONDITION);
        }
    }

    private void validateTotalHoldDays(int totalHoldDays, Integer validPeriodAmount) {
        if (totalHoldDays < MIN_TOTAL_HOLD_DAYS) {
            throw new PassProductException(PassProductErrorCode.INVALID_HOLD_DAYS);
        }
        if (validPeriodAmount == null && totalHoldDays > MIN_TOTAL_HOLD_DAYS) {
            throw new PassProductException(PassProductErrorCode.HOLD_DAYS_NOT_ALLOWED);
        }
    }
}
