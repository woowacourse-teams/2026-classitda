package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.di.myschedule.myScheduleDemoModule
import com.classitda.feature.student.myschedule.MyScheduleFlowRoute
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(myScheduleDemoModule)
            },
    ) {
        AppTheme(theme = ThemeType.STUDENT) {
            MyScheduleFlowRoute()
        }
    }
}
