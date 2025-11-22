package com.example.productivitycontrol

import android.widget.Toast
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
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }

    // Search State
    var searchQuery by remember { mutableStateOf("") }

    // Data
    val publicGroups = viewModel.allPublicGroups
    val myGroups = viewModel.myGroups

    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.red < 0.1f

    // Dynamic Background
    val bgBrush = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF151515), Color(0xFF000000)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = bgBrush)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

            // 1. HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(Icons.Default.ArrowBack) { onBack() }
                    Spacer(Modifier.width(16.dp))
                    Text("Study Squads", style = MaterialTheme.typography.headlineSmall, color = colors.primary, fontWeight = FontWeight.SemiBold)
                }

                // Add Button (Create or Join Code)
                GlassIconButton(Icons.Default.Add) { showJoinCodeDialog = true }
            }

            Spacer(Modifier.height(24.dp))

            // 2. SEARCH BAR
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

            // 3. TABS (My Groups vs Browse)
            var selectedTab by remember { mutableStateOf(0) } // 0 = My Groups, 1 = Browse
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
                    GroupCard(group = group, isMember = selectedTab == 0, onJoin = { viewModel.joinGroup(group) })
                }
            }
        }

        // FAB to Create
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp)) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Create")
            }
        }

        // DIALOGS
        if (showCreateDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, desc, goal, isPublic ->
                    viewModel.createGroup(name, desc, goal, isPublic)
                    showCreateDialog = false
                }
            )
        }

        if (showJoinCodeDialog) {
            JoinByCodeDialog(
                onDismiss = { showJoinCodeDialog = false },
                onJoin = { code ->
                    viewModel.joinByCode(code,
                        onSuccess = {
                            Toast.makeText(context, "Joined Successfully!", Toast.LENGTH_SHORT).show()
                            showJoinCodeDialog = false
                        },
                        onFailure = {
                            Toast.makeText(context, "Invalid Code", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun GroupCard(group: Group, isMember: Boolean, onJoin: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(0.5.dp, Brush.verticalGradient(listOf(colors.outline.copy(0.3f), colors.outline.copy(0.05f))), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(group.name, style = MaterialTheme.typography.titleMedium, color = colors.primary, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if(group.isPublic) Icons.Default.Public else Icons.Default.Lock,
                            null,
                            tint = colors.primary.copy(0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if(group.isPublic) "Public" else "Private", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f))
                    }
                }

                if (!isMember) {
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Join", color = colors.onPrimary)
                    }
                } else {
                    // Show Code if Member
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primary.copy(0.1f))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(group.code))
                                // Toast.makeText(LocalContext.current, "Code Copied", Toast.LENGTH_SHORT).show()
                            }
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
                Surface(color = colors.primary.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(" 🎯 ${group.dailyGoalMinutes} min goal ", style = MaterialTheme.typography.labelSmall, color = colors.primary, modifier = Modifier.padding(6.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("👥 ${group.members.size} members", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.4f), modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if(isSelected) colors.primary else colors.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text, color = if(isSelected) colors.onPrimary else colors.primary)
    }
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onCreate: (String, String, Int, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("120") }
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
                DarkGroupTextField(value = goal, onValueChange = { goal = it }, label = "Daily Goal (min)")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                    Spacer(Modifier.width(8.dp))
                    Text(if(isPublic) "Public Group" else "Private (Code Only)", color = colors.primary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, description, goal.toIntOrNull()?:60, isPublic) },
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
        confirmButton = {
            Button(
                onClick = { if (code.isNotBlank()) onJoin(code) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) { Text("Join", color = colors.onPrimary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.primary.copy(0.5f)) } }
    )
}

// Added the missing function!
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