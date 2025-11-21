package com.example.productivitycontrol

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// We deleted "DailyTask" because we use "TaskEntity" now!

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

// Changed to "AndroidViewModel" so we can access the Database
class AppViewModel(application: Application) : AndroidViewModel(application) {

    // --- DATABASE CONNECTION ---
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.taskDao()

    var isDarkTheme by mutableStateOf(true)
        private set

    // We now hold a list of Database Entities
    var tasks = mutableStateListOf<TaskEntity>()
        private set

    val blockedApps = mutableStateListOf<BlockedApp>()
    val groups = mutableStateListOf<Group>()
    var totalPoints by mutableStateOf(0)
        private set

    var focusRunning by mutableStateOf(false)
        private set
    var focusSeconds by mutableStateOf(0)
        private set

    // Initialize: Listen to the database!
    init {
        viewModelScope.launch {
            // This updates the list automatically whenever the DB changes
            dao.getAllTasks().collect { dbTasks ->
                tasks.clear()
                tasks.addAll(dbTasks)
            }
        }
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }

    // UPDATED: Save to Database
    fun addTask(name: String, durationMinutes: Int) {
        viewModelScope.launch {
            dao.insertTask(
                TaskEntity(
                    name = name,
                    durationMinutes = durationMinutes
                )
            )
        }
    }

    // UPDATED: Update Database
    fun markTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            dao.updateCompletion(task.id, newStatus)

            if (newStatus) {
                totalPoints += 10
            }
        }
    }

    fun addMockBlockedAppsIfEmpty() {
        if (blockedApps.isEmpty()) {
            blockedApps.addAll(
                listOf(
                    BlockedApp("com.instagram.android", "Instagram"),
                    BlockedApp("com.facebook.katana", "Facebook"),
                    BlockedApp("com.twitter.android", "X / Twitter"),
                    BlockedApp("com.snapchat.android", "Snapchat")
                )
            )
        }
    }

    fun startFocus() {
        focusRunning = true
        // Turn ON the shield!
        FocusState.isBlockingActive = true
    }

    fun stopFocus() {
        focusRunning = false
        // Turn OFF the shield
        FocusState.isBlockingActive = false
    }

    fun completeFocusSecondTick() {
        if (focusRunning) {
            focusSeconds++
        }
    }
}