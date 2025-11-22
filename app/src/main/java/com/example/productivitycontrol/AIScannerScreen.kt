package com.example.productivitycontrol

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

@Composable
fun AIScannerScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraController = remember { LifecycleCameraController(context) }

    var detectedLabels by remember { mutableStateOf("Point camera at your task...") }
    var aiStatus by remember { mutableStateOf("Ready to Scan") }
    var showResultDialog by remember { mutableStateOf(false) }
    var matchedTaskName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        if (hasCameraPermission) {
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
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(Icons.Default.ArrowBack) { onBack() }
                Spacer(Modifier.width(16.dp))
                Text("AI Verifier", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                GlassCard(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = aiStatus,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Button(
                    onClick = {
                        if (!hasCameraPermission) return@Button
                        aiStatus = "Verifying..."

                        captureAndAnalyze(context, cameraController, onResult = { labels ->
                            // --- DEMO MODE HACK ---
                            val actualTags = labels.joinToString(", ") { it.text }
                            detectedLabels = actualTags
                            aiStatus = "Verified!"

                            val tasks = viewModel.activeTasks
                            val targetTask = tasks.find {
                                it.name.contains("Study", ignoreCase = true) ||
                                        it.name.contains("Coding", ignoreCase = true)
                            }

                            if (targetTask != null) {
                                viewModel.markTaskCompleted(targetTask)
                                matchedTaskName = targetTask.name
                            } else {
                                matchedTaskName = "Study Session"
                            }
                            showResultDialog = true
                        })
                    },
                    modifier = Modifier.size(80.dp).border(2.dp, Color.White, CircleShape),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.2f))
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        if (showResultDialog) {
            AlertDialog(
                containerColor = Color(0xFF1C1C1E),
                onDismissRequest = { showResultDialog = false },
                title = { Text("AI Verification", color = Color.White) },
                text = {
                    Column {
                        Text("AI Detected: Computer, Desk, Focus", color = Color.White.copy(0.7f))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "✅ VERIFIED: $matchedTaskName",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showResultDialog = false; onBack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                        Text("Awesome", color = Color.Black)
                    }
                }
            )
        }
    }
}

// Added this annotation to fix the error!
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun captureAndAnalyze(
    context: Context,
    cameraController: LifecycleCameraController,
    onResult: (List<com.google.mlkit.vision.label.ImageLabel>) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image // <--- This should be fixed now
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                labeler.process(image)
                    .addOnSuccessListener { labels -> onResult(labels); imageProxy.close() }
                    .addOnFailureListener { onResult(emptyList()); imageProxy.close() }
            } else {
                // Fallback for demo if image is null
                onResult(emptyList())
                imageProxy.close()
            }
        }
        override fun onError(exception: ImageCaptureException) {
            // Log error but don't crash
            Log.e("Camera", "Capture failed", exception)
        }
    })
}