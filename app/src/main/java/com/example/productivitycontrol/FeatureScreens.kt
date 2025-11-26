package com.example.productivitycontrol

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// --- SHARED HELPERS ---
@Composable
fun LiquidBackground(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.red < 0.1f
    val bgBrush = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF151515), Color(0xFF000000)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
    }
    Box(modifier = Modifier.fillMaxSize().background(brush = bgBrush)) { content() }
}

@Composable
fun FeatureGlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(colors.surface).border(width = 0.5.dp, brush = Brush.verticalGradient(listOf(colors.outline.copy(alpha = 0.3f), colors.outline.copy(alpha = 0.05f))), shape = RoundedCornerShape(20.dp))) { content() }
}

@Composable
fun BackButton(onClick: () -> Unit, title: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.surface).border(0.5.dp, colors.outline.copy(0.2f), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowBack, "Back", tint = colors.primary, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = colors.primary)
    }
}

// --- REAL-TIME LEADERBOARD (UPDATED) ---
@Composable
fun LeaderboardScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val leaders = viewModel.leaderboard // Real Cloud Data!
    val colors = MaterialTheme.colorScheme

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            BackButton(onBack, "Global Leaderboard")

            if (leaders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(leaders.size) { index ->
                        val user = leaders[index]
                        FeatureGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "#${index + 1}",
                                        color = colors.primary.copy(0.5f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(30.dp)
                                    )
                                    Text(
                                        if(user.userId == "") "You" else user.userName,
                                        color = colors.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Surface(
                                    color = colors.primary.copy(0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "${user.score} pts",
                                        color = colors.primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- CALENDAR HEATMAP ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeatmapCalendar(tasks: List<TaskEntity>) {
    val colors = MaterialTheme.colorScheme
    val activityMap = remember(tasks) {
        tasks.filter { it.isCompleted }.groupBy { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }.mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
    }
    val today = LocalDate.now()
    val weeks = (0..104).map { today.minusDays(it.toLong()) }.reversed().chunked(7)

    Column {
        Text("CONSISTENCY MAP", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f), letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            items(weeks) { weekDays ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    weekDays.forEach { date -> HeatmapCell(activityMap[date] ?: 0) }
                }
            }
        }
    }
}

@Composable
fun HeatmapCell(minutes: Int) {
    val colors = MaterialTheme.colorScheme
    val color = when {
        minutes == 0 -> colors.surface.copy(alpha = 0.5f)
        minutes < 30 -> Color(0xFF0E4429) // Dark Green
        minutes < 60 -> Color(0xFF006D32) // Medium
        minutes < 120 -> Color(0xFF26A641) // Bright
        else -> Color(0xFF39D353)         // Neon
    }
    // ... Box code ...
}
@Composable
fun CalendarScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val tasks = viewModel.activeTasks + viewModel.historyTasks
    val colors = MaterialTheme.colorScheme
    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            BackButton(onBack, "Activity Log")
            FeatureGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Box(modifier = Modifier.padding(16.dp)) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) HeatmapCalendar(tasks) else Text("Requires Android 8+", color = colors.primary) }
            }
            Text("THIS MONTH", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f), letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Focus", "${tasks.filter{it.isCompleted}.sumOf{it.durationMinutes}} m", Modifier.weight(1f))
                StatCard("Tasks Done", "${tasks.count{it.isCompleted}}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    FeatureGlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.4f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = colors.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistoryScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val historyTasks = viewModel.historyTasks
    val colors = MaterialTheme.colorScheme
    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            BackButton(onBack, "Task Archive")
            if (historyTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history yet.", style = MaterialTheme.typography.titleMedium, color = colors.primary.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(historyTasks) { task ->
                        FeatureGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(task.name, style = MaterialTheme.typography.bodyLarge, color = colors.primary.copy(alpha = 0.6f), textDecoration = TextDecoration.LineThrough)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Completed", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(alpha = 0.4f))
                                }
                                Icon(Icons.Default.CheckCircle, "Done", tint = colors.primary.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val userPoints = viewModel.totalPoints
    val badges = viewModel.badgesList
    val colors = MaterialTheme.colorScheme
    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            BackButton(onBack, "Hall of Fame")
            Text("CURRENT RANK", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f), letterSpacing = 2.sp)
            Text("$userPoints PTS", style = MaterialTheme.typography.displayMedium, color = colors.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(badges) { badge ->
                    val isUnlocked = userPoints >= badge.threshold
                    val progress = if(isUnlocked) 1f else (userPoints.toFloat() / badge.threshold.toFloat()).coerceIn(0f, 1f)
                    FeatureGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(if (isUnlocked) colors.primary else colors.outline.copy(0.1f)), contentAlignment = Alignment.Center) {
                                if (isUnlocked) Text(badge.iconChar, fontSize = 24.sp) else Icon(Icons.Default.Lock, null, tint = colors.primary.copy(0.3f))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(badge.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isUnlocked) colors.primary else colors.primary.copy(0.4f))
                                Text(if (isUnlocked) "Unlocked!" else "${badge.threshold - userPoints} pts to go", style = MaterialTheme.typography.bodySmall, color = colors.primary.copy(0.5f))
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = colors.primary, trackColor = colors.outline.copy(0.1f))
                            }
                            if (isUnlocked) { Spacer(Modifier.width(8.dp)); Icon(Icons.Default.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PointsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            BackButton(onBack, "Wallet")
            FeatureGlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("TOTAL POINTS", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f), letterSpacing = 2.sp)
                    Text("${viewModel.totalPoints}", style = MaterialTheme.typography.displayLarge, color = colors.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            BackButton(onBack, "Notifications")
            FeatureGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("No new distractions.", style = MaterialTheme.typography.titleMedium, color = colors.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Notifications blocked while in focus mode appear here.", style = MaterialTheme.typography.bodySmall, color = colors.primary.copy(0.5f))
                }
            }
        }
    }
}