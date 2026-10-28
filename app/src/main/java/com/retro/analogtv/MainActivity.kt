package com.retro.analogtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                LiveCamcorderScreen()
            }
        }
    }
}

@Composable
fun LiveCamcorderScreen() {
    var params by remember { mutableStateOf(AnalogTvPresets.looks.first()) }
    var isRecording by remember { mutableStateOf(false) }
    var stompScramble by remember { mutableStateOf(false) }
    var stompImpulse by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Camcorder Top HUD Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isRecording) "● REC [${params.tapeSpeedMode}]" else "■ STBY [${params.tapeSpeedMode}]",
                    color = if (isRecording) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "PANASONIC PK-958 / MB88303 OSD",
                    color = Color(0xFFFFB300),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
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

        // Camcorder Zoom Rocker & Tape Speed Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                            "EP" -> params.copy(tapeSpeedMode = "EP", chromaBleed = 0.90f, tapeNoise = 0.55f, audioTapeWear = 0.80f)
                            "LP" -> params.copy(tapeSpeedMode = "LP", chromaBleed = 0.78f, tapeNoise = 0.40f, audioTapeWear = 0.60f)
                            else -> params.copy(tapeSpeedMode = "SP", chromaBleed = 0.65f, tapeNoise = 0.25f, audioTapeWear = 0.40f)
                        }
                    },
                    label = { Text(mode) }
                )
            }
        }

        // One-Tap Looks Strip
        Text("One-Tap Camcorder Looks", color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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

        // AnalogCam 9-Pedal Live Stompbox Board
        Text("AnalogCam Live Stompbox Pedals", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                label = { Text("💥 Ignition Impulse") }
            )
            FilterChip(
                selected = params.pauseBar > 0f,
                onClick = { params = params.copy(pauseBar = if (params.pauseBar > 0f) 0f else 1f) },
                label = { Text("⏸ Pause FM Bar") }
            )
            FilterChip(
                selected = params.ghosting > 0.4f,
                onClick = { params = params.copy(ghosting = if (params.ghosting > 0.4f) 0.1f else 0.65f) },
                label = { Text("👻 Multipath Ghost") }
            )
        }

        // Core Signal & Tube Controls
        SliderControl("Vidicon Camera Tube Lag (Comet Trails)", params.tubeLag) { params = params.copy(tubeLag = it) }
        SliderControl("VHS Chroma Smear (629 kHz Colour-Under)", params.chromaBleed) { params = params.copy(chromaBleed = it) }
        SliderControl("FM Tape Noise & Dropouts", params.tapeNoise) { params = params.copy(tapeNoise = it) }
        SliderControl("Flagging (Top H-Sync Tension Pull)", params.flagging) { params = params.copy(flagging = it) }
        SliderControl("3-Tube Camera R/B Registration", params.tubeReg) { params = params.copy(tubeReg = it) }
        SliderControl("Microphone Tape Wear (Wow/Flutter & Hiss)", params.audioTapeWear) { params = params.copy(audioTapeWear = it) }
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
