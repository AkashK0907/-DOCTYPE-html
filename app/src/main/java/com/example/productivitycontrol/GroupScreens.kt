package com.example.productivitycontrol

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GroupsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onGroupClick: (String) -> Unit
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val publicGroups = viewModel.allPublicGroups
    val myGroups = viewModel.myGroups
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.red < 0.1f

    val bgBrush = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF151515), Color(0xFF000000)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = bgBrush)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

            // 1. HEADER
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(Icons.Default.ArrowBack) { onBack() }
                Spacer(Modifier.width(16.dp))
                Text("Study Squads", style = MaterialTheme.typography.headlineSmall, color = colors.primary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))

            // 2. ACTION BUTTONS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showJoinCodeDialog = true },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                    border = BorderStroke(0.5.dp, colors.outline.copy(0.5f))
                ) {
                    Icon(Icons.Default.GroupAdd, null, tint = colors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Join Code", color = colors.primary)
                }

                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Icon(Icons.Default.Add, null, tint = colors.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Create", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. SEARCH & TABS
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search public groups...", color = colors.primary.copy(0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.primary.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary.copy(0.5f),
                    unfocusedBorderColor = colors.outline.copy(0.2f),
                    focusedTextColor = colors.primary,
                    unfocusedTextColor = colors.primary
                )
            )
            Spacer(Modifier.height(24.dp))

            var selectedTab by remember { mutableStateOf(0) }
            Row(modifier = Modifier.fillMaxWidth()) {
                TabButton("My Squads", selectedTab == 0) { selectedTab = 0 }
                Spacer(Modifier.width(12.dp))
                TabButton("Browse Public", selectedTab == 1) { selectedTab = 1 }
            }

            Spacer(Modifier.height(16.dp))

            // 4. LIST
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val listToShow = if (selectedTab == 0) myGroups else publicGroups.filter { it.name.contains(searchQuery, ignoreCase = true) }

                if (listToShow.isEmpty()) {
                    item {
                        Text(
                            if(selectedTab == 0) "You haven't joined any squads yet." else "No public groups found.",
                            color = colors.primary.copy(0.5f),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                items(listToShow) { group ->
                    GroupCard(
                        group = group,
                        isMember = selectedTab == 0,
                        onJoin = { viewModel.joinGroup(group) },
                        onClick = { onGroupClick(group.id) }
                    )
                }
            }
        }

        // DIALOGS
        if (showCreateDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, desc, goal, isPublic, max ->
                    viewModel.createGroup(name, desc, goal, isPublic, max)
                    showCreateDialog = false
                }
            )
        }
        if (showJoinCodeDialog) {
            JoinByCodeDialog(
                onDismiss = { showJoinCodeDialog = false },
                onJoin = { code ->
                    viewModel.joinByCode(code,
                        onSuccess = { Toast.makeText(context, "Joined!", Toast.LENGTH_SHORT).show(); showJoinCodeDialog = false },
                        onFailure = { Toast.makeText(context, "Invalid Code", Toast.LENGTH_SHORT).show() }
                    )
                }
            )
        }
    }
}

@Composable
fun GroupCard(group: Group, isMember: Boolean, onJoin: () -> Unit, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(0.5.dp, Brush.verticalGradient(listOf(colors.outline.copy(0.3f), colors.outline.copy(0.05f))), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(group.name, style = MaterialTheme.typography.titleMedium, color = colors.primary, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if(group.isPublic) Icons.Default.Public else Icons.Default.Lock, null, tint = colors.primary.copy(0.5f), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if(group.isPublic) "Public" else "Private", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f))
                    }
                }
                if (!isMember) {
                    Button(onClick = onJoin, colors = ButtonDefaults.buttonColors(containerColor = colors.primary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(36.dp)) { Text("Join", color = colors.onPrimary) }
                } else {
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(colors.primary.copy(0.1f))
                            .clickable { clipboardManager.setText(AnnotatedString(group.code)) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Code: ${group.code}", style = MaterialTheme.typography.labelSmall, color = colors.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ContentCopy, null, tint = colors.primary, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(group.description, style = MaterialTheme.typography.bodyMedium, color = colors.primary.copy(0.7f))
            Spacer(Modifier.height(16.dp))
            Row {
                Surface(color = colors.primary.copy(0.1f), shape = RoundedCornerShape(8.dp)) { Text(" 🎯 ${group.dailyGoalMinutes} min goal ", style = MaterialTheme.typography.labelSmall, color = colors.primary, modifier = Modifier.padding(6.dp)) }
                Spacer(Modifier.width(8.dp))
                Text("👥 ${group.members.size} / ${group.maxMembers}", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.4f), modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onCreate: (String, String, Int, Boolean, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("120") }
    var maxMembers by remember { mutableStateOf("20") }
    var isPublic by remember { mutableStateOf(true) }
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        containerColor = colors.surface.copy(alpha=0.95f),
        onDismissRequest = onDismiss,
        title = { Text("New Squad", color = colors.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DarkGroupTextField(value = name, onValueChange = { name = it }, label = "Group Name")
                DarkGroupTextField(value = description, onValueChange = { description = it }, label = "Description")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { DarkGroupTextField(value = goal, onValueChange = { goal = it }, label = "Goal (min)") }
                    Box(Modifier.weight(1f)) { DarkGroupTextField(value = maxMembers, onValueChange = { maxMembers = it }, label = "Max Members") }
                }

                // --- IMPROVED SWITCH ROW ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primary.copy(0.05f), RoundedCornerShape(8.dp))
                        .clickable { isPublic = !isPublic }
                        .padding(8.dp)
                ) {
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            checkedTrackColor = colors.primary.copy(0.2f)
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if(isPublic) "Public Squad" else "Private Squad", color = colors.primary, fontWeight = FontWeight.Bold)
                        Text(if(isPublic) "Anyone can find." else "Code required.", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.6f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, description, goal.toIntOrNull()?:60, isPublic, maxMembers.toIntOrNull()?:20) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) { Text("Create", color = colors.onPrimary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.primary.copy(0.5f)) } }
    )
}

@Composable
fun JoinByCodeDialog(onDismiss: () -> Unit, onJoin: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        containerColor = colors.surface.copy(alpha=0.95f),
        onDismissRequest = onDismiss,
        title = { Text("Enter Group Code", color = colors.primary) },
        text = { DarkGroupTextField(value = code, onValueChange = { code = it }, label = "6-Digit Code") },
        confirmButton = { Button(onClick = { if (code.isNotBlank()) onJoin(code) }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) { Text("Join", color = colors.onPrimary) } },
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

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = if(isSelected) colors.primary else colors.surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.height(40.dp)) { Text(text, color = if(isSelected) colors.onPrimary else colors.primary) }
}

