package com.example.productivitycontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModelProvider
import com.example.productivitycontrol.ui.theme.ProductivityTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIX: We now create the ViewModel here so it gets the Database context
        val appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        setContent {
            // We removed the 'remember { AppViewModel() }' line

            ProductivityTheme(darkTheme = appViewModel.isDarkTheme) {
                AppNavHost(appViewModel = appViewModel)
            }
        }
    }
}