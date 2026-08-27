package com.classitda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.auth.AndroidKeystoreAuthTokenStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                tokenStorage =
                    AndroidKeystoreAuthTokenStorage(applicationContext),
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
