package com.retro.analogtv

data class AnalogTvParameters(
    val presetName: String = "Home Movie (VHS)",
    val tapeSpeedMode: String = "SP", // SP (240 TVL), LP, EP/SLP (Heavy Wear)
    val standard: Int = 0,            // 0=NTSC, 1=PAL, 2=SECAM, 3=405-Line Mono
    val chromaBleed: Float = 0.65f,   // VHS 629kHz colour-under smear
    val tapeNoise: Float = 0.28f,     // FM noise triangle + dropouts
    val flagging: Float = 0.22f,      // Top-of-frame tension pull
    val tracking: Float = 0.50f,      // Head switch & pause bar position
    val pauseBar: Float = 0.0f,       // Stationary drum azimuth bar
    val mainsHum: Float = 0.0f,       // 50/60 Hz hum bars
    val tubeLag: Float = 0.45f,       // Vidicon camera tube comet persistence
    val tubeReg: Float = 0.20f,       // Camera tube R/B registration drift
    val ghosting: Float = 0.15f,      // RF multipath ghosting
    val curvature: Float = 0.08f,     // CRT faceplate curvature
    val scanlines: Float = 0.55f,     // Scanlines + phosphor mask
    val audioTapeWear: Float = 0.45f, // Audio wow/flutter & bandwidth limit
    val zoomRatio: Float = 1.0f,      // Camcorder zoom rocker (1.0x .. 4.0x)
    val osdTitle: String = "OCT 28 1985"
)

object AnalogTvPresets {
    val looks = listOf(
        AnalogTvParameters("Home Movie (VHS)", "SP", standard = 0, chromaBleed = 0.72f, tapeNoise = 0.32f, flagging = 0.28f, tubeLag = 0.38f, tubeReg = 0.15f),
        AnalogTvParameters("Night Tram (Vidicon)", "SP", standard = 1, chromaBleed = 0.48f, tapeNoise = 0.20f, flagging = 0.14f, tubeLag = 0.78f, tubeReg = 0.34f, ghosting = 0.26f),
        AnalogTvParameters("Camcorder Hi8", "SP", standard = 0, chromaBleed = 0.34f, tapeNoise = 0.14f, flagging = 0.08f, tubeLag = 0.12f, tubeReg = 0.08f),
        AnalogTvParameters("3rd-Gen EP Dub", "EP", standard = 0, chromaBleed = 0.92f, tapeNoise = 0.58f, flagging = 0.52f, tubeLag = 0.50f, tubeReg = 0.40f, audioTapeWear = 0.82f),
        AnalogTvParameters("Cinéma Vérité (SECAM)", "SP", standard = 2, chromaBleed = 0.64f, tapeNoise = 0.30f, flagging = 0.20f, mainsHum = 0.35f, tubeLag = 0.45f),
        AnalogTvParameters("Classic 405 B&W", "SP", standard = 3, chromaBleed = 0.0f, tapeNoise = 0.38f, flagging = 0.18f, tubeLag = 0.65f, curvature = 0.16f)
    )
}
