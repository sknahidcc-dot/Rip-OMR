package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCardBorder
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.OmrViewModel
import java.io.InputStream
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: OmrViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isProcessing by viewModel.isProcessingScan.collectAsStateWithLifecycle()
    val selectedExam by viewModel.selectedExam.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.scanImage(bitmap)
                }
            } catch (e: Exception) {
                // Handled in ViewModel
            }
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AmoledBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                // Live Camera Preview
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }
                )

                // Laser scan line animation
                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                val scanLinePosition by infiniteTransition.animateFloat(
                    initialValue = 0.15f,
                    targetValue = 0.85f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scanLine"
                )

                // A4 guide frame overlay with 4 corner brackets
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // A4 aspect ratio: 210 / 297 = 0.707
                    val frameWidth = canvasWidth * 0.84f
                    val frameHeight = frameWidth / 0.707f
                    val frameLeft = (canvasWidth - frameWidth) / 2f
                    val frameTop = (canvasHeight - frameHeight) / 2f - 20f

                    // Semi-transparent outer vignette
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        size = size
                    )

                    // Clear A4 center window
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(frameLeft, frameTop),
                        size = Size(frameWidth, frameHeight),
                        cornerRadius = CornerRadius(16f, 16f),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                    )

                    // Neon guide border
                    drawRoundRect(
                        color = CyanAccent.copy(alpha = 0.7f),
                        topLeft = Offset(frameLeft, frameTop),
                        size = Size(frameWidth, frameHeight),
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 4 Corner Alignment Marker indicator brackets
                    val bracketLen = 28.dp.toPx()
                    val strokeW = 4.dp.toPx()
                    val strokeCol = CyanAccent

                    // Top-Left corner
                    drawLine(strokeCol, Offset(frameLeft, frameTop), Offset(frameLeft + bracketLen, frameTop), strokeW)
                    drawLine(strokeCol, Offset(frameLeft, frameTop), Offset(frameLeft, frameTop + bracketLen), strokeW)

                    // Top-Right corner
                    drawLine(strokeCol, Offset(frameLeft + frameWidth, frameTop), Offset(frameLeft + frameWidth - bracketLen, frameTop), strokeW)
                    drawLine(strokeCol, Offset(frameLeft + frameWidth, frameTop), Offset(frameLeft + frameWidth, frameTop + bracketLen), strokeW)

                    // Bottom-Left corner
                    drawLine(strokeCol, Offset(frameLeft, frameTop + frameHeight), Offset(frameLeft + bracketLen, frameTop + frameHeight), strokeW)
                    drawLine(strokeCol, Offset(frameLeft, frameTop + frameHeight), Offset(frameLeft, frameTop + frameHeight - bracketLen), strokeW)

                    // Bottom-Right corner
                    drawLine(strokeCol, Offset(frameLeft + frameWidth, frameTop + frameHeight), Offset(frameLeft + frameWidth - bracketLen, frameTop + frameHeight), strokeW)
                    drawLine(strokeCol, Offset(frameLeft + frameWidth, frameTop + frameHeight), Offset(frameLeft + frameWidth, frameTop + frameHeight - bracketLen), strokeW)

                    // Moving laser scanning line
                    val laserY = frameTop + frameHeight * scanLinePosition
                    drawLine(
                        color = CyanAccent.copy(alpha = 0.85f),
                        start = Offset(frameLeft + 4.dp.toPx(), laserY),
                        end = Offset(frameLeft + frameWidth - 4.dp.toPx(), laserY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            } else {
                // Permission Denied state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ক্যামেরা পারমিশন প্রয়োজন",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ওএমআর শিট স্ক্যান করতে ক্যামেরার অনুমোদন দিন অথবা গ্যালারি থেকে ছবি সিলেক্ট করুন।",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                contentColor = AmoledBlack
                            )
                        ) {
                            Text("অনুমোদন দিন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top Overlay Bar (Back, Active Exam Title, Flash)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AmoledBlack.copy(alpha = 0.6f))
                        .border(1.dp, AmoledCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                // Active Exam tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmoledBlack.copy(alpha = 0.7f))
                        .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = selectedExam?.name ?: "পরীক্ষা নির্বাচন করুন",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                // Flash toggle
                IconButton(
                    onClick = {
                        val currentCam = camera
                        if (currentCam != null && currentCam.cameraInfo.hasFlashUnit()) {
                            isFlashOn = !isFlashOn
                            currentCam.cameraControl.enableTorch(isFlashOn)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AmoledBlack.copy(alpha = 0.6f))
                        .border(1.dp, AmoledCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch",
                        tint = if (isFlashOn) CyanAccent else TextSecondary
                    )
                }
            }

            // Bottom Scanning Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Instructions hint pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmoledBlack.copy(alpha = 0.75f))
                        .border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "শিটের ৪টি কোণার কালো মার্কার ফ্রেমের মধ্যে রাখুন",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons Row: Gallery, Shutter, Sample Test
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Gallery Picker Button
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AmoledBlack.copy(alpha = 0.7f))
                            .border(1.dp, AmoledCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Shutter Button (Capture)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AmoledBlack.copy(alpha = 0.5f))
                            .border(3.dp, CyanAccent, CircleShape)
                            .clickable(enabled = !isProcessing) {
                                imageCapture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                            val buffer = imageProxy.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            imageProxy.close()

                                            // Rotate bitmap to match camera preview orientation
                                            val finalBitmap = if (rotationDegrees != 0) {
                                                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                                                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                                            } else {
                                                rawBitmap
                                            }

                                            viewModel.scanImage(finalBitmap)
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            exception.printStackTrace()
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyanAccent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Capture",
                                tint = AmoledBlack,
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    // Sample Sheet Demo Button
                    IconButton(
                        onClick = {
                            viewModel.testWithSampleSheet(fillSample = true, sampleRoll = "1248")
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AmoledBlack.copy(alpha = 0.7f))
                            .border(1.dp, EmeraldPass.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Sample Test",
                            tint = EmeraldPass,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Processing indicator overlay
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AmoledBlack.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurface)
                            .border(1.dp, CyanAccent, RoundedCornerShape(16.dp))
                            .padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyanAccent,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ওএমআর শিট বিশ্লেষণ হচ্ছে...",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "মার্কার শনাক্তকরণ ও উত্তর যাচাই চলছে",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
