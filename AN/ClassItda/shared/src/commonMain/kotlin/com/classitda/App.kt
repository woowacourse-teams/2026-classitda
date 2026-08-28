package com.classitda

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text("ClassItda")
        }
    }
}
