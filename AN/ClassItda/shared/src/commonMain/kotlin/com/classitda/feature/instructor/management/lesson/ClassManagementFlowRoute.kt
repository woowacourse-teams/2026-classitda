package com.classitda.feature.instructor.management.lesson

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.di.instructor.classManagementModule
import com.classitda.feature.instructor.management.lesson.create.ClassSessionCreateRoute
import com.classitda.feature.instructor.management.lesson.create.ClassTemplateCreateRoute
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

@Serializable
private data object ClassManagementListDestination

@Serializable
private data class ClassTemplateCreateDestination(
    val templateId: String? = null,
)

@Serializable
private data object ClassSessionCreateDestination

@Composable
internal fun ClassManagementFlowRoute(
    onBackClick: () -> Unit,
    onTemplateCardClick: (String) -> Unit,
    onSessionCardClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ClassManagementViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val templates = (uiState as? ClassManagementUiState.Success)?.content?.templates.orEmpty()
    val customCategories = (uiState as? ClassManagementUiState.Success)?.content?.customCategories.orEmpty()

    NavHost(
        navController = navController,
        startDestination = ClassManagementListDestination,
        modifier = modifier,
    ) {
        composable<ClassManagementListDestination> {
            ClassManagementRoute(
                viewModel = viewModel,
                onBackClick = onBackClick,
                onCreateTemplateClick = { navController.navigate(ClassTemplateCreateDestination()) },
                onCreateSessionClick = { navController.navigate(ClassSessionCreateDestination) },
                onTemplateCardClick = onTemplateCardClick,
                onTemplateEditClick = { templateId ->
                    navController.navigate(ClassTemplateCreateDestination(templateId = templateId))
                },
                onSessionCardClick = onSessionCardClick,
                bottomBar = bottomBar,
            )
        }

        composable<ClassTemplateCreateDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ClassTemplateCreateDestination>()
            ClassTemplateCreateRoute(
                templateId = destination.templateId,
                categories = customCategories,
                onBackClick = navController::popBackStack,
                onCreated = {
                    navController.popBackStack()
                    viewModel.onRetry()
                },
            )
        }

        composable<ClassSessionCreateDestination> {
            ClassSessionCreateRoute(
                templates = templates,
                categories = customCategories,
                onBackClick = navController::popBackStack,
                onCreated = {
                    navController.popBackStack()
                    viewModel.onRetry()
                },
            )
        }
    }
}

@Composable
@Preview
private fun ClassManagementFlowRoutePreview() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(classManagementModule)
            },
    ) {
        AppTheme(theme = ThemeType.INSTRUCTOR) {
            ClassManagementFlowRoute(
                onBackClick = {},
                onTemplateCardClick = {},
                onSessionCardClick = {},
                bottomBar = {},
            )
        }
    }
}
