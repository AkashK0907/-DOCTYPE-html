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
fun GroupsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    val groups = viewModel.groups

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
                    text = "Study Groups",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { showJoinDialog = true }) { Text("Join Group") }
                OutlinedButton(onClick = { showCreateDialog = true }) { Text("Create Group") }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups.size) { index ->
                    val g = groups[index]
                    Surface(
                        color = colors.surface,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(g.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                            Text(
                                g.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Goal: ${g.dailyGoalMinutes} min · ${if (g.visibilityPublic) "Public" else "Private"} · Max ${g.maxMembers}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, desc, goal, visibility, maxMembers ->
                    viewModel.groups.add(
                        Group(
                            id = viewModel.groups.size + 1,
                            name = name,
                            description = desc,
                            dailyGoalMinutes = goal,
                            visibilityPublic = visibility,
                            maxMembers = maxMembers
                        )
                    )
                    showCreateDialog = false
                }
            )
        }

        if (showJoinDialog) {
            JoinGroupDialog(
                onDismiss = { showJoinDialog = false },
                onJoin = { _, _ ->
                    // TODO: join group backend
                    showJoinDialog = false
                }
            )
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Int, Boolean, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("120") }
    var visibilityPublic by remember { mutableStateOf(true) }
    var maxMembers by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Group") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Group Name") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password / Access Code") })
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it.filter(Char::isDigit) },
                    label = { Text("Daily Study Goal (min)") }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = visibilityPublic,
                        onCheckedChange = { visibilityPublic = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (visibilityPublic) "Public" else "Private")
                }
                OutlinedTextField(
                    value = maxMembers,
                    onValueChange = { maxMembers = it.filter(Char::isDigit) },
                    label = { Text("Max Members") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onCreate(
                        name.trim(),
                        description.trim(),
                        goal.toIntOrNull() ?: 60,
                        visibilityPublic,
                        maxMembers.toIntOrNull() ?: 20
                    )
                }
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onJoin: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Group") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Group Name") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onJoin(name, password) }) { Text("Join") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}