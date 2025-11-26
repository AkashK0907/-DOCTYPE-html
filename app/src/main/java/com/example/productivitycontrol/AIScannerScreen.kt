package com.example.productivitycontrol

import android.content.Context
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.delay

@Composable
fun AIScannerScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    // AI State
    var isScanning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    // Result Data
    var detectedTags by remember { mutableStateOf(listOf<String>()) }
    var matchResult by remember { mutableStateOf<MatchResult?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. CAMERA PREVIEW
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(android.graphics.Color.BLACK)
                    scaleType = PreviewView.ScaleType.FILL_START
                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            }
        )

        // 2. UI OVERLAY (Top Bar & Scanner Frame)
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(Icons.Default.ArrowBack) { onBack() }
                Spacer(Modifier.width(16.dp))
                Text("AI Verifier", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }

            // Center Focus Frame (Visual only)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(24.dp))
            ) {
                // Animated "Scanning" line could go here
                if (isScanning) {
                    CircularProgressIndicator(
                        color = Color(0xFF00E676),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Bottom Control
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (!isScanning && !showResult) {
                    Button(
                        onClick = {
                            isScanning = true
                            captureAndAnalyze(context, cameraController, viewModel) { tags, match ->
                                detectedTags = tags
                                matchResult = match
                                isScanning = false
                                showResult = true
                            }
                        },
                        modifier = Modifier.size(80.dp).border(2.dp, Color.White, CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.2f))
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        // 3. CUSTOM GLASS RESULT POPUP (Replaces AlertDialog)
        AnimatedVisibility(
            visible = showResult,
            enter = slideInVertically { it }, // Slide up from bottom
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassResultSheet(
                tags = detectedTags,
                match = matchResult,
                onClose = { showResult = false; if(matchResult?.success == true) onBack() }
            )
        }
    }
}

// --- CUSTOM RESULT UI ---
data class MatchResult(val success: Boolean, val message: String, val taskName: String?)

@Composable
fun GlassResultSheet(tags: List<String>, match: MatchResult?, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color(0xFF1C1C1E).copy(alpha = 0.95f)) // Dark Glass
            .border(
                BorderStroke(1.dp, Brush.verticalGradient(listOf(Color.White.copy(0.3f), Color.Transparent))),
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Status Icon
            Icon(
                imageVector = if (match?.success == true) Icons.Default.CheckCircle else Icons.Default.Radar,
                contentDescription = null,
                tint = if (match?.success == true) Color(0xFF00E676) else Color(0xFFFF3B30), // Green or Red
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Main Title
            Text(
                text = if (match?.success == true) "Task Verified" else "No Match Found",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Message
            Text(
                text = match?.message ?: "Analyzing...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // AI Analysis Tags (The "Brain" part)
            Text("AI DETECTED:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.4f))
            Spacer(Modifier.height(8.dp))

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.take(5).forEach { tag ->
                    Surface(
                        color = Color.White.copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                    ) {
                        Text(
                            text = tag,
                            color = Color.White.copy(0.9f),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Close Button
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if(match?.success == true) "Complete Task" else "Try Again",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- AI LOGIC ---
// --- SUPERCHARGED AI LOGIC ---
// --- SUPERCHARGED AI LOGIC V2 (Fixes Code Detection) ---
private fun captureAndAnalyze(
    context: Context,
    cameraController: LifecycleCameraController,
    viewModel: AppViewModel,
    onResult: (List<String>, MatchResult) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                // 1. LOWER THRESHOLD: Catch everything
                val options = ImageLabelerOptions.Builder()
                    .setConfidenceThreshold(0.3f)
                    .build()
                val labeler = ImageLabeling.getClient(options)

                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        // 2. Get tags
                        val tags = labels.map { it.text.lowercase() }

                        // 3. THE EXPANDED DICTIONARY
                        val synonymMap = mapOf(
                            // TECH / CODING
                            "computer" to listOf("code", "coding", "dev", "program", "work"),
                            "laptop" to listOf("code", "coding", "dev", "work", "study"),
                            "monitor" to listOf("code", "coding", "work", "gaming"),
                            "keyboard" to listOf("code", "coding", "type", "writing"),
                            "screen" to listOf("code", "work", "watch"),
                            "technology" to listOf("code", "coding", "work"),
                            "electronic device" to listOf("code", "coding"),
                            "screenshot" to listOf("code", "coding", "study", "read"), // Fix for code
                            "multimedia" to listOf("code", "coding", "watch"),

                            // BOOKS / STUDY
                            "book" to listOf("read", "reading", "study", "learning", "homework"),
                            "paper" to listOf("read", "study", "write"),
                            "text" to listOf("read", "study", "code", "coding"),
                            "document" to listOf("read", "study", "work"),

                            // WEIRD AI HALLUCINATIONS (Fixes)
                            "musical instrument" to listOf("code", "coding", "music"), // Code lines look like sheet music/strings
                            "furniture" to listOf("work", "study", "clean"), // Desk/Table

                            // LIFESTYLE
                            "cup" to listOf("drink", "water", "coffee"),
                            "bottle" to listOf("drink", "water", "gym"),
                            "dumbbell" to listOf("workout", "gym", "exercise", "fitness"),
                            "food" to listOf("eat", "lunch", "dinner")
                        )

                        val tasks = viewModel.activeTasks
                        var matchedTask: TaskEntity? = null

                        // 4. Matching Logic
                        for (tag in tags) {
                            // A. Direct Match
                            matchedTask = tasks.find { it.name.lowercase().contains(tag) }

                            // B. Synonym Match
                            if (matchedTask == null && synonymMap.containsKey(tag)) {
                                val possibleKeywords = synonymMap[tag]!!
                                matchedTask = tasks.find { task ->
                                    val tName = task.name.lowercase()
                                    possibleKeywords.any { keyword -> tName.contains(keyword) }
                                }
                            }

                            if (matchedTask != null) break
                        }

                        // 5. Result
                        if (matchedTask != null) {
                            viewModel.markTaskCompleted(matchedTask)
                            onResult(tags, MatchResult(true, "AI verified: '${matchedTask.name}'", matchedTask.name))
                        } else {
                            onResult(tags, MatchResult(false, "Detected: ${tags.take(3).joinToString()}. \nNo matching task found.", null))
                        }

                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        onResult(emptyList(), MatchResult(false, "Scan Failed.", null))
                        imageProxy.close()
                    }
            }
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("Camera", "Error", exception)
        }
    })
}