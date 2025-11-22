package com.example.productivitycontrol

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(appViewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.HOME) { popUpTo(0) } },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistrationComplete = { navController.navigate(Routes.WHY) },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.WHY) {
            WhyScreen(
                onNext = { navController.navigate(Routes.TASK_SELECTION) },
                onPrev = { navController.popBackStack() }
            )
        }

        composable(Routes.TASK_SELECTION) {
            TaskSelectionScreen(
                viewModel = appViewModel,
                onNext = { navController.navigate(Routes.APP_BLOCK_SELECTION) },
                onPrev = { navController.popBackStack() }
            )
        }

        composable(Routes.APP_BLOCK_SELECTION) {
            AppBlockSelectionScreen(
                viewModel = appViewModel,
                onNext = { navController.navigate(Routes.PERMISSIONS) },
                onPrev = { navController.popBackStack() }
            )
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                onDone = { navController.navigate(Routes.HOME) { popUpTo(0) } },
                onPrev = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                appViewModel = appViewModel,
                onOpenGroups = { navController.navigate(Routes.GROUPS) },
                onOpenLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
                onOpenPoints = { navController.navigate(Routes.POINTS) },
                onOpenBadges = { navController.navigate(Routes.BADGES) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) } // <--- NEW LINK
            )
        }

        composable(Routes.GROUPS) {
            GroupsScreen(viewModel = appViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(viewModel = appViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(viewModel = appViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.POINTS) {
            PointsScreen(viewModel = appViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.BADGES) {
            BadgesScreen(viewModel = appViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        // NEW HISTORY SCREEN ROUTE
        composable(Routes.HISTORY) {
            HistoryScreen(viewModel = appViewModel, onBack = { navController.popBackStack() })
        }
    }
}