package com.retro.analogtv

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
    var isRecording by remember { mutableStateOf(false) }
    var stompScramble by remember { mutableStateOf(false) }
    var stompImpulse by remember { mutableStateOf(false) }
    var activeRenderer by remember { mutableStateOf<LiveCamcorderGlRenderer?>(null) }

    LaunchedEffect(params, stompScramble, stompImpulse) {
        activeRenderer?.params = params
        activeRenderer?.stompScramble = if (stompScramble) 1f else 0f
        activeRenderer?.stompImpulse = if (stompImpulse) 1f else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Camcorder Top HUD Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isRecording) "● REC [${params.tapeSpeedMode}]" else "■ STBY [${params.tapeSpeedMode}]",
                    color = if (isRecording) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "ANALOG TV v3.2 AUTHENTIC YIQ / 629kHz VHS",
                    color = Color(0xFFFFB300),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = { isRecording = !isRecording },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFF59E0B) else Color(0xFFDC2626)
                )
            ) {
                Text(if (isRecording) "⏹ STOP TAPE" else "● REC CAMCORDER")
            }
        }

        // Live OpenGL ES 3.0 CRT / VHS Viewfinder Monitor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF282C3E), RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            if (hasCameraPerm) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        GLSurfaceView(ctx).apply {
                            setEGLContextClientVersion(3)
                            val renderer = LiveCamcorderGlRenderer(shaderCode) { surfaceTexture ->
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build()
                                        val executor = Executors.newSingleThreadExecutor()
                                        preview.setSurfaceProvider(executor) { request ->
                                            surfaceTexture.setDefaultBufferSize(
                                                request.resolution.width,
                                                request.resolution.height
                                            )
                                            val surface = Surface(surfaceTexture)
                                            request.provideSurface(surface, executor) {
                                                surface.release()
                                            }
                                        }
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview
                                        )
                                    } catch (_: Exception) {
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
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
                    Text("CAMERA PERMISSION REQUIRED FOR LIVE VHS VIEWFINDER", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Camera Permission")
                    }
                }
            }

            // Authentic NEC uPD6450 Camcorder OSD Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isRecording) "REC  ${params.tapeSpeedMode}" else "PLAY ${params.tapeSpeedMode}",
                        color = Color(0xFFF4F6F0),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = params.presetName.uppercase(),
                        color = Color(0xFFFFB300),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = params.osdTitle,
                        color = Color(0xFFF4F6F0),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "TBC JITTER: ${(params.ghosting * 100).toInt()}%",
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Camcorder Zoom Rocker & Tape Speed Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ZOOM ${"%.1f".format(params.zoomRatio)}x", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Slider(
                value = params.zoomRatio,
                onValueChange = { params = params.copy(zoomRatio = it) },
                valueRange = 1.0f..4.0f,
                modifier = Modifier.weight(1f)
            )
            listOf("SP", "LP", "EP").forEach { mode ->
                FilterChip(
                    selected = params.tapeSpeedMode == mode,
                    onClick = {
                        params = when (mode) {
                            "EP" -> params.copy(tapeSpeedMode = "EP", chromaBleed = 0.85f, tapeNoise = 0.42f, ghosting = 0.16f)
                            "LP" -> params.copy(tapeSpeedMode = "LP", chromaBleed = 0.72f, tapeNoise = 0.28f, ghosting = 0.11f)
                            else -> params.copy(tapeSpeedMode = "SP", chromaBleed = 0.60f, tapeNoise = 0.18f, ghosting = 0.08f)
                        }
                    },
                    label = { Text(mode) }
                )
            }
        }

        // One-Tap Looks Strip
        Text("One-Tap Authentic VHS & Broadcast Looks", color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalogTvPresets.looks.forEach { look ->
                FilterChip(
                    selected = params.presetName == look.presetName,
                    onClick = { params = look },
                    label = { Text(look.presetName) }
                )
            }
        }

        // AnalogCam Live Stompbox Board
        Text("AnalogCam Stompbox Pedals", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = stompScramble,
                onClick = { stompScramble = !stompScramble },
                label = { Text("⚡ Scramble (SSAVI)") }
            )
            FilterChip(
                selected = stompImpulse,
                onClick = { stompImpulse = !stompImpulse },
                label = { Text("💥 Oxide Dropouts") }
            )
            FilterChip(
                selected = params.pauseBar > 0f,
                onClick = { params = params.copy(pauseBar = if (params.pauseBar > 0f) 0f else 1f) },
                label = { Text("⏸ Pause FM Bar") }
            )
        }

        // Authentic YIQ / VCR Hardware Sliders
        SliderControl("VHS Chroma Smear (629 kHz LPF + 1H Comb)", params.chromaBleed) { params = params.copy(chromaBleed = it) }
        SliderControl("Luma Edge Ringing & Halos (Pre-Emphasis)", params.tubeLag) { params = params.copy(tubeLag = it) }
        SliderControl("Per-Line H-Sync TBC Jitter (No Wavy Sine)", params.ghosting) { params = params.copy(ghosting = it) }
        SliderControl("FM Tape Noise & White Oxide Dropouts", params.tapeNoise) { params = params.copy(tapeNoise = it) }
        SliderControl("Top Flagging (Exponential H-PLL Skew)", params.flagging) { params = params.copy(flagging = it) }
        SliderControl("Rotary Head-Switch Tracking", params.tracking) { params = params.copy(tracking = it) }
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
            Text(label, color = Color(0xFFE2E4EC), fontSize = 12.sp)
            Text("${(value * 100).toInt()}%", color = Color(0xFFFFB300), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
    }
}