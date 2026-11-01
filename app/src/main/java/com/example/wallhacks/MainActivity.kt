package com.example.wallhacks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wallhacks.ui.CameraScreen
import com.example.wallhacks.ui.MapScreen
import com.example.wallhacks.ui.WallhackerViewModel
import com.example.wallhacks.ui.theme.WallhacksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WallhacksTheme {
                WallhackerApp()
            }
        }
    }
}

private val tabs = listOf("Camera", "Map")

@Composable
fun WallhackerApp(viewModel: WallhackerViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(if (index == 0) "📷" else "🗺") },
                        label = { Text(title) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> CameraScreen(
                state = state,
                onPhotoCaptured = viewModel::onPhotoCaptured,
                onCaptureFailed = viewModel::onCaptureFailed,
                modifier = contentModifier,
            )
            else -> MapScreen(result = state.result, modifier = contentModifier)
        }
    }
}
