package com.example.productivitycontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
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
fun GroupDetailScreen(
    groupId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    // Find the group object from the ID
    val group = viewModel.myGroups.find { it.id == groupId } ?: viewModel.allPublicGroups.find { it.id == groupId }

    // Fetch members when screen loads
    LaunchedEffect(group) {
        group?.let { viewModel.fetchGroupMembers(it.members) }
    }

    val members = viewModel.currentGroupMembers
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.red < 0.1f

    val bgBrush = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF151515), Color(0xFF000000)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = bgBrush)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

            // HEADER
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(Icons.Default.ArrowBack) { onBack() }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(group?.name ?: "Loading...", style = MaterialTheme.typography.headlineSmall, color = colors.primary, fontWeight = FontWeight.Bold)
                    Text("Squad Details", style = MaterialTheme.typography.bodySmall, color = colors.primary.copy(0.5f))
                }
            }

            Spacer(Modifier.height(24.dp))

            if (group != null) {
                // INFO CARD
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Description", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f))
                        Text(group.description, style = MaterialTheme.typography.bodyMedium, color = colors.primary)
                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Code", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f))
                                Text(group.code, style = MaterialTheme.typography.titleMedium, color = colors.primary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Goal", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f))
                                Text("${group.dailyGoalMinutes} mins", style = MaterialTheme.typography.titleMedium, color = colors.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("MEMBERS (${members.size})", style = MaterialTheme.typography.labelSmall, color = colors.primary.copy(0.5f), letterSpacing = 2.sp)
                Spacer(Modifier.height(12.dp))

                // MEMBER LIST
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(members) { member ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primary.copy(0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, null, tint = colors.primary)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(member.userName, style = MaterialTheme.typography.bodyLarge, color = colors.primary, fontWeight = FontWeight.Medium)
                                }

                                // Study Stats (Using Total Points)
                                // Study Stats (Now shows Minutes!)
                                Surface(color = colors.primary.copy(0.05f), shape = RoundedCornerShape(8.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, null, tint = colors.primary.copy(0.6f), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        // CHANGED: Now showing Today's Minutes
                                        Text(
                                            "${member.todayMinutes}m today",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.primary.copy(0.8f),
                                            fontWeight = FontWeight.Bold
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
}