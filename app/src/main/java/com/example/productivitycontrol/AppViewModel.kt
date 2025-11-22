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

// UPDATED GROUP MODEL
data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val code: String = "",          // Unique 6-char code
    val dailyGoalMinutes: Int = 60,
    val isPublic: Boolean = true,   // Public vs Private
    val adminId: String = "",       // The creator
    val members: List<String> = emptyList() // List of User IDs
)

data class BadgeDef(val title: String, val description: String, val threshold: Int, val iconChar: String)
data class UserScore(val userId: String = "", val userName: String = "Anonymous", val score: Int = 0)

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

    // GROUPS LISTS
    var allPublicGroups = mutableStateListOf<Group>()
        private set
    var myGroups = mutableStateListOf<Group>()
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
            // 1. Listen to Tasks
            db.collection("tasks")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {
                        val allTasks = snapshots.toObjects(TaskEntity::class.java)
                        for (i in allTasks.indices) allTasks[i].id = snapshots.documents[i].id
                        processTasks(allTasks)
                    }
                }

            // 2. Listen to Leaderboard
            fetchLeaderboard()

            // 3. Listen to Public Groups
            fetchPublicGroups()

            // 4. Listen to My Groups
            fetchMyGroups()
        }
    }

    // --- GROUP LOGIC ---

    private fun fetchPublicGroups() {
        db.collection("groups")
            .whereEqualTo("public", true) // Only public groups
            .limit(20)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    allPublicGroups.clear()
                    allPublicGroups.addAll(snapshots.toObjects(Group::class.java))
                }
            }
    }

    private fun fetchMyGroups() {
        if (currentUserId == null) return
        // Firestore doesn't support "array-contains" AND "whereEqualTo" easily in one query
        // For MVP, we just filter client side or fetch groups where members contains ID
        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    myGroups.clear()
                    myGroups.addAll(snapshots.toObjects(Group::class.java))
                }
            }
    }

    fun createGroup(name: String, description: String, goal: Int, isPublic: Boolean) {
        if (currentUserId == null) return

        // Generate unique 6-char code
        val uniqueCode = UUID.randomUUID().toString().substring(0, 6).uppercase()

        val newGroup = Group(
            id = "", // Firestore will generate
            name = name,
            description = description,
            code = uniqueCode,
            dailyGoalMinutes = goal,
            isPublic = isPublic,
            adminId = currentUserId,
            members = listOf(currentUserId) // Creator is first member
        )

        db.collection("groups").add(newGroup)
    }

    fun joinGroup(group: Group) {
        if (currentUserId == null) return
        if (group.members.contains(currentUserId)) return // Already in it

        // Add user to members list
        // Note: We need the Document ID. In a real app, we map it.
        // For now, we assume we query it or it's passed.
        // Since 'Group' data class might not have the doc ID set from 'toObjects',
        // we usually need a manual mapping. But for this MVP, let's rely on matching Codes.

        db.collection("groups")
            .whereEqualTo("code", group.code)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Add user to the array
                    document.reference.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                }
            }
    }

    fun joinByCode(code: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (currentUserId == null) return

        db.collection("groups")
            .whereEqualTo("code", code.uppercase())
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    doc.reference.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                    onSuccess()
                } else {
                    onFailure()
                }
            }
    }

    // --- EXISTING LOGIC ---
    private fun fetchLeaderboard() {
        db.collection("leaderboard").orderBy("score", Query.Direction.DESCENDING).limit(20)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    leaderboard.clear()
                    leaderboard.addAll(snapshots.toObjects(UserScore::class.java))
                }
            }
    }

    private fun processTasks(allTasks: List<TaskEntity>) {
        val cutoff = System.currentTimeMillis() - 86400000
        activeTasks.clear(); historyTasks.clear()
        allTasks.sortedByDescending { it.date }.forEach { task ->
            if (!task.isCompleted || task.date > cutoff) activeTasks.add(task) else historyTasks.add(task)
        }
        recalculateStats(allTasks)
    }

    private fun recalculateStats(allTasks: List<TaskEntity>) {
        totalPoints = allTasks.filter { it.isCompleted }.size * 10
        updateCloudScore(totalPoints)

        val completedDates = allTasks.filter { it.isCompleted }
            .map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct().sortedDescending()

        if (completedDates.isEmpty()) { currentStreak = 0; return }
        var streak = 0; val today = LocalDate.now(); var checkDate = today
        if (!completedDates.contains(today)) checkDate = today.minusDays(1)
        while (completedDates.contains(checkDate)) { streak++; checkDate = checkDate.minusDays(1) }
        currentStreak = streak
    }

    private fun updateCloudScore(points: Int) {
        if (currentUserId == null) return
        val userScore = UserScore(currentUserId, currentUserName, points)
        db.collection("leaderboard").document(currentUserId).set(userScore)
    }

    fun toggleTheme() { isDarkTheme = !isDarkTheme }
    fun addTask(name: String, durationMinutes: Int) {
        if (currentUserId == null) return
        val newTask = TaskEntity(name = name, durationMinutes = durationMinutes, date = System.currentTimeMillis(), userId = currentUserId, isCompleted = false)
        db.collection("tasks").add(newTask)
    }
    fun markTaskCompleted(task: TaskEntity) { if (task.id.isNotEmpty()) db.collection("tasks").document(task.id).update("completed", !task.isCompleted) }

    fun addMockBlockedAppsIfEmpty() {
        if (blockedApps.isEmpty()) {
            blockedApps.addAll(listOf(BlockedApp("com.instagram.android", "Instagram"), BlockedApp("com.facebook.katana", "Facebook"), BlockedApp("com.twitter.android", "X / Twitter"), BlockedApp("com.snapchat.android", "Snapchat")))
        }
    }
    fun startFocus() { focusRunning = true; FocusState.isBlockingActive = true }
    fun stopFocus() { focusRunning = false; FocusState.isBlockingActive = false }
    fun completeFocusSecondTick() { if (focusRunning) focusSeconds++ }
}