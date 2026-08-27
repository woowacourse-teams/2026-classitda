package com.classitda.feature.instructor.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    var templateRefreshKey by remember { mutableStateOf(0) }

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

        composable<ClassListDestination> {
            ClassListRoute(
                onBackClick = navController::popBackStack,
                onCreateSessionClick = { navController.navigate(ClassSessionCreateDestination) },
                onSessionCardClick = {},
                bottomBar = {},
            )
        }

        composable<ClassSessionCreateDestination> {
            ClassSessionCreateRoute(
                onBackClick = navController::popBackStack,
                onCreated = navController::popBackStack,
            )
        }

        composable<ClassTemplateManagementDestination> {
            ClassTemplateManagementRoute(
                onBackClick = navController::popBackStack,
                onCreateTemplateClick = { navController.navigate(ClassTemplateCreateDestination()) },
                onTemplateCardClick = {},
                onTemplateEditClick = { id -> navController.navigate(ClassTemplateCreateDestination(templateId = id)) },
                bottomBar = {},
                refreshKey = templateRefreshKey,
            )
        }

        composable<ClassTemplateCreateDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ClassTemplateCreateDestination>()
            ClassTemplateCreateRoute(
                templateId = destination.templateId,
                onBackClick = navController::popBackStack,
                onCreated = {
                    templateRefreshKey++
                    navController.popBackStack()
                },
            )
        }
    }
}
