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
