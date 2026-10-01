package com.example.buildingfexfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.iam.presentation.AuthScreen
import com.example.buildingfexfrontend.shell.presentation.AppShell
import com.example.buildingfexfrontend.ui.theme.BuildingFexFrontEndTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = BuildingFexApp.from(this).container
        setContent {
            BuildingFexFrontEndTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot(container)
                }
            }
        }
    }
}

/** Routes between login and the drawer shell depending on the stored session. */
@Composable
private fun AppRoot(container: AppContainer) {
    val session by container.session.session.collectAsStateWithLifecycle()
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        if (session == null) {
            AuthScreen(container)
        } else {
            AppShell(container)
        }
    }
}
