package com.example.productivitycontrol

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GroupsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    val groups = viewModel.groups
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.red < 0.1f

    // 1. DYNAMIC LIQUID BACKGROUND
    val bgBrush = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF151515), Color(0xFF000000)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            // 2. HEADER
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(0.5.dp, colors.outline.copy(0.2f), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = colors.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Study Squads",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // 3. ACTION BUTTONS
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showJoinDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Join", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                    border = BorderStroke(0.5.dp, colors.outline.copy(0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Create", color = colors.primary)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 4. GROUP LIST
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(groups.size) { index ->
                    val g = groups[index]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.surface) // Dynamic Glass
                            .border(
                                width = 0.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(colors.outline.copy(0.3f), colors.outline.copy(0.05f))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(g.name, style = MaterialTheme.typography.titleMedium, color = colors.primary, fontWeight = FontWeight.Bold)
                                if(g.visibilityPublic) {
                                    Text("PUBLIC", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f))
                                } else {
                                    Text("PRIVATE", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.3f))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(g.description, style = MaterialTheme.typography.bodyMedium, color = colors.primary.copy(alpha = 0.7f))
                            Spacer(Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = colors.primary.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(" 🎯 ${g.dailyGoalMinutes} min goal ", style = MaterialTheme.typography.labelSmall, color = colors.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("👥 ${g.maxMembers} members", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.4f))
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, desc, goal, visibility, maxMembers ->
                    viewModel.groups.add(Group(viewModel.groups.size + 1, name, desc, goal, visibility, maxMembers))
                    showCreateDialog = false
                }
            )
        }
        if (showJoinDialog) {
            JoinGroupDialog(
                onDismiss = { showJoinDialog = false },
                onJoin = { _, _ -> showJoinDialog = false }
            )
        }
    }
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onCreate: (String, String, Int, Boolean, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("120") }
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        containerColor = colors.surface.copy(alpha=0.95f),
        onDismissRequest = onDismiss,
        title = { Text("New Squad", color = colors.primary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DarkGroupTextField(value = name, onValueChange = { name = it }, label = "Group Name")
                DarkGroupTextField(value = description, onValueChange = { description = it }, label = "Description")
                DarkGroupTextField(value = goal, onValueChange = { goal = it }, label = "Daily Goal (min)")
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, description, goal.toIntOrNull()?:60, true, 20) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) { Text("Create", color = colors.onPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.primary.copy(0.5f)) } }
    )
}

@Composable
fun JoinGroupDialog(onDismiss: () -> Unit, onJoin: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        containerColor = colors.surface.copy(alpha=0.95f),
        onDismissRequest = onDismiss,
        title = { Text("Join Squad", color = colors.primary, fontWeight = FontWeight.Bold) },
        text = { DarkGroupTextField(value = name, onValueChange = { name = it }, label = "Group Code") },
        confirmButton = { Button(onClick = { onJoin(name, "") }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) { Text("Join", color = colors.onPrimary, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.primary.copy(0.5f)) } }
    )
}

@Composable
fun DarkGroupTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = colors.primary.copy(0.5f)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.primary,
            unfocusedTextColor = colors.primary,
            focusedBorderColor = colors.primary.copy(0.5f),
            unfocusedBorderColor = colors.primary.copy(0.2f),
            cursorColor = colors.primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}