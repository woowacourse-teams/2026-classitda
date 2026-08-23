package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.di.instructor.mypage.instructorMyPageDemoModule
import com.classitda.di.instructor.mypage.instructorMyPageModule
import com.classitda.feature.instructor.mypage.InstructorMyPageNavHost
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(instructorMyPageModule, instructorMyPageDemoModule)
            },
    ) {
        AppTheme(theme = ThemeType.INSTRUCTOR) {
            InstructorMyPageNavHost()
        }
    }
}
