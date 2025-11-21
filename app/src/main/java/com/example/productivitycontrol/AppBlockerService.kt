package com.example.productivitycontrol

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.accessibilityservice.AccessibilityServiceInfo

class AppBlockerService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Configure the service
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!FocusState.isBlockingActive) return

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (FocusState.blockedPackages.contains(packageName)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                Toast.makeText(applicationContext, "🚫 BLOCKED! Get back to work!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onInterrupt() { }
}