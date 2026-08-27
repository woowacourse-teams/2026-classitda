package com.classitda.feature.instructor.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.classitda.feature.instructor.management.classes.ClassListRoute
import com.classitda.feature.instructor.management.classes.create.ClassSessionCreateRoute
import com.classitda.feature.instructor.management.classtemplates.ClassTemplateManagementRoute
import com.classitda.feature.instructor.management.classtemplates.create.ClassTemplateCreateRoute
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

@Composable
internal fun ManagementFlowNavHost(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = ManagementMenuDestination,
        modifier = modifier,
    ) {
        composable<ManagementMenuDestination> {
            ManagementMenuScreen(
                onClassListClick = { navController.navigate(ClassListDestination) },
                onClassTemplateManagementClick = { navController.navigate(ClassTemplateManagementDestination) },
                onMemberManagementClick = {},
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
