package com.example.productivitycontrol

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// --- DATA CLASSES ---
data class BlockedApp(
    val packageName: String,
    val displayName: String,
    var enabled: Boolean = true
)

data class Group(
    val id: Int,
    val name: String,
    val description: String,
    val dailyGoalMinutes: Int,
    val visibilityPublic: Boolean,
    val maxMembers: Int
)

data class BadgeDef(
    val title: String,
    val description: String,
    val threshold: Int,
    val iconChar: String
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.taskDao()

    var isDarkTheme by mutableStateOf(true)
        private set

    // --- SPLIT LISTS ---
    var activeTasks = mutableStateListOf<TaskEntity>() // For Home Screen
        private set

    var historyTasks = mutableStateListOf<TaskEntity>() // For Past Tasks Screen
        private set

    val blockedApps = mutableStateListOf<BlockedApp>()
    val groups = mutableStateListOf<Group>()

    var totalPoints by mutableStateOf(0)
        private set

    var currentStreak by mutableStateOf(0)
        private set

    var focusRunning by mutableStateOf(false)
        private set
    var focusSeconds by mutableStateOf(0)
        private set

    val badgesList = listOf(
        BadgeDef("Novice", "Earn your first 100 points", 100, "🌱"),
        BadgeDef("Consistency", "Reach 500 points", 500, "🔥"),
        BadgeDef("Achiever", "Hit the 1,000 point mark", 1000, "🚀"),
        BadgeDef("Expert", "Accumulate 5,000 points", 5000, "💎"),
        BadgeDef("Master", "Serious dedication (10k)", 10000, "👑"),
        BadgeDef("Productivity God", "The ultimate rank (20k)", 20000, "⚡")
    )

    init {
        viewModelScope.launch {
            dao.getAllTasks().collect { dbTasks ->
                // 24 Hour Cutoff (86400000 milliseconds)
                val cutoff = System.currentTimeMillis() - 86400000

                activeTasks.clear()
                historyTasks.clear()

                dbTasks.forEach { task ->
                    // IF task is NOT done OR it was done recently -> Keep on Home
                    if (!task.isCompleted || task.date > cutoff) {
                        activeTasks.add(task)
                    } else {
                        // IF task is done AND old -> Move to History
                        historyTasks.add(task)
                    }
                }

                recalculateStats(dbTasks) // Pass full list for points calc
            }
        }
    }

    private fun recalculateStats(allTasks: List<TaskEntity>) {
        totalPoints = allTasks.filter { it.isCompleted }.size * 10

        // Streak Logic
        val completedDates = allTasks
            .filter { it.isCompleted }
            .map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct()
            .sortedDescending()

        if (completedDates.isEmpty()) {
            currentStreak = 0
            return
        }

        var streak = 0
        val today = LocalDate.now()
        var checkDate = today

        if (!completedDates.contains(today)) checkDate = today.minusDays(1)

        while (completedDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        currentStreak = streak
    }

    fun toggleTheme() { isDarkTheme = !isDarkTheme }

    fun addTask(name: String, durationMinutes: Int) {
        viewModelScope.launch {
            dao.insertTask(TaskEntity(name = name, durationMinutes = durationMinutes, date = System.currentTimeMillis()))
        }
    }

    fun markTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            dao.updateCompletion(task.id, newStatus)
        }
    }

    fun addMockBlockedAppsIfEmpty() {
        if (blockedApps.isEmpty()) {
            blockedApps.addAll(listOf(
                BlockedApp("com.instagram.android", "Instagram"),
                BlockedApp("com.facebook.katana", "Facebook"),
                BlockedApp("com.twitter.android", "X / Twitter"),
                BlockedApp("com.snapchat.android", "Snapchat")
            ))
        }
    }

    fun startFocus() {
        focusRunning = true
        FocusState.isBlockingActive = true
    }

    fun stopFocus() {
        focusRunning = false
        FocusState.isBlockingActive = false
    }

    fun completeFocusSecondTick() {
        if (focusRunning) focusSeconds++
    }
}