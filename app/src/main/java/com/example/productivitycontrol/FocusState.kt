package com.example.productivitycontrol

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object FocusState {
    // This is a global switch.
    // When TRUE, the Blocker Service will start attacking apps.
    var isBlockingActive by mutableStateOf(false)

    // List of "Bad Apps" (Package names)
    val blockedPackages = listOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.facebook.katana",
        "com.twitter.android",
        "com.snapchat.android",
        "com.google.android.youtube"
    )
}