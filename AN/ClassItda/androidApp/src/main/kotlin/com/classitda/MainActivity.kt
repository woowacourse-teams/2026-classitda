package com.classitda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.auth.AndroidKeystoreAuthTokenStorage
import com.classitda.core.database.createInMemoryDatabaseModule
import com.classitda.core.database.createPlatformDatabaseModule
import com.classitda.core.network.ClassItdaApiConfig
import com.classitda.core.studio.SettingsInstructorStudioSelectionStorage
import com.russhwolf.settings.SharedPreferencesSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val localDatabaseModule = createPlatformDatabaseModule(applicationContext)
        setContent {
            App(
                baseUrl = if (BuildConfig.DEBUG) ClassItdaApiConfig.DEV_BASE_URL else ClassItdaApiConfig.PROD_BASE_URL,
                localDatabaseModule = localDatabaseModule,
                tokenStorage =
                    AndroidKeystoreAuthTokenStorage(applicationContext),
                studioSelectionStorage =
                    SettingsInstructorStudioSelectionStorage(
                        SharedPreferencesSettings.Factory(applicationContext).create("app_preferences"),
                    ),
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        baseUrl = ClassItdaApiConfig.DEV_BASE_URL,
        localDatabaseModule = createInMemoryDatabaseModule(),
    )
}
