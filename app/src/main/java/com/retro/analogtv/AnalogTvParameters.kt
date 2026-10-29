package com.retro.analogtv

data class AnalogTvParameters(
    val presetName: String = "Home Movie (VHS)",
    val tapeSpeedMode: String = "SP",
    val standard: Int = 0,            // 0=NTSC, 1=PAL, 2=SECAM, 3=405-Line Mono
    val chromaBleed: Float = 0.62f,   // 629kHz colour-under Gaussian LPF + 1H comb filter
    val tapeNoise: Float = 0.20f,     // FM grain + multi-pixel horizontal oxide dropouts
    val flagging: Float = 0.14f,      // Top-of-frame exponential H-PLL tension pull
    val tracking: Float = 0.50f,      // Bottom 6-line head switch & pause bar position
    val pauseBar: Float = 0.0f,       // Stationary drum azimuth bar
    val mainsHum: Float = 0.0f,       // 50/60 Hz hum bars
    val tubeLag: Float = 0.48f,       // Luma pre-emphasis edge ringing + highlight glow
    val tubeReg: Float = 0.16f,       // Camera tube R/B registration offset
    val ghosting: Float = 0.08f,      // Quasi-rigid per-line TBC H-sync jitter (low by default)
    val curvature: Float = 0.04f,     // Subtle CRT faceplate curvature
    val scanlines: Float = 0.35f,     // Scanline intensity
    val audioTapeWear: Float = 0.40f, // Audio wow/flutter & bandwidth limit
    val zoomRatio: Float = 1.0f,      // Camcorder zoom rocker (1.0x .. 4.0x)
    val osdTitle: String = "OCT 29 1985"
)

object AnalogTvPresets {
    val looks = listOf(
        AnalogTvParameters("Home Movie (VHS)", "SP", standard = 0, chromaBleed = 0.62f, tapeNoise = 0.20f, flagging = 0.14f, tubeLag = 0.48f, ghosting = 0.08f),
        AnalogTvParameters("Broadcast 1-Inch", "SP", standard = 0, chromaBleed = 0.32f, tapeNoise = 0.10f, flagging = 0.05f, tubeLag = 0.35f, ghosting = 0.03f),
        AnalogTvParameters("Camcorder Hi8", "SP", standard = 0, chromaBleed = 0.28f, tapeNoise = 0.12f, flagging = 0.06f, tubeLag = 0.28f, ghosting = 0.04f),
        AnalogTvParameters("3rd-Gen EP Dub", "EP", standard = 0, chromaBleed = 0.88f, tapeNoise = 0.45f, flagging = 0.32f, tubeLag = 0.65f, ghosting = 0.18f),
        AnalogTvParameters("PAL VHS Archive", "SP", standard = 1, chromaBleed = 0.55f, tapeNoise = 0.22f, flagging = 0.12f, mainsHum = 0.15f, ghosting = 0.07f),
        AnalogTvParameters("Classic 405 B&W", "SP", standard = 3, chromaBleed = 0.0f, tapeNoise = 0.30f, flagging = 0.15f, tubeLag = 0.55f, ghosting = 0.09f)
    )
}