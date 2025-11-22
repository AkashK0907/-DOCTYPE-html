package com.example.productivitycontrol

import com.google.firebase.firestore.Exclude

// This is the shape of our data on the Cloud.
// We must remove all Room (DAO, @Entity, etc.) definitions.
data class TaskEntity(
    // @Exclude is needed so Firebase doesn't try to save the ID inside the document.
    @get:Exclude @set:Exclude var id: String = "",
    val name: String = "",
    val durationMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val date: Long = System.currentTimeMillis(), // We keep the date for streak/heatmap
    val userId: String = "" // Essential for security rules (knowing who owns the task)
)