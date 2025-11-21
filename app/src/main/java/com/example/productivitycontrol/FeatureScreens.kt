package com.example.productivitycontrol

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val fakeLeaders = listOf(
        "Alice – 3200 pts",
        "You – 2750 pts",
        "Dev – 2600 pts",
        "Chris – 2400 pts"
    )
    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onBackground
                    )
                }
                Text(
                    text = "Global Leaderboard",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onBackground
                )
            }

            Spacer(Modifier.height(12.dp))

            fakeLeaders.forEachIndexed { index, line ->
                Surface(
                    color = colors.surface,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("#${index + 1}", color = colors.onSurface)
                        Text(line, color = colors.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onBackground
                    )
                }
                Text(
                    text = "Calendar & Streaks",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onBackground
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Month view (stub): highlight best days and streak periods.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
            Spacer(Modifier.height(16.dp))
            // Mock Calendar UI
            Surface(color = colors.surface, shape = MaterialTheme.shapes.medium) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Calendar Placeholder", color = colors.onSurface)
                }
            }
        }
    }
}

@Composable
fun PointsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onBackground
                    )
                }
                Text("Points History", style = MaterialTheme.typography.titleLarge, color = colors.onBackground)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Total Points: ${viewModel.totalPoints}",
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "History list can go here.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
    }
}

@Composable
fun BadgesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val badgeList = listOf(
        "7-day streak",
        "10-day streak",
        "25-day streak",
        "50-day streak",
        "100-day streak",
        "25 tasks completed",
        "50 tasks completed",
        "100 tasks completed"
    )

    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onBackground
                    )
                }
                Text("Badges", style = MaterialTheme.typography.titleLarge, color = colors.onBackground)
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(badgeList.size) { index ->
                    val name = badgeList[index]
                    Surface(
                        color = colors.surface,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, color = colors.onSurface)
                            Icon(
                                imageVector = Icons.Default.ArrowBack, // Using generic icon for lock
                                contentDescription = "Locked",
                                tint = colors.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onBackground
                    )
                }
                Text("Blocked Notifications", style = MaterialTheme.typography.titleLarge, color = colors.onBackground)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Here you’ll see notifications that were held while you were in focus mode.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
    }
}