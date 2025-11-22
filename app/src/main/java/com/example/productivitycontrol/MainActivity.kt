package com.example.productivitycontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.productivitycontrol.ui.theme.ProductivityTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- ENABLE FULL SCREEN (IMMERSIVE MODE) ---

        // 1. Allow the app to draw behind the bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Hide the System Bars (Status Bar & Navigation Bar)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Configure behavior: Swipe from edge to bring bars back temporarily
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide both the top Status Bar and the bottom Navigation Bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // -------------------------------------------

        val appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        setContent {
            ProductivityTheme(darkTheme = appViewModel.isDarkTheme) {
                AppNavHost(appViewModel = appViewModel)
            }
        }
    }
}