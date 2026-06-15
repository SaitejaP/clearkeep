package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.MediaRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SwipeScreen
import com.example.ui.screens.WhitelistScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room database and repository setup
        val database = AppDatabase.getDatabase(this)
        val repository = MediaRepository(database.mediaDao())
        
        // Instantiate ViewModel
        val viewModel = ViewModelProvider(
            this, 
            AppViewModelFactory(repository)
        )[AppViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: AppViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Swipe") },
                    label = { Text("Swipe Deck") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Whitelist") },
                    label = { Text("Whitelist") }
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Adapt Insights") },
                    label = { Text("AI Insight") }
                )
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentTab) {
            0 -> DashboardScreen(
                viewModel = viewModel, 
                onNavigateToSwipe = { viewModel.setTab(1) },
                modifier = modifier
            )
            1 -> SwipeScreen(
                viewModel = viewModel, 
                modifier = modifier
            )
            2 -> WhitelistScreen(
                viewModel = viewModel, 
                modifier = modifier
            )
            3 -> InsightsScreen(
                viewModel = viewModel, 
                modifier = modifier
            )
        }
    }
}
