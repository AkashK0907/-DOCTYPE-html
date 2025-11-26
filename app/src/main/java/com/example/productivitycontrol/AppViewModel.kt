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
import java.util.UUID

// --- DATA CLASSES ---
data class BlockedApp(val packageName: String, val displayName: String, var enabled: Boolean = true)

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val code: String = "",
    val dailyGoalMinutes: Int = 60,
    val isPublic: Boolean = true,
    val adminId: String = "",
    val members: List<String> = emptyList(),
    val maxMembers: Int = 20
)

data class BadgeDef(val title: String, val description: String, val threshold: Int, val iconChar: String)

data class UserScore(
    val userId: String = "",
    val userName: String = "Anonymous",
    val score: Int = 0,
    val todayMinutes: Int = 0
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
    var leaderboard = mutableStateListOf<UserScore>()
        private set

    var allPublicGroups = mutableStateListOf<Group>()
        private set
    var myGroups = mutableStateListOf<Group>()
        private set
    var currentGroupMembers = mutableStateListOf<UserScore>()
        private set

    val blockedApps = mutableStateListOf<BlockedApp>()

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
            db.collection("tasks")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {
                        val allTasks = snapshots.toObjects(TaskEntity::class.java)
                        for (i in allTasks.indices) allTasks[i].id = snapshots.documents[i].id
                        processTasks(allTasks)
                    }
                }

            fetchLeaderboard()
            fetchPublicGroups()
            fetchMyGroups()
        }
    }

    // --- FIXED LOGIC ---

    private fun processTasks(allTasks: List<TaskEntity>) {
        val cutoff = System.currentTimeMillis() - 86400000 // 24 hours
        activeTasks.clear()
        historyTasks.clear()

        allTasks.sortedByDescending { it.date }.forEach { task ->
            // FIX 2: Do NOT add "Focus Session" to the visible UI lists
            if (task.name == "Focus Session") {
                return@forEach
            }

            if (!task.isCompleted || task.date > cutoff) {
                activeTasks.add(task)
            } else {
                historyTasks.add(task)
            }
        }

        // We still pass 'allTasks' (including hidden sessions) to stats,
        // so your points and calendar still update!
        recalculateStats(allTasks)
    }

    private fun recalculateStats(allTasks: List<TaskEntity>) {
        // 1. Calculate Points
        totalPoints = allTasks.filter { it.isCompleted }.size * 10

        // 2. Calculate Today's Minutes
        val today = LocalDate.now()
        val todayMinutes = allTasks
            .filter { it.isCompleted }
            .filter { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() == today }
            .sumOf { it.durationMinutes }

        updateCloudScore(totalPoints, todayMinutes)

        // 3. Streak
        val completedDates = allTasks.filter { it.isCompleted }
            .map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct().sortedDescending()

        if (completedDates.isEmpty()) { currentStreak = 0; return }
        var streak = 0; var checkDate = today
        if (!completedDates.contains(today)) checkDate = today.minusDays(1)
        while (completedDates.contains(checkDate)) { streak++; checkDate = checkDate.minusDays(1) }
        currentStreak = streak
    }

    private fun updateCloudScore(points: Int, minutes: Int) {
        if (currentUserId == null) return
        val userScore = UserScore(currentUserId, currentUserName, points, minutes)
        db.collection("leaderboard").document(currentUserId).set(userScore)
    }

    // --- ACTIONS ---

    fun startFocus() {
        focusRunning = true
        FocusState.isBlockingActive = true
    }

    fun stopFocus() {
        focusRunning = false
        FocusState.isBlockingActive = false

        if (focusSeconds > 60 && currentUserId != null) {
            val minutes = focusSeconds / 60
            val sessionTask = TaskEntity(
                name = "Focus Session", // This name triggers the filter in processTasks
                durationMinutes = minutes,
                date = System.currentTimeMillis(),
                userId = currentUserId,
                isCompleted = true
            )
            db.collection("tasks").add(sessionTask)
        }
        focusSeconds = 0
    }

    fun completeFocusSecondTick() { if (focusRunning) focusSeconds++ }

    fun toggleTheme() { isDarkTheme = !isDarkTheme }

    fun addTask(name: String, durationMinutes: Int) {
        if (currentUserId == null) return
        val newTask = TaskEntity(name = name, durationMinutes = durationMinutes, date = System.currentTimeMillis(), userId = currentUserId, isCompleted = false)
        db.collection("tasks").add(newTask)
    }

    fun markTaskCompleted(task: TaskEntity) {
        // FIX 1: INSTANT UI UPDATE (Optimistic)
        val index = activeTasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            // We create a copy to force the UI to redraw immediately
            activeTasks[index] = activeTasks[index].copy(isCompleted = !task.isCompleted)
        }

        // Send to DB in background
        if (task.id.isNotEmpty()) {
            db.collection("tasks").document(task.id).update("isCompleted", !task.isCompleted)
                .addOnFailureListener {
                    // Revert if internet fails
                    if (index != -1) activeTasks[index] = activeTasks[index].copy(isCompleted = task.isCompleted)
                }
        }
    }

    // --- FETCHERS ---
    private fun fetchLeaderboard() {
        db.collection("leaderboard").orderBy("score", Query.Direction.DESCENDING).limit(20)
            .addSnapshotListener { snapshots, _ -> if (snapshots != null) { leaderboard.clear(); leaderboard.addAll(snapshots.toObjects(UserScore::class.java)) } }
    }
    private fun fetchPublicGroups() {
        db.collection("groups").whereEqualTo("public", true).limit(20)
            .addSnapshotListener { snapshots, _ -> if (snapshots != null) { allPublicGroups.clear(); allPublicGroups.addAll(snapshots.toObjects(Group::class.java)) } }
    }
    private fun fetchMyGroups() {
        if (currentUserId == null) return
        db.collection("groups").whereArrayContains("members", currentUserId)
            .addSnapshotListener { snapshots, _ -> if (snapshots != null) { myGroups.clear(); myGroups.addAll(snapshots.toObjects(Group::class.java)) } }
    }
    fun fetchGroupMembers(memberIds: List<String>) {
        currentGroupMembers.clear()
        if (memberIds.isEmpty()) return
        memberIds.forEach { memberId ->
            db.collection("leaderboard").document(memberId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(UserScore::class.java)
                    if (user != null) currentGroupMembers.add(user)
                }
            }
        }
    }
    fun createGroup(name: String, description: String, goal: Int, isPublic: Boolean, maxMembers: Int) {
        if (currentUserId == null) return
        val uniqueCode = UUID.randomUUID().toString().substring(0, 6).uppercase()
        val newGroup = Group("", name, description, uniqueCode, goal, isPublic, currentUserId, listOf(currentUserId), maxMembers)
        db.collection("groups").add(newGroup)
    }
    fun joinGroup(group: Group) {
        if (currentUserId == null || group.members.contains(currentUserId)) return
        db.collection("groups").whereEqualTo("code", group.code).get().addOnSuccessListener { documents -> for (document in documents) document.reference.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId)) }
    }
    fun joinByCode(code: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (currentUserId == null) return
        db.collection("groups").whereEqualTo("code", code.uppercase()).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) { documents.documents[0].reference.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId)); onSuccess() } else { onFailure() }
        }
    }
    fun addMockBlockedAppsIfEmpty() { if (blockedApps.isEmpty()) blockedApps.addAll(listOf(BlockedApp("com.instagram.android", "Instagram"), BlockedApp("com.facebook.katana", "Facebook"), BlockedApp("com.twitter.android", "X / Twitter"), BlockedApp("com.snapchat.android", "Snapchat"))) }
}