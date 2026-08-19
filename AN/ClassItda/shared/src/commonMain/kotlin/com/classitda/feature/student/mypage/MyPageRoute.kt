package com.classitda.feature.student.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.student.StudentTab
import com.classitda.feature.student.mypage.contract.MyPageAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MyPageRoute(
    onOpenProfile: () -> Unit,
    onOpenPasses: () -> Unit,
    onOpenConnectedFacilities: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenInstructorSignup: () -> Unit,
    onSwitchToInstructor: () -> Unit,
    onTabSelected: (StudentTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPageViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPageScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                MyPageAction.Retry -> viewModel.onAction(action)
                MyPageAction.OpenProfile -> onOpenProfile()
                MyPageAction.OpenPasses -> onOpenPasses()
                MyPageAction.OpenConnectedFacilities -> onOpenConnectedFacilities()
                MyPageAction.OpenNotificationSettings -> onOpenNotificationSettings()
                MyPageAction.OpenPrivacyPolicy -> onOpenPrivacyPolicy()
                MyPageAction.OpenInstructorSignup -> onOpenInstructorSignup()
                MyPageAction.SwitchToInstructor -> onSwitchToInstructor()
            }
        },
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}
