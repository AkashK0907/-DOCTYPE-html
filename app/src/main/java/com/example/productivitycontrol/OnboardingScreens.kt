package com.example.productivitycontrol

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WhyScreen(
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val options = listOf(
        "Stop wasting time",
        "Build study habit",
        "Stay focused",
        "Improve routine",
        "Track productive hours",
        "Finish daily tasks"
    )
    val selected = remember { mutableStateListOf<String>() }

    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Why are you using this app?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onBackground
                )

                options.forEach { item ->
                    val isSelected = item in selected
                    OutlinedButton(
                        onClick = {
                            if (isSelected) selected.remove(item) else selected.add(item)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) colors.primary else Color.Transparent,
                            contentColor = if (isSelected) colors.onPrimary else colors.onBackground
                        )
                    ) {
                        Text(item)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onPrev) { Text("Prev") }
                Button(onClick = onNext) { Text("Next") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskSelectionScreen(
    viewModel: AppViewModel,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val presets = listOf(
        "Study", "Coding", "Workout", "Reading",
        "Meditation", "Clean Space", "Custom Task"
    )

    var customTask by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("60") }

    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Your Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onBackground
                )

                // FlowRow might require experimental opt-in on some versions
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { taskName ->
                        AssistChip(
                            onClick = {
                                if (taskName != "Custom Task") {
                                    viewModel.addTask(taskName, durationMinutes.toIntOrNull() ?: 60)
                                }
                            },
                            label = { Text(taskName) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.surface,
                                labelColor = colors.onSurface
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = customTask,
                    onValueChange = { customTask = it },
                    label = { Text("Custom Task Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (customTask.isNotBlank()) {
                            viewModel.addTask(
                                customTask.trim(),
                                durationMinutes.toIntOrNull() ?: 60
                            )
                            customTask = ""
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(44.dp)
                ) {
                    Text("Add Task")
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onPrev) { Text("Prev") }
                Button(onClick = onNext) { Text("Next") }
            }
        }
    }
}

@Composable
fun AppBlockSelectionScreen(
    viewModel: AppViewModel,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    // Side effect to load mock data once
    LaunchedEffect(Unit) {
        viewModel.addMockBlockedAppsIfEmpty()
    }

    val apps = viewModel.blockedApps

    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select the Apps to Block",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onBackground
                )
                Text(
                    text = "We auto-detect social media apps installed on your phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )

                // Using LazyColumn for scrollable list
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(apps.size) { index ->
                        val app = apps[index]
                        Surface(
                            color = colors.surface,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(app.displayName, color = colors.onSurface)
                                Switch(
                                    checked = app.enabled,
                                    onCheckedChange = { apps[index] = app.copy(enabled = it) }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onPrev) { Text("Prev") }
                Button(onClick = onNext) { Text("Next") }
            }
        }
    }
}

@Composable
fun PermissionsScreen(
    onDone: () -> Unit,
    onPrev: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val permissions = listOf(
        "Accessibility (detect app usage)",
        "Display Over Other Apps (blocking popup)",
        "Camera (upload proof)"
    )

    Surface(
        color = colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Grant Required Permissions",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onBackground
                )

                permissions.forEach {
                    Surface(
                        color = colors.surface,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                            Text(
                                "Required for core functionality.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onPrev) { Text("Prev") }
                Button(onClick = {
                    // TODO: request actual permissions
                    onDone()
                }) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}