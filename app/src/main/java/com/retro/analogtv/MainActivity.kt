package com.retro.analogtv

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shaderCode = resources.openRawResource(R.raw.analog_vhs_fragment)
            .bufferedReader()
            .use { it.readText() }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                LiveCamcorderScreen(shaderCode = shaderCode)
            }
        }
    }
}

@Composable
fun LiveCamcorderScreen(shaderCode: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPerm = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPerm) {
            permLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var params by remember { mutableStateOf(AnalogTvPresets.looks.first()) }
    var activeTab by remember { mutableStateOf(0) } // 0=VCR, 1=RF, 2=Tube/OSD, 3=CRT, 4=Tapes/Audio

    // Source & Orientation state
    // sensorRot = 1 (90 CW) fixes the 90-degree anticlockwise rear camera orientation in portrait!
    var useFrontCamera by remember { mutableStateOf(false) }
    var sensorRot by remember { mutableStateOf(1) } // 0=0, 1=90 CW, 2=180, 3=270 CW
    var sourceMode by remember { mutableStateOf(0) } // 0=Camera/Video, 1=TestCard, 2=Synthwave
    var importedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var oesSurfaceTexture by remember { mutableStateOf<android.graphics.SurfaceTexture?>(null) }

    // VCR Transport & Stompboxes
    var transportMode by remember { mutableStateOf(0) } // 0=Play, 1=Pause, 2=Cue, 3=Rev
    var stompScramble by remember { mutableStateOf(false) }
    var stompImpulse by remember { mutableStateOf(false) }
    var crtPowerOn by remember { mutableStateOf(true) }
    var wipeEnabled by remember { mutableStateOf(false) }
    var wipeAutoSweep by remember { mutableStateOf(true) }
    var wipePercent by remember { mutableStateOf(50f) }

    // Video Recording & Recorded Tapes Vault
    val mp4Recorder = remember { VhsMp4Recorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recElapsedSec by remember { mutableStateOf(0) }
    val recordedTapes = remember { mutableStateListOf<RecordedTapeItem>() }
    var latestRecordedTape by remember { mutableStateOf<RecordedTapeItem?>(null) }
    var playingTapeItem by remember { mutableStateOf<RecordedTapeItem?>(null) }
    var showTapesDialog by remember { mutableStateOf(false) }

    var activeRenderer by remember { mutableStateOf<LiveCamcorderGlRenderer?>(null) }

    // Pre-populate any existing MP4 recordings from app Movies directory
    LaunchedEffect(Unit) {
        val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
        dir?.listFiles()?.filter { it.extension.lowercase() == "mp4" && it.length() > 512L }
            ?.sortedByDescending { it.lastModified() }
            ?.forEach { file ->
                recordedTapes.add(
                    RecordedTapeItem(
                        file = file,
                        title = file.name,
                        presetBadge = "SAVED VHS MP4",
                        durationSec = 0,
                        isVideo = true
                    )
                )
            }
    }

    // Recording elapsed timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recElapsedSec = 0
            while (isRecording) {
                delay(1000L)
                recElapsedSec += 1
            }
        } else {
            recElapsedSec = 0
        }
    }

    // Sync state to OpenGL Renderer
    LaunchedEffect(
        params, sensorRot, useFrontCamera, sourceMode, transportMode,
        stompScramble, stompImpulse, crtPowerOn, wipeEnabled, wipeAutoSweep, wipePercent, isRecording
    ) {
        activeRenderer?.let { r ->
            r.params = params
            r.sensorRot = sensorRot
            r.mirrorX = if (useFrontCamera && importedVideoUri == null) 1 else 0
            r.sourceMode = sourceMode
            r.transportMode = transportMode
            r.stompScramble = if (stompScramble) 1f else 0f
            r.stompImpulse = if (stompImpulse) 1f else 0f
            r.crtPowerOn = crtPowerOn
            r.wipeEnabled = wipeEnabled
            r.wipeAutoSweep = wipeAutoSweep
            r.wipePercent = wipePercent
            r.isRecordingVideo = isRecording
        }
    }

    // Bind either CameraX or Imported Gallery Video to the OES SurfaceTexture
    LaunchedEffect(hasCameraPerm, oesSurfaceTexture, useFrontCamera, importedVideoUri, sourceMode) {
        val st = oesSurfaceTexture ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        if (sourceMode != 0) {
            try {
                mediaPlayerRef?.pause()
            } catch (_: Exception) {
            }
            return@LaunchedEffect
        }

        if (importedVideoUri != null) {
            // Unbind camera and play imported video into OES SurfaceTexture
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (_: Exception) {
            }
            try {
                mediaPlayerRef?.release()
            } catch (_: Exception) {
            }
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(context, importedVideoUri!!)
                    setSurface(Surface(st))
                    isLooping = true
                    setOnPreparedListener { player ->
                        val vw = player.videoWidth
                        val vh = player.videoHeight
                        if (vw > 0 && vh > 0) {
                            st.setDefaultBufferSize(vw, vh)
                        }
                        sensorRot = 0 // Gallery videos via MediaPlayer already apply rotation metadata
                        player.start()
                    }
                    prepareAsync()
                }
                mediaPlayerRef = mp
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load video: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else if (hasCameraPerm) {
            try {
                mediaPlayerRef?.release()
                mediaPlayerRef = null
            } catch (_: Exception) {
            }
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    val executor = Executors.newSingleThreadExecutor()
                    preview.setSurfaceProvider(executor) { request ->
                        st.setDefaultBufferSize(request.resolution.width, request.resolution.height)
                        val surface = Surface(st)
                        request.provideSurface(surface, executor) {
                            surface.release()
                        }
                    }
                    val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                } catch (_: Exception) {
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Gallery Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            sourceMode = 0
            importedVideoUri = uri
            Toast.makeText(context, "Loaded video into VHS deck! Tap REC to save converted MP4.", Toast.LENGTH_LONG).show()
        }
    }

    fun toggleRecording() {
        if (!isRecording) {
            mp4Recorder.startRecording(
                presetName = params.presetName,
                onStarted = {
                    mainHandler.post {
                        isRecording = true
                        Toast.makeText(context, "Recording VHS MP4...", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { err ->
                    mainHandler.post {
                        isRecording = false
                        Toast.makeText(context, "Recorder error: $err", Toast.LENGTH_LONG).show()
                    }
                }
            )
        } else {
            isRecording = false
            mp4Recorder.stopRecording(
                presetName = params.presetName,
                badge = params.badge
            ) { item ->
                mainHandler.post {
                    if (item != null) {
                        recordedTapes.add(0, item)
                        latestRecordedTape = item
                        Toast.makeText(
                            context,
                            "Saved ${item.title} to Gallery (Movies/AnalogTV)!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Recording was too short to save.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun shareTapeFile(item: RecordedTapeItem) {
        val shareUri = item.galleryUri ?: VhsMp4Recorder.saveVideoToGallery(context, item.file)
        if (shareUri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (item.isVideo) "video/mp4" else "image/png"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share VHS Tape"))
        } else {
            Toast.makeText(context, "Saved at: ${item.file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. TOP APP BAR — Fixed single-line red "REC" button + Tapes Vault button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) Color(0xFFEF4444) else Color(0xFF10B981))
                    )
                    Text(
                        text = if (isRecording) {
                            "REC %02d:%02d [%s]".format(recElapsedSec / 60, recElapsedSec % 60, params.tapeSpeedMode)
                        } else {
                            "STBY [${params.tapeSpeedMode}] · ${params.presetName}"
                        },
                        color = if (isRecording) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "AnalogTV 3.3 · YIQ 629kHz & CRT Engine",
                    color = Color(0xFFF59E0B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tapes Vault Button
                OutlinedButton(
                    onClick = { showTapesDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "📼 Tapes (${recordedTapes.size})",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Sleek Single-Line Crimson Red REC Button (Issue #5 fixed!)
                Button(
                    onClick = { toggleRecording() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFF59E0B) else Color(0xFFDC2626)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isRecording) "⏹ STOP" else "● REC",
                        color = if (isRecording) Color(0xFF090A0F) else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 2. SOURCE PICKER & ORIENTATION BAR (Camera, Flip, Rotate 90, Open Video, Test Card, Synthwave)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121520), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF1E2436), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniChip(
                label = "📷 Live Cam",
                selected = sourceMode == 0 && importedVideoUri == null,
                onClick = {
                    importedVideoUri = null
                    sourceMode = 0
                    sensorRot = if (useFrontCamera) 3 else 1
                }
            )
            MiniChip(
                label = "⇄ Flip Cam",
                selected = useFrontCamera && sourceMode == 0 && importedVideoUri == null,
                onClick = {
                    importedVideoUri = null
                    sourceMode = 0
                    useFrontCamera = !useFrontCamera
                    sensorRot = if (useFrontCamera) 3 else 1
                }
            )
            MiniChip(
                label = "↻ Rotate ${sensorRot * 90}°",
                selected = false,
                onClick = {
                    sensorRot = (sensorRot + 1) % 4
                }
            )
            MiniChip(
                label = "📁 Open Video",
                selected = sourceMode == 0 && importedVideoUri != null,
                onClick = {
                    videoPickerLauncher.launch("video/*")
                }
            )
            MiniChip(
                label = "📺 Test Card",
                selected = sourceMode == 1,
                onClick = { sourceMode = 1 }
            )
            MiniChip(
                label = "🌆 '85 Synth",
                selected = sourceMode == 2,
                onClick = { sourceMode = 2 }
            )
        }

        // 3. CRT MONITOR VIEWFINDER (4:3 Classic CRT Aspect Ratio)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, Color(0xFF282E42), RoundedCornerShape(14.dp))
                .background(Color.Black)
        ) {
            if (hasCameraPerm || sourceMode != 0 || importedVideoUri != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        GLSurfaceView(ctx).apply {
                            setEGLContextClientVersion(3)
                            val renderer = LiveCamcorderGlRenderer(
                                fragmentShaderSource = shaderCode,
                                onSurfaceTextureReady = { st ->
                                    mainHandler.post {
                                        oesSurfaceTexture = st
                                    }
                                },
                                onVideoFrameCaptured = { bmp ->
                                    mp4Recorder.encodeFrame(bmp)
                                },
                                onSnapshotCaptured = { bmp ->
                                    val saved = VhsMp4Recorder.saveBitmapToGallery(ctx, bmp, params.presetName)
                                    mainHandler.post {
                                        if (saved != null) {
                                            recordedTapes.add(0, saved)
                                            Toast.makeText(
                                                ctx,
                                                "Saved PNG snapshot to Gallery (Pictures/AnalogTV)!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            )
                            activeRenderer = renderer
                            setRenderer(renderer)
                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                        }
                    }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CAMERA PERMISSION REQUIRED",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Camera Permission")
                    }
                }
            }

            // Before/After Split-Screen Wipe Controls Overlay
            if (wipeEnabled) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xCC090A0F))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("◀ Clean Source", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        MiniChip(
                            label = if (wipeAutoSweep) "⇄ Auto-Sweep: ON" else "⇄ Auto-Sweep: OFF",
                            selected = wipeAutoSweep,
                            onClick = { wipeAutoSweep = !wipeAutoSweep }
                        )
                        Text("AnalogTV 3.3 ▶", color = Color(0xFFF59E0B), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = wipePercent,
                        onValueChange = {
                            wipeAutoSweep = false
                            wipePercent = it
                        },
                        valueRange = 5f..95f,
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }

        // 4. NEWLY RECORDED TAPE BANNER (Play, Save to Gallery, Share) — Issue #4 solved!
        latestRecordedTape?.let { tape ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11221C), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✅ Saved: ${tape.title}",
                        color = Color(0xFF6EE7B7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "In Gallery (Movies/AnalogTV) · Tap Play to watch",
                        color = Color(0xFFA7F3D0),
                        fontSize = 10.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { playingTapeItem = tape },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("▶ Play", color = Color(0xFF052E16), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { shareTapeFile(tape) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("📤 Share", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        // 5. MONITOR TOOLS, VCR TRANSPORT & STOMPBOX PEDALS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniChip(
                label = "🌗 Before/After",
                selected = wipeEnabled,
                onClick = { wipeEnabled = !wipeEnabled }
            )
            MiniChip(
                label = "📸 Snap PNG",
                selected = false,
                onClick = { activeRenderer?.snapshotRequested = true }
            )
            MiniChip(
                label = "▶ Play",
                selected = transportMode == 0,
                onClick = {
                    transportMode = 0
                    try { mediaPlayerRef?.start() } catch (_: Exception) {}
                }
            )
            MiniChip(
                label = "⏸ Pause Bar",
                selected = transportMode == 1,
                onClick = {
                    transportMode = if (transportMode == 1) 0 else 1
                    try {
                        if (transportMode == 1) mediaPlayerRef?.pause() else mediaPlayerRef?.start()
                    } catch (_: Exception) {}
                }
            )
            MiniChip(
                label = "⏩ Cue ×5",
                selected = transportMode == 2,
                onClick = { transportMode = if (transportMode == 2) 0 else 2 }
            )
            MiniChip(
                label = "⏪ Rev ×5",
                selected = transportMode == 3,
                onClick = { transportMode = if (transportMode == 3) 0 else 3 }
            )
            MiniChip(
                label = "🧲 Degauss",
                selected = false,
                onClick = { activeRenderer?.degaussPhase = 1.8f }
            )
            MiniChip(
                label = "⚡ Scramble",
                selected = stompScramble,
                onClick = { stompScramble = !stompScramble }
            )
            MiniChip(
                label = "💥 Dropouts",
                selected = stompImpulse,
                onClick = { stompImpulse = !stompImpulse }
            )
            MiniChip(
                label = if (crtPowerOn) "🔌 CRT ON" else "🔌 CRT OFF",
                selected = !crtPowerOn,
                onClick = { crtPowerOn = !crtPowerOn }
            )
        }

        // 6. ONE-TAP LOOKS STRIP (All 10 Presets from the Web Studio!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnalogTvPresets.looks.forEach { look ->
                val isSel = params.id == look.id
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Color(0xFF2A2112) else Color(0xFF121520))
                        .border(
                            1.dp,
                            if (isSel) Color(0xFFF59E0B) else Color(0xFF22283A),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            params = look.copy(
                                osdEnabled = params.osdEnabled,
                                osdVobBox = params.osdVobBox,
                                osdTitle = params.osdTitle,
                                zoomRatio = params.zoomRatio
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = look.presetName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        text = look.badge,
                        color = if (isSel) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // 7. 5-TAB HARDWARE CONTROL RACK (Matches the Web Interface!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121520), RoundedCornerShape(10.dp))
                .padding(4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                "📼 1. VCR & Tape",
                "📡 2. RF & Signal",
                "🎥 3. Tube & OSD",
                "📺 4. CRT & Mask",
                "🎞️ 5. Tapes (${recordedTapes.size})"
            )
            tabs.forEachIndexed { idx, title ->
                MiniChip(
                    label = title,
                    selected = activeTab == idx,
                    onClick = { activeTab = idx }
                )
            }
        }

        // TAB PANELS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121520), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1E2436), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // TAB 1: VCR & TAPE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VCR Deck Format & 4fsc TBC", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        MiniChip(
                            label = if (params.tbcLocked) "TBC: ON (4fsc)" else "TBC: OFF (Raw)",
                            selected = params.tbcLocked,
                            onClick = { params = params.copy(tbcLocked = !params.tbcLocked) }
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "vhs_sp" to "VHS SP",
                            "vhs_ep" to "VHS EP",
                            "betamax" to "Betamax",
                            "svhs" to "S-VHS",
                            "hi8" to "Hi8",
                            "pxl2000" to "PXL-2000"
                        ).forEach { (code, name) ->
                            MiniChip(
                                label = name,
                                selected = params.vcrFormat == code,
                                onClick = {
                                    params = when (code) {
                                        "vhs_ep" -> params.copy(vcrFormat = code, tapeSpeedMode = "EP", chromaBleed = 0.90f, tapeNoise = 0.55f, flutter = 0.35f)
                                        "betamax" -> params.copy(vcrFormat = code, tapeSpeedMode = "SP", chromaBleed = 0.48f, tapeNoise = 0.18f, edgeRinging = 0.62f)
                                        "svhs", "hi8" -> params.copy(vcrFormat = code, tapeSpeedMode = "SP", chromaBleed = 0.28f, tapeNoise = 0.12f, edgeRinging = 0.65f)
                                        "pxl2000" -> params.copy(vcrFormat = code, tapeSpeedMode = "EP", standard = 3, phosphorMode = 2, chromaBleed = 0f, tapeNoise = 0.38f)
                                        else -> params.copy(vcrFormat = code, tapeSpeedMode = "SP", chromaBleed = 0.68f, tapeNoise = 0.24f, flutter = 0.16f)
                                    }
                                }
                            )
                        }
                    }

                    // Camcorder Zoom Rocker & SP/LP/EP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ZOOM ${"%.1f".format(params.zoomRatio)}x", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Slider(
                            value = params.zoomRatio,
                            onValueChange = { params = params.copy(zoomRatio = it) },
                            valueRange = 1.0f..4.0f,
                            modifier = Modifier.weight(1f)
                        )
                        listOf("SP", "LP", "EP").forEach { spd ->
                            MiniChip(
                                label = spd,
                                selected = params.tapeSpeedMode == spd,
                                onClick = {
                                    params = when (spd) {
                                        "EP" -> params.copy(tapeSpeedMode = "EP", chromaBleed = 0.90f, tapeNoise = 0.55f, flutter = 0.35f)
                                        "LP" -> params.copy(tapeSpeedMode = "LP", chromaBleed = 0.78f, tapeNoise = 0.38f, flutter = 0.24f)
                                        else -> params.copy(tapeSpeedMode = "SP", chromaBleed = 0.66f, tapeNoise = 0.22f, flutter = 0.15f)
                                    }
                                }
                            )
                        }
                    }

                    SliderControl("VHS Chroma Bleed (629 kHz + Group Delay)", params.chromaBleed) { params = params.copy(chromaBleed = it) }
                    SliderControl("Luma 2 MHz Peaking & Overshoot Ringing", params.edgeRinging) { params = params.copy(edgeRinging = it) }
                    SliderControl("Flagging (Top-of-Frame H-PLL Tension Skew)", params.flagging) { params = params.copy(flagging = it) }
                    SliderControl("Tape Oxide Dropouts (1H DOC Substitution)", params.tapeNoise) { params = params.copy(tapeNoise = it) }
                    SliderControl("Per-Line H-Sync Timebase Jitter", params.flutter) { params = params.copy(flutter = it) }
                    SliderControl("Rotary Head-Switch & Pause Tracking", params.tracking) { params = params.copy(tracking = it) }
                }

                1 -> {
                    // TAB 2: RF & COMPOSITE SIGNAL
                    Text("Broadcast Colour Standard & Mains Hum", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "NTSC-M (525)",
                            1 to "PAL-B/G (625)",
                            2 to "SECAM-L (FM)",
                            3 to "405-Line Mono"
                        ).forEach { (std, label) ->
                            MiniChip(
                                label = label,
                                selected = params.standard == std,
                                onClick = { params = params.copy(standard = std) }
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Mains Hum:", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        listOf(0f to "OFF", 0.5f to "50 Hz", 0.9f to "60 Hz").forEach { (humVal, lbl) ->
                            MiniChip(
                                label = lbl,
                                selected = kotlin.math.abs(params.mainsHum - humVal) < 0.15f,
                                onClick = { params = params.copy(mainsHum = humVal) }
                            )
                        }
                    }

                    SliderControl("Subcarrier Dot Crawl & Cross-Colour Moiré", params.dotCrawl) { params = params.copy(dotCrawl = it) }
                    SliderControl("RF Snow / IF Strip Noise (Colour Killer >80%)", params.snow) { params = params.copy(snow = it) }
                    SliderControl("Multipath RF Ghosting (Delayed Echo)", params.ghosting) { params = params.copy(ghosting = it) }
                    SliderControl("PAL Hanover Bars / SECAM Fire Intensity", params.hanover) { params = params.copy(hanover = it) }
                    SliderControl("V-Hold Sync Drift (Vertical Roll)", params.vHold) { params = params.copy(vHold = it) }
                }

                2 -> {
                    // TAB 3: CAMERA TUBE & CAMCORDER OSD TITLER
                    Text("Broadcast Camera Tube Model", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "Vidicon (Comet Lag)",
                            1 to "Plumbicon (3-Tube)",
                            2 to "Image Orthicon (Halo)",
                            3 to "Early CCD"
                        ).forEach { (tm, label) ->
                            MiniChip(
                                label = label,
                                selected = params.tubeMode == tm,
                                onClick = {
                                    params = when (tm) {
                                        0 -> params.copy(tubeMode = tm, tubeLag = 0.68f)
                                        1 -> params.copy(tubeMode = tm, tubeLag = 0.18f, tubeReg = 0.42f)
                                        2 -> params.copy(tubeMode = tm, tubeLag = 0.55f)
                                        else -> params.copy(tubeMode = tm, tubeLag = 0.08f)
                                    }
                                }
                            )
                        }
                    }
                    SliderControl("Camera Tube Persistence Lag (Comet Trails)", params.tubeLag) { params = params.copy(tubeLag = it) }
                    SliderControl("3-Tube R/B Prism Registration Drift", params.tubeReg) { params = params.copy(tubeReg = it) }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E2436)))
                    Text("Panasonic PK-958 / Fujitsu MB88303 OSD Titler", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniChip(
                            label = if (params.osdEnabled) "OSD: ON" else "OSD: OFF",
                            selected = params.osdEnabled,
                            onClick = { params = params.copy(osdEnabled = !params.osdEnabled) }
                        )
                        MiniChip(
                            label = if (params.osdVobBox) "VOB Black Box: ON" else "VOB Black Box: OFF",
                            selected = params.osdVobBox,
                            onClick = { params = params.copy(osdVobBox = !params.osdVobBox) }
                        )
                    }
                    OutlinedTextField(
                        value = params.osdTitle,
                        onValueChange = { params = params.copy(osdTitle = it.uppercase().take(22)) },
                        label = { Text("Pre-Encoder Camcorder OSD Stamp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                3 -> {
                    // TAB 4: CRT DISPLAY & PHOSPHOR CHEMISTRY
                    Text("CRT Phosphor Chemistry", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "P22 Consumer Colour",
                            1 to "P31 Scope Green",
                            2 to "P4 B&W White",
                            3 to "P3 Amber Monitor"
                        ).forEach { (pm, label) ->
                            MiniChip(
                                label = label,
                                selected = params.phosphorMode == pm,
                                onClick = { params = params.copy(phosphorMode = pm) }
                            )
                        }
                    }

                    Text("Convergence Mask Geometry", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "Aperture Grille",
                            1 to "Shadow Mask",
                            2 to "Slot Mask"
                        ).forEach { (mm, label) ->
                            MiniChip(
                                label = label,
                                selected = params.maskMode == mm,
                                onClick = { params = params.copy(maskMode = mm) }
                            )
                        }
                    }

                    SliderControl("CRT Scanlines & Mask Pitch", params.scanlines) { params = params.copy(scanlines = it) }
                    SliderControl("Faceplate Barrel Curvature & Vignette", params.curvature) { params = params.copy(curvature = it) }
                    SliderControl("2D Glass Halation & Phosphor Bloom", params.bloom) { params = params.copy(bloom = it) }
                    SliderControl("Vintage CRT S-Curve Contrast & Chroma Punch", params.crtContrast) { params = params.copy(crtContrast = it) }
                }

                else -> {
                    // TAB 5: RECORDED TAPES VAULT & EXPORT
                    Text("Recorded VHS Tapes & Frame Snapshots", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (recordedTapes.isEmpty()) {
                        Text(
                            "No recorded tapes yet. Tap the red ● REC button at the top right to record live VHS video, or tap 📸 Snap PNG.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    } else {
                        recordedTapes.forEach { item ->
                            RecordedTapeRow(
                                item = item,
                                onPlay = { playingTapeItem = item },
                                onSaveGallery = {
                                    if (item.isVideo) {
                                        val uri = VhsMp4Recorder.saveVideoToGallery(context, item.file)
                                        Toast.makeText(
                                            context,
                                            if (uri != null) "Saved to Gallery (Movies/AnalogTV)!" else "Already in storage: ${item.file.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onShare = { shareTapeFile(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Recorded Tapes List (opened from top bar "📼 Tapes (N)")
    if (showTapesDialog) {
        Dialog(onDismissRequest = { showTapesDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121520), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF2E364F), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📼 Recorded VHS Tapes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    TextButton(onClick = { showTapesDialog = false }) {
                        Text("Close", color = Color(0xFFFBBF24))
                    }
                }
                if (recordedTapes.isEmpty()) {
                    Text(
                        "Tap the red ● REC button in the top right to record a VHS video clip. All recordings are automatically saved to your phone's Gallery (Movies/AnalogTV) and listed here for instant playback.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recordedTapes.forEach { item ->
                            RecordedTapeRow(
                                item = item,
                                onPlay = {
                                    showTapesDialog = false
                                    playingTapeItem = item
                                },
                                onSaveGallery = {
                                    if (item.isVideo) {
                                        val uri = VhsMp4Recorder.saveVideoToGallery(context, item.file)
                                        Toast.makeText(
                                            context,
                                            if (uri != null) "Saved copy to Gallery (Movies/AnalogTV)!" else "Saved: ${item.file.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onShare = { shareTapeFile(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // In-App VHS Video Player Modal Dialog
    playingTapeItem?.let { tape ->
        Dialog(onDismissRequest = { playingTapeItem = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1018), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📼 ${tape.title}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { playingTapeItem = null }) {
                        Text("✖ Close", color = Color(0xFFFBBF24))
                    }
                }

                if (tape.isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoPath(tape.file.absolutePath)
                                    val mc = MediaController(ctx)
                                    mc.setAnchorView(this)
                                    setMediaController(mc)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val uri = VhsMp4Recorder.saveVideoToGallery(context, tape.file)
                            Toast.makeText(
                                context,
                                if (uri != null) "Saved to Phone Gallery (Movies/AnalogTV)!" else "File in Gallery!",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("💾 Save to Gallery", color = Color(0xFF090A0F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { shareTapeFile(tape) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📤 Share Video", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordedTapeRow(
    item: RecordedTapeItem,
    onPlay: () -> Unit,
    onSaveGallery: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0D14), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF22283A), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.presetBadge} · Movies/AnalogTV",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (item.isVideo) {
                MiniChip(label = "▶ Play", selected = true, onClick = onPlay)
                MiniChip(label = "💾 Save", selected = false, onClick = onSaveGallery)
            }
            MiniChip(label = "📤 Share", selected = false, onClick = onShare)
        }
    }
}

@Composable
fun MiniChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF3B2A10) else Color(0xFF1A1E2E))
            .border(
                1.dp,
                if (selected) Color(0xFFF59E0B) else Color(0xFF2E354B),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFFFBBF24) else Color(0xFFE2E8F0),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun SliderControl(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color(0xFFE2E4EC), fontSize = 11.sp)
            Text("${(value * 100).toInt()}%", color = Color(0xFFFFB300), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.height(26.dp)
        )
    }
}