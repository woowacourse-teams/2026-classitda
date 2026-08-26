package com.classitda.di.instructor

import com.classitda.data.remote.api.ClassTemplatesApi
import com.classitda.data.remote.api.ClassTypesApi
import com.classitda.data.repository.instructor.management.ClassTemplateManagementRepositoryImpl
import com.classitda.data.repository.instructor.management.FakeClassManagementRepository
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.domain.repository.instructor.management.ClassTemplateManagementRepository
import com.classitda.feature.instructor.classsession.detail.ClassSessionDetailViewModel
import com.classitda.feature.instructor.classsession.edit.ClassSessionEditViewModel
import com.classitda.feature.instructor.classsession.member.edit.ClassSessionMemberEditViewModel
import com.classitda.feature.instructor.management.classes.ClassListViewModel
import com.classitda.feature.instructor.management.classes.create.ClassSessionCreateViewModel
import com.classitda.feature.instructor.management.classtemplates.ClassTemplateManagementViewModel
import com.classitda.feature.instructor.management.classtemplates.create.ClassTemplateCreateViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val classManagementModule =
    module {
        single<ClassManagementRepository> { FakeClassManagementRepository() }
        viewModel { ClassManagementViewModel(get()) }
        viewModel { ClassSessionDetailViewModel(get(), get()) }
        viewModel { ClassSessionEditViewModel(get(), get()) }
        viewModel { ClassSessionMemberEditViewModel(get(), get(), get()) }
        single { ClassTemplatesApi(get()) }
        single { ClassTypesApi(get()) }
        single<ClassTemplateManagementRepository> { ClassTemplateManagementRepositoryImpl(get(), get()) }
        viewModel { ClassTemplateManagementViewModel(get()) }
        viewModel { ClassListViewModel(get()) }
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
