package com.classitda.feature.instructor.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.feature.instructor.management.classes.ClassListRoute
import com.classitda.feature.instructor.management.classes.create.ClassSessionCreateRoute
import com.classitda.feature.instructor.management.classtemplates.ClassTemplateManagementRoute
import com.classitda.feature.instructor.management.classtemplates.create.ClassTemplateCreateRoute
import com.classitda.feature.instructor.mypage.InstructorMemberEditRoute
import com.classitda.feature.instructor.mypage.InstructorMemberManagementRoute
import com.classitda.feature.instructor.mypage.InstructorMemberRegistrationRoute
import kotlinx.serialization.Serializable

private const val REFRESH_RESULT_KEY = "refresh"

@Serializable
private data object ManagementMenuDestination

@Serializable
private data object ClassListDestination

@Serializable
private data object ClassSessionCreateDestination

@Serializable
private data object ClassTemplateManagementDestination

@Serializable
private data class ClassTemplateCreateDestination(
    val templateId: String? = null,
)

@Serializable
private data object MemberManagementDestination

@Serializable
private data object MemberRegistrationDestination

@Serializable
private data class MemberEditDestination(
    val memberId: String,
)

@Composable
internal fun ManagementFlowNavHost(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onOpenStudioRegistration: () -> Unit = {},
) {
    var memberRefreshKey by remember { mutableStateOf(0) }

    NavHost(
        navController = navController,
        startDestination = ManagementMenuDestination,
        modifier = modifier,
    ) {
        composable<ManagementMenuDestination> {
            ManagementMenuScreen(
                onClassListClick = { navController.navigate(ClassListDestination) },
                onClassTemplateManagementClick = { navController.navigate(ClassTemplateManagementDestination) },
                onMemberManagementClick = { navController.navigate(MemberManagementDestination) },
                bottomBar = bottomBar,
            )
        }

        composable<ClassListDestination> { backStackEntry ->
            val shouldRefresh by backStackEntry.savedStateHandle
                .getStateFlow(REFRESH_RESULT_KEY, false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) backStackEntry.savedStateHandle[REFRESH_RESULT_KEY] = false
            }

            ClassListRoute(
                onBackClick = navController::popBackStack,
                onCreateSessionClick = { navController.navigate(ClassSessionCreateDestination) },
                onSessionCardClick = {},
                bottomBar = {},
                shouldRefresh = shouldRefresh,
            )
        }

        composable<ClassSessionCreateDestination> {
            ClassSessionCreateRoute(
                onBackClick = navController::popBackStack,
                onCreated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(REFRESH_RESULT_KEY, true)
                    navController.popBackStack()
                },
            )
        }

        composable<ClassTemplateManagementDestination> { backStackEntry ->
            val shouldRefresh by backStackEntry.savedStateHandle
                .getStateFlow(REFRESH_RESULT_KEY, false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) backStackEntry.savedStateHandle[REFRESH_RESULT_KEY] = false
            }

            ClassTemplateManagementRoute(
                onBackClick = navController::popBackStack,
                onCreateTemplateClick = { navController.navigate(ClassTemplateCreateDestination()) },
                onTemplateCardClick = {},
                onTemplateEditClick = { id -> navController.navigate(ClassTemplateCreateDestination(templateId = id)) },
                bottomBar = {},
                shouldRefresh = shouldRefresh,
            )
        }

        composable<MemberManagementDestination> {
            InstructorMemberManagementRoute(
                onBack = navController::popBackStack,
                onEditMember = { memberId ->
                    navController.navigate(MemberEditDestination(memberId.value))
                },
                onOpenMemberRegistration = { navController.navigate(MemberRegistrationDestination) },
                onOpenStudioRegistration = onOpenStudioRegistration,
                refreshToken = memberRefreshKey,
            )
        }

        composable<MemberRegistrationDestination> {
            InstructorMemberRegistrationRoute(
                onBack = navController::popBackStack,
                onSuccess = {
                    memberRefreshKey++
                    navController.popBackStack(MemberManagementDestination, false)
                },
            )
        }

        composable<MemberEditDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<MemberEditDestination>()
            InstructorMemberEditRoute(
                memberId = InstructorMemberId(destination.memberId),
                onBack = navController::popBackStack,
                onSaved = {
                    memberRefreshKey++
                    navController.popBackStack(MemberManagementDestination, false)
                },
            )
        }

        composable<ClassTemplateCreateDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ClassTemplateCreateDestination>()
            ClassTemplateCreateRoute(
                templateId = destination.templateId,
                onBackClick = navController::popBackStack,
                onCreated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(REFRESH_RESULT_KEY, true)
                    navController.popBackStack()
                },
            )
        }
    }
}
