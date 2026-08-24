package com.classitda.di.instructor

import com.classitda.data.repository.instructor.management.FakeClassManagementRepository
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.classsession.detail.ClassSessionDetailViewModel
import com.classitda.feature.instructor.classsession.edit.ClassSessionEditViewModel
import com.classitda.feature.instructor.classsession.member.edit.ClassSessionMemberEditViewModel
import com.classitda.feature.instructor.management.lesson.ClassManagementViewModel
import com.classitda.feature.instructor.management.lesson.create.ClassSessionCreateViewModel
import com.classitda.feature.instructor.management.lesson.create.ClassTemplateCreateViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val classManagementModule =
    module {
        single<ClassManagementRepository> { FakeClassManagementRepository() }
        viewModel { ClassManagementViewModel(get()) }
        viewModel { ClassSessionDetailViewModel(get()) }
        viewModel { ClassSessionEditViewModel(get()) }
        viewModel { ClassSessionMemberEditViewModel(get()) }
        viewModel { parameters ->
            ClassTemplateCreateViewModel(
                templateId = parameters.getOrNull(),
                repository = get(),
            )
        }
        viewModel { ClassSessionCreateViewModel(get()) }
    }
