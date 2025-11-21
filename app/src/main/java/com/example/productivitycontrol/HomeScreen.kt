package com.example.productivitycontrol

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onOpenGroups: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenPoints: () -> Unit,
    onOpenBadges: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val tasks = appViewModel.tasks // This is now a list of Database Entities

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

    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.15f),
                            colors.background,
                            colors.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, "Menu", tint = colors.onBackground)
                    }

                    Text(
                        text = "beWise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        letterSpacing = 1.sp
                    )

                    IconButton(onClick = { onOpenNotifications() }) {
                        Icon(Icons.Default.Notifications, "Notifications", tint = colors.onBackground)
                    }
                }

                if (showMenu) {
                    Surface(
                        color = colors.surface.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MenuItem("Calendar") { onOpenCalendar(); showMenu = false }
                            MenuItem("Toggle Theme") { appViewModel.toggleTheme() }
                            MenuItem("Points (${appViewModel.totalPoints})") { onOpenPoints(); showMenu = false }
                            MenuItem("Badges") { onOpenBadges(); showMenu = false }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("What Can I Do For You?", style = MaterialTheme.typography.headlineMedium, color = colors.onBackground, fontWeight = FontWeight.SemiBold)

                Text(
                    text = "YOUR TASKS",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary.copy(alpha = 0.8f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tasks.size) { index ->
                        val task = tasks[index]
                        TaskCard(
                            task = task,
                            onToggleCompleted = { appViewModel.markTaskCompleted(task) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
                ) {
                    Text("+ ADD NEW TASK")
                }

                Spacer(Modifier.height(20.dp))

                FocusControlRow(
                    isRunning = appViewModel.focusRunning,
                    seconds = appViewModel.focusSeconds,
                    onStart = { appViewModel.startFocus() },
                    onStop = { appViewModel.stopFocus() }
                )

                Spacer(Modifier.height(80.dp))
            }

            // FABs
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                FloatingActionButton(onClick = { /* TODO */ }, containerColor = colors.primary, contentColor = colors.onPrimary, shape = MaterialTheme.shapes.extraLarge) { Icon(Icons.Default.CameraAlt, "Scanner") }
            }
            Box(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                FloatingActionButton(onClick = { onOpenLeaderboard() }, containerColor = colors.surface, contentColor = colors.primary, shape = MaterialTheme.shapes.large) { Icon(Icons.Default.EmojiEvents, "Leaderboard") }
            }
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
                FloatingActionButton(onClick = { onOpenGroups() }, containerColor = colors.surface, contentColor = colors.primary, shape = MaterialTheme.shapes.large) { Icon(Icons.Default.Groups, "Groups") }
            }

            if (showAddTaskDialog) {
                AddTaskDialog(
                    onDismiss = { showAddTaskDialog = false },
                    onAdd = { name, minutes ->
                        appViewModel.addTask(name, minutes)
                        showAddTaskDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItem(label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = colors.onSurface, modifier = Modifier.fillMaxWidth())
    }
}

// Updated to use TaskEntity (Database Object)
@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleCompleted: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) colors.onSurface.copy(alpha = 0.4f) else colors.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${task.durationMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary.copy(alpha = 0.8f)
                )
            }
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompleted() },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primary,
                    uncheckedColor = colors.onSurface.copy(alpha = 0.3f),
                    checkmarkColor = colors.onPrimary
                )
            )
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Task name") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (min)") })
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onAdd(name, duration.toIntOrNull()?:30) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun FocusControlRow(isRunning: Boolean, seconds: Int, onStart: () -> Unit, onStop: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val minutes = seconds / 60
    val remSeconds = seconds % 60

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("FOCUS TIMER", style = MaterialTheme.typography.labelSmall, color = colors.primary, letterSpacing = 2.sp)
                Spacer(Modifier.height(4.dp))
                Text(String.format("%02d:%02d", minutes, remSeconds), style = MaterialTheme.typography.displayMedium, color = colors.onSurface, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { if (isRunning) onStop() else onStart() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
            ) { Text(text = if (isRunning) "PAUSE" else "START") }
        }
    }
}