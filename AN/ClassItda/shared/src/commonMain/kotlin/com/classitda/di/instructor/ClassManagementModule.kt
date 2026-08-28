package com.classitda.di.instructor

import com.classitda.core.time.CurrentDateTimeProvider
import com.classitda.core.time.DeviceCurrentDateTimeProvider
import com.classitda.data.remote.api.ClassSessionsApi
import com.classitda.data.remote.api.ClassTemplatesApi
import com.classitda.data.remote.api.ClassTypesApi
import com.classitda.data.repository.instructor.management.ClassManagementRepositoryImpl
import com.classitda.data.repository.instructor.management.ClassTemplateManagementRepositoryImpl
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
        viewModel { ClassSessionDetailViewModel(get(), get()) }
        viewModel { ClassSessionEditViewModel(get(), get(), get()) }
        viewModel { ClassSessionMemberEditViewModel(get(), get(), get()) }
        single { ClassSessionsApi(get()) }
        single { ClassTemplatesApi(get()) }
        single { ClassTypesApi(get()) }
        single<ClassManagementRepository> { ClassManagementRepositoryImpl(get(), get(), get()) }
        single<ClassTemplateManagementRepository> { ClassTemplateManagementRepositoryImpl(get(), get()) }
        viewModel { ClassTemplateManagementViewModel(get(), get()) }
        viewModel { ClassListViewModel(get()) }
        viewModel { parameters ->
            ClassTemplateCreateViewModel(
                templateId = parameters.getOrNull(),
                repository = get(),
                studioContext = get(),
            )
        }
        single<CurrentDateTimeProvider> { DeviceCurrentDateTimeProvider }
        viewModel { ClassSessionCreateViewModel(get(), get(), get(), get()) }
    }
