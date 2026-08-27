package com.classitda.feature.instructor.management.component

import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassType

internal sealed interface CategoryFilter {
    data object All : CategoryFilter

    data class Form(
        val classForm: ClassForm,
    ) : CategoryFilter

    data class Category(
        val classType: ClassType,
    ) : CategoryFilter
}

internal val CategoryFilter.label: String
    get() =
        when (this) {
            CategoryFilter.All -> "전체"
            is CategoryFilter.Form -> classForm.toDisplayLabel()
            is CategoryFilter.Category -> classType.name
        }

internal fun ClassForm.toDisplayLabel(): String =
    when (this) {
        ClassForm.INDIVIDUAL -> "개인 수업"
        ClassForm.GROUP -> "그룹 수업"
    }
