package com.example.productivitycontrol

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onOpenGroups: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenPoints: () -> Unit,
    onOpenBadges: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenHistory: () -> Unit // <--- Added this parameter
) {
    // FIX: Change 'tasks' to 'activeTasks'
    val tasks = appViewModel.activeTasks

    val colors = MaterialTheme.colorScheme
    val isDark = appViewModel.isDarkTheme

    var showMenu by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(appViewModel.focusRunning) {
        if (appViewModel.focusRunning) {
            while (true) {
                delay(1000L)
                appViewModel.completeFocusSecondTick()
            }
        }
    }

    val bgBrush = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF151515), Color(0xFF000000)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = bgBrush)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(icon = Icons.Default.Menu, onClick = { showMenu = !showMenu })

                // LOGO
                Image(
                    painter = painterResource(id = R.drawable.header),
                    contentDescription = "App Logo",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.height(40.dp).widthIn(max = 150.dp),
                    colorFilter = if (isDark) null else androidx.compose.ui.graphics.ColorFilter.tint(Color.Black)
                )

                GlassIconButton(icon = Icons.Default.Notifications, onClick = { onOpenNotifications() })
            }

            // MENU
            if (showMenu) {
                Spacer(Modifier.height(16.dp))
                GlassCard(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column {
                        MenuRow(Icons.Default.CalendarMonth, "Calendar") { onOpenCalendar(); showMenu = false }
                        MenuDivider()
                        // NEW HISTORY BUTTON
                        MenuRow(Icons.Default.History, "Past Tasks") { onOpenHistory(); showMenu = false }
                        MenuDivider()
                        MenuRow(Icons.Default.Star, "Points (${appViewModel.totalPoints})") { onOpenPoints(); showMenu = false }
                        MenuDivider()
                        MenuRow(Icons.Default.WorkspacePremium, "Badges") { onOpenBadges(); showMenu = false }
                        MenuDivider()
                        MenuRow(if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle Theme") { appViewModel.toggleTheme() }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // GREETING & STREAK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Your Focus", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = colors.primary)
                    Text("${tasks.count { !it.isCompleted }} tasks remaining", style = MaterialTheme.typography.bodyMedium, color = colors.primary.copy(alpha = 0.5f))
                }
                if (appViewModel.currentStreak > 0) {
                    GlassCard {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("${appViewModel.currentStreak} Days", style = MaterialTheme.typography.labelSmall, color = colors.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // TASK LIST
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(tasks) { task ->
                    TaskCard(task = task, onToggleCompleted = { appViewModel.markTaskCompleted(task) })
                }
            }

            Spacer(Modifier.height(12.dp))

            // ADD BUTTON
            Button(
                onClick = { showAddTaskDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp).border(0.5.dp, Brush.verticalGradient(listOf(colors.primary.copy(0.5f), colors.primary.copy(0.1f))), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
            ) { Text("+ New Task", fontSize = 15.sp, color = colors.primary.copy(0.9f)) }

            Spacer(Modifier.height(16.dp))

            // TIMER
            FocusControlRow(appViewModel.focusRunning, appViewModel.focusSeconds, { appViewModel.startFocus() }, { appViewModel.stopFocus() })

            Spacer(Modifier.height(100.dp))
        }

        // FABs
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
            GlassFAB(icon = Icons.Default.CameraAlt, size = 75.dp, iconSize = 32.dp, onClick = { /* Camera */ })
        }
        Box(modifier = Modifier.align(Alignment.BottomStart).padding(32.dp)) {
            GlassFAB(icon = Icons.Default.EmojiEvents, size = 56.dp, iconSize = 24.dp, onClick = { onOpenLeaderboard() })
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp)) {
            GlassFAB(icon = Icons.Default.Groups, size = 56.dp, iconSize = 24.dp, onClick = { onOpenGroups() })
        }

        if (showAddTaskDialog) {
            AddTaskDialog({ showAddTaskDialog = false }, { name, mins -> appViewModel.addTask(name, mins); showAddTaskDialog = false })
        }
    }
}

// --- COMPONENTS ---
@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(colors.surface).border(0.5.dp, Brush.verticalGradient(listOf(colors.outline.copy(alpha = 0.3f), colors.outline.copy(alpha = 0.05f))), RoundedCornerShape(20.dp))) { content() }
}

@Composable
fun TaskCard(task: TaskEntity, onToggleCompleted: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.name, style = MaterialTheme.typography.bodyLarge, color = if (task.isCompleted) colors.primary.copy(alpha = 0.3f) else colors.primary.copy(0.9f), textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null)
                Spacer(Modifier.height(2.dp))
                Text("${task.durationMinutes} min", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(alpha = 0.4f))
            }
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleCompleted() }, colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.primary.copy(alpha = 0.3f), checkmarkColor = colors.onPrimary))
        }
    }
}

@Composable
fun FocusControlRow(isRunning: Boolean, seconds: Int, onStart: () -> Unit, onStop: () -> Unit) {
    val minutes = seconds / 60
    val remSeconds = seconds % 60
    val colors = MaterialTheme.colorScheme
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("TIMER", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(alpha = 0.4f), letterSpacing = 1.sp)
                Text(String.format(Locale.getDefault(), "%02d:%02d", minutes, remSeconds), style = MaterialTheme.typography.headlineLarge, color = colors.primary, fontWeight = FontWeight.Light)
            }
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.primary).clickable { if (isRunning) onStop() else onStart() }, contentAlignment = Alignment.Center) {
                if (isRunning) Box(Modifier.size(16.dp).background(colors.onPrimary, RoundedCornerShape(2.dp)))
                else Icon(Icons.Default.PlayArrow, null, tint = colors.onPrimary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = colors.primary.copy(0.9f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = colors.primary.copy(0.9f), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = colors.primary.copy(0.3f), modifier = Modifier.size(20.dp))
    }
}

@Composable
fun MenuDivider() {
    val colors = MaterialTheme.colorScheme
    HorizontalDivider(color = colors.outline.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun GlassIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.surface).border(0.5.dp, colors.outline.copy(0.2f), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(icon, null, tint = colors.primary, modifier = Modifier.size(20.dp)) }
}

@Composable
fun GlassFAB(icon: androidx.compose.ui.graphics.vector.ImageVector, size: Dp = 60.dp, iconSize: Dp = 26.dp, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(size).clip(CircleShape).background(colors.surface.copy(alpha=0.9f)).border(0.5.dp, Brush.verticalGradient(listOf(colors.primary.copy(0.5f), Color.Transparent)), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(icon, null, tint = colors.primary, modifier = Modifier.size(iconSize)) }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45") }
    val colors = MaterialTheme.colorScheme
    AlertDialog(containerColor = colors.surface.copy(alpha=0.95f), onDismissRequest = onDismiss, title = { Text("New Task", color = colors.primary) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Title", color = colors.primary.copy(0.5f)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.primary, unfocusedTextColor = colors.primary, focusedBorderColor = colors.primary.copy(0.5f), unfocusedBorderColor = colors.primary.copy(0.2f))); OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Minutes", color = colors.primary.copy(0.5f)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.primary, unfocusedTextColor = colors.primary, focusedBorderColor = colors.primary.copy(0.5f), unfocusedBorderColor = colors.primary.copy(0.2f))) } }, confirmButton = { Button(onClick = { if (name.isNotBlank()) onAdd(name, duration.toIntOrNull()?:30) }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) { Text("Create", color = colors.onPrimary) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.primary.copy(0.5f)) } })
}