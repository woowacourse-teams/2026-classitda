package com.classitda.di.instructor

import com.classitda.data.repository.instructor.management.FakeClassManagementRepository
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.management.`class`.ClassManagementViewModel
import com.classitda.feature.instructor.management.`class`.create.ClassSessionCreateViewModel
import com.classitda.feature.instructor.management.`class`.create.ClassTemplateCreateViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val classManagementModule =
    module {
        single<ClassManagementRepository> { FakeClassManagementRepository() }
        viewModel { ClassManagementViewModel(get()) }
        viewModel { parameters ->
            ClassTemplateCreateViewModel(
                templateId = parameters.getOrNull(),
                repository = get(),
            )
        }
        viewModel { ClassSessionCreateViewModel(get()) }
    }
