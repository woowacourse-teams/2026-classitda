package com.classitda.feature.student.mypage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.di.mypage.myPageDemoModule
import com.classitda.feature.student.StudentTab
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Serializable
private data object MyPageDestination

@Serializable
private data object ProfileViewDestination

@Serializable
private data object ProfileEditDestination

@Serializable
private data class PhoneNumberChangeDestination(
    val initialPhoneNumber: String,
)

@Serializable
private data object ConnectedFacilitiesDestination

@Serializable
private data object NotificationSettingsDestination

@Composable
internal fun MyPageDemoNavHost(
    onExternalAction: (String) -> Unit,
    onLogout: () -> Unit = {},
    onTabSelected: (StudentTab) -> Unit = {},
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    var profileViewRefreshToken by remember { mutableStateOf(0) }
    var profileEditRefreshToken by remember { mutableStateOf(0) }

    fun navigateToMyPage() {
        navController.navigate(MyPageDestination) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun navigateToProfileView() {
        navController.navigate(ProfileViewDestination) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun navigateToProfileEdit() {
        navController.navigate(ProfileEditDestination) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = MyPageDestination,
        modifier =
            modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .clipToBounds(),
    ) {
        composable<MyPageDestination> {
            MyPageRoute(
                onOpenProfile = {
                    onExternalAction("onOpenProfile")
                    navController.navigate(ProfileViewDestination)
                },
                onOpenPasses = { onExternalAction("OpenPasses") },
                onOpenConnectedFacilities = {
                    onExternalAction("onOpenConnectedFacilities")
                    navController.navigate(ConnectedFacilitiesDestination)
                },
                onOpenNotificationSettings = {
                    onExternalAction("onOpenNotificationSettings")
                    navController.navigate(NotificationSettingsDestination)
                },
                onOpenPrivacyPolicy = { onExternalAction("OpenPrivacyPolicy") },
                onOpenInstructorSignup = { onExternalAction("OpenInstructorSignup") },
                onSwitchToInstructor = { onExternalAction("SwitchToInstructor") },
                onTabSelected = { tab ->
                    onExternalAction("SelectTab/${tab.name}")
                    onTabSelected(tab)
                },
            )
        }

        composable<ProfileViewDestination> {
            ProfileViewRoute(
                onBack = {
                    onExternalAction("onBack/ProfileView")
                    navigateToMyPage()
                },
                onOpenEdit = {
                    onExternalAction("onOpenEdit")
                    navController.navigate(ProfileEditDestination)
                },
                onRequestLogout = {
                    onExternalAction("RequestLogout")
                    onLogout()
                },
                onRequestWithdrawal = { onExternalAction("RequestWithdrawal") },
                refreshToken = profileViewRefreshToken,
            )
        }

        composable<ProfileEditDestination> {
            ProfileEditRoute(
                onBack = {
                    onExternalAction("onBack/ProfileEdit")
                    navigateToProfileView()
                },
                onRequestPhotoChange = { onExternalAction("RequestPhotoChange") },
                onProfileRefreshRequested = { profileViewRefreshToken += 1 },
                onOpenPhoneNumberChange = { phoneNumber ->
                    onExternalAction("onOpenPhoneNumberChange")
                    navController.navigate(
                        PhoneNumberChangeDestination(initialPhoneNumber = phoneNumber),
                    )
                },
                refreshToken = profileEditRefreshToken,
            )
        }

        composable<PhoneNumberChangeDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<PhoneNumberChangeDestination>()
            PhoneNumberChangeRoute(
                initialPhoneNumber = destination.initialPhoneNumber,
                onBack = {
                    onExternalAction("onBack/PhoneNumberChange")
                    navController.popBackStack()
                },
                onComplete = {
                    onExternalAction("onComplete/PhoneNumberChange")
                    profileViewRefreshToken += 1
                    profileEditRefreshToken += 1
                    navigateToProfileEdit()
                },
            )
        }

        composable<ConnectedFacilitiesDestination> {
            ConnectedFacilitiesRoute(
                onBack = {
                    onExternalAction("onBack/ConnectedFacilities")
                    navigateToMyPage()
                },
            )
        }

        composable<NotificationSettingsDestination> {
            NotificationSettingsRoute(
                onBack = {
                    onExternalAction("onBack/NotificationSettings")
                    navigateToMyPage()
                },
            )
        }
    }
}

@Composable
internal fun MyPageDemoHarness(
    showInstructorSignupBanner: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var lastAction by remember { mutableStateOf("없음") }

    KoinApplication(
        configuration =
            koinConfiguration {
                modules(myPageDemoModule(showInstructorSignupBanner))
            },
    ) {
        AppTheme(theme = ThemeType.STUDENT) {
            Box(modifier = modifier.fillMaxSize()) {
                MyPageDemoNavHost(
                    onExternalAction = { action -> lastAction = action },
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = AppShape.Card,
                ) {
                    Text(
                        text = "마지막 callback/action: $lastAction",
                        modifier = Modifier.padding(AppSpacing.sm),
                        style = appTypography().labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(
    name = "F01-F07 Demo flow / Student",
    group = "Harness/MyPage/Flow",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyPageDemoHarnessPreview() {
    MyPageDemoHarness()
}

@Preview(
    name = "F02 Demo flow without banner / Student",
    group = "Harness/MyPage/Flow",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyPageDemoHarnessPreview_HiddenBanner() {
    MyPageDemoHarness(showInstructorSignupBanner = false)
}
