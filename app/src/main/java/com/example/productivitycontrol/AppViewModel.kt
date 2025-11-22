package com.example.productivitycontrol

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// --- DATA CLASSES ---
data class BlockedApp(val packageName: String, val displayName: String, var enabled: Boolean = true)
data class Group(val id: Int, val name: String, val description: String, val dailyGoalMinutes: Int, val visibilityPublic: Boolean, val maxMembers: Int)
data class BadgeDef(val title: String, val description: String, val threshold: Int, val iconChar: String)

// NEW: Leaderboard Data Model
data class UserScore(
    val userId: String = "",
    val userName: String = "Anonymous",
    val score: Int = 0
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val currentUserId = auth.currentUser?.uid
    private val currentUserName = auth.currentUser?.displayName ?: "You"

    var isDarkTheme by mutableStateOf(true)
        private set

    // Lists
    var activeTasks = mutableStateListOf<TaskEntity>()
        private set
    var historyTasks = mutableStateListOf<TaskEntity>()
        private set

    // NEW: The Real Leaderboard List
    var leaderboard = mutableStateListOf<UserScore>()
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
        if (currentUserId != null) {
            // 1. Listen to YOUR tasks
            db.collection("tasks")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshots != null) {
                        val allTasks = snapshots.toObjects(TaskEntity::class.java)
                        for (i in allTasks.indices) allTasks[i].id = snapshots.documents[i].id
                        processTasks(allTasks)
                    }
                }

            // 2. Listen to Global Leaderboard
            fetchLeaderboard()
        }
    }

    private fun fetchLeaderboard() {
        db.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshots, e ->
                if (e == null && snapshots != null) {
                    leaderboard.clear()
                    leaderboard.addAll(snapshots.toObjects(UserScore::class.java))
                }
            }
    }

    private fun processTasks(allTasks: List<TaskEntity>) {
        val cutoff = System.currentTimeMillis() - 86400000
        activeTasks.clear()
        historyTasks.clear()

        allTasks.sortedByDescending { it.date }.forEach { task ->
            if (!task.isCompleted || task.date > cutoff) activeTasks.add(task)
            else historyTasks.add(task)
        }
        recalculateStats(allTasks)
    }

    private fun recalculateStats(allTasks: List<TaskEntity>) {
        totalPoints = allTasks.filter { it.isCompleted }.size * 10

        // Update Cloud Score
        updateCloudScore(totalPoints)

        val completedDates = allTasks.filter { it.isCompleted }
            .map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct().sortedDescending()

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

    private fun updateCloudScore(points: Int) {
        if (currentUserId == null) return
        // Upsert score to "leaderboard" collection
        val userScore = UserScore(currentUserId, currentUserName, points)
        db.collection("leaderboard").document(currentUserId).set(userScore)
    }

    fun toggleTheme() { isDarkTheme = !isDarkTheme }

    fun addTask(name: String, durationMinutes: Int) {
        if (currentUserId == null) return
        val newTask = TaskEntity(name = name, durationMinutes = durationMinutes, date = System.currentTimeMillis(), userId = currentUserId, isCompleted = false)
        db.collection("tasks").add(newTask)
    }

    fun markTaskCompleted(task: TaskEntity) {
        if (task.id.isNotEmpty()) {
            db.collection("tasks").document(task.id).update("completed", !task.isCompleted)
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

    fun startFocus() { focusRunning = true; FocusState.isBlockingActive = true }
    fun stopFocus() { focusRunning = false; FocusState.isBlockingActive = false }
    fun completeFocusSecondTick() { if (focusRunning) focusSeconds++ }
}