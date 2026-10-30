package com.retro.analogtv

data class AnalogTvParameters(
    val id: String = "home_vhs",
    val presetName: String = "Home Movie (VHS)",
    val badge: String = "NTSC · VHS SP",
    val vcrFormat: String = "vhs_sp",    // vhs_sp, vhs_ep, betamax, svhs, hi8, pxl2000
    val tapeSpeedMode: String = "SP",    // SP, LP, EP
    val standard: Int = 0,               // 0=NTSC, 1=PAL, 2=SECAM, 3=405-Line Mono
    val tubeMode: Int = 0,               // 0=Vidicon, 1=Plumbicon, 2=Orthicon, 3=Early CCD
    val phosphorMode: Int = 0,           // 0=P22 Colour, 1=P31 Green, 2=P4 B&W White, 3=P3 Amber
    val maskMode: Int = 0,               // 0=Aperture Grille, 1=Shadow Mask, 2=Slot Mask
    val tbcLocked: Boolean = false,
    val chromaBleed: Float = 0.68f,      // 629kHz colour-under smear + group delay
    val flagging: Float = 0.16f,         // Top-of-frame exponential H-PLL skew
    val tapeNoise: Float = 0.24f,        // Oxide dropouts & FM spikes
    val flutter: Float = 0.15f,          // Per-scanline H-sync jitter
    val tracking: Float = 0.50f,         // Head-switch & pause noise bar position
    val edgeRinging: Float = 0.55f,      // 2 MHz luma peaking & overshoot halo
    val dotCrawl: Float = 0.58f,         // Subcarrier cross-colour moire & dot crawl
    val snow: Float = 0.16f,             // RF snow / IF noise (colour killer > 0.82)
    val ghosting: Float = 0.20f,         // Multipath RF delayed ghost echo
    val hanover: Float = 0.35f,          // PAL Hanover bars / SECAM fire
    val vHold: Float = 0.0f,             // Vertical roll speed
    val mainsHum: Float = 0.0f,          // 0=Off, 0.5=50Hz, 1.0=60Hz
    val tubeLag: Float = 0.48f,          // Ping-pong FBO camera tube persistence
    val tubeReg: Float = 0.26f,          // 3-tube R/B prism registration drift
    val scanlines: Float = 0.64f,        // Dynamic EHT scanlines & mask pitch
    val curvature: Float = 0.22f,        // CRT barrel curvature & bezel vignette
    val bloom: Float = 0.48f,            // 2D glass halation & phosphor bloom
    val crtContrast: Float = 0.65f,      // Vintage CRT S-curve contrast & chroma punch
    val audioTapeWear: Float = 0.45f,
    val audioBuzz: Float = 0.25f,
    val zoomRatio: Float = 1.0f,
    val osdEnabled: Boolean = true,
    val osdVobBox: Boolean = true,
    val osdTitle: String = "OCT 29 1985"
)

object AnalogTvPresets {
    val looks = listOf(
        AnalogTvParameters(
            id = "home_vhs", presetName = "Home Movie (VHS)", badge = "NTSC · VHS SP",
            vcrFormat = "vhs_sp", tapeSpeedMode = "SP", standard = 0, tubeMode = 0, phosphorMode = 0, maskMode = 0,
            chromaBleed = 0.68f, flagging = 0.16f, tapeNoise = 0.24f, flutter = 0.16f, edgeRinging = 0.56f,
            dotCrawl = 0.58f, snow = 0.16f, ghosting = 0.18f, tubeLag = 0.46f, tubeReg = 0.25f,
            scanlines = 0.64f, curvature = 0.22f, bloom = 0.48f, crtContrast = 0.68f
        ),
        AnalogTvParameters(
            id = "night_tram", presetName = "Night Tram (Vidicon)", badge = "PAL · Heavy Lag",
            vcrFormat = "vhs_sp", tapeSpeedMode = "SP", standard = 1, tubeMode = 0, phosphorMode = 0, maskMode = 1,
            chromaBleed = 0.58f, flagging = 0.12f, tapeNoise = 0.20f, flutter = 0.14f, edgeRinging = 0.48f,
            dotCrawl = 0.45f, snow = 0.18f, ghosting = 0.26f, tubeLag = 0.82f, tubeReg = 0.34f,
            scanlines = 0.62f, curvature = 0.24f, bloom = 0.65f, crtContrast = 0.72f
        ),
        AnalogTvParameters(
            id = "sydney_pal", presetName = "Sydney Ferry (PAL)", badge = "PAL · 625-Line",
            vcrFormat = "vhs_sp", tapeSpeedMode = "SP", standard = 1, tubeMode = 0, phosphorMode = 0, maskMode = 2,
            chromaBleed = 0.62f, flagging = 0.14f, tapeNoise = 0.22f, flutter = 0.15f, edgeRinging = 0.45f,
            dotCrawl = 0.48f, snow = 0.16f, ghosting = 0.22f, hanover = 0.55f, tubeLag = 0.42f, tubeReg = 0.24f,
            scanlines = 0.60f, curvature = 0.22f, bloom = 0.45f, crtContrast = 0.65f
        ),
        AnalogTvParameters(
            id = "studio_82", presetName = "'82 News Studio", badge = "NTSC · Plumbicon",
            vcrFormat = "svhs", tapeSpeedMode = "SP", standard = 0, tubeMode = 1, phosphorMode = 0, maskMode = 0,
            tbcLocked = true, chromaBleed = 0.35f, flagging = 0.06f, tapeNoise = 0.10f, flutter = 0.06f,
            edgeRinging = 0.42f, dotCrawl = 0.62f, snow = 0.08f, ghosting = 0.12f, tubeLag = 0.18f, tubeReg = 0.38f,
            scanlines = 0.58f, curvature = 0.18f, bloom = 0.38f, crtContrast = 0.62f
        ),
        AnalogTvParameters(
            id = "cam_hi8", presetName = "Camcorder Hi8", badge = "Hi8 · 400 TVL",
            vcrFormat = "hi8", tapeSpeedMode = "SP", standard = 0, tubeMode = 3, phosphorMode = 0, maskMode = 0,
            chromaBleed = 0.30f, flagging = 0.08f, tapeNoise = 0.14f, flutter = 0.08f, edgeRinging = 0.62f,
            dotCrawl = 0.42f, snow = 0.10f, ghosting = 0.10f, tubeLag = 0.12f, tubeReg = 0.12f,
            scanlines = 0.55f, curvature = 0.16f, bloom = 0.35f, crtContrast = 0.60f
        ),
        AnalogTvParameters(
            id = "worn_dub", presetName = "3rd-Gen Rental Dub", badge = "VHS EP · Worn",
            vcrFormat = "vhs_ep", tapeSpeedMode = "EP", standard = 0, tubeMode = 0, phosphorMode = 0, maskMode = 1,
            chromaBleed = 0.92f, flagging = 0.38f, tapeNoise = 0.64f, flutter = 0.38f, tracking = 0.66f,
            edgeRinging = 0.72f, dotCrawl = 0.78f, snow = 0.34f, ghosting = 0.36f, mainsHum = 0.6f,
            tubeLag = 0.52f, tubeReg = 0.42f, scanlines = 0.68f, curvature = 0.26f, bloom = 0.56f, crtContrast = 0.75f
        ),
        AnalogTvParameters(
            id = "secam_verite", presetName = "Cinéma Vérité (SECAM)", badge = "SECAM-L · FM Chroma",
            vcrFormat = "vhs_sp", tapeSpeedMode = "SP", standard = 2, tubeMode = 0, phosphorMode = 0, maskMode = 2,
            chromaBleed = 0.66f, flagging = 0.18f, tapeNoise = 0.28f, flutter = 0.18f, edgeRinging = 0.52f,
            dotCrawl = 0.50f, snow = 0.22f, ghosting = 0.24f, hanover = 0.75f, mainsHum = 0.4f,
            tubeLag = 0.48f, tubeReg = 0.28f, scanlines = 0.62f, curvature = 0.24f, bloom = 0.46f, crtContrast = 0.68f
        ),
        AnalogTvParameters(
            id = "classic_405", presetName = "1952 System A (405 B&W)", badge = "405-Line · P4 B&W",
            vcrFormat = "betamax", tapeSpeedMode = "SP", standard = 3, tubeMode = 2, phosphorMode = 2, maskMode = 1,
            tbcLocked = true, chromaBleed = 0.0f, flagging = 0.12f, tapeNoise = 0.24f, flutter = 0.12f,
            edgeRinging = 0.35f, dotCrawl = 0.0f, snow = 0.30f, ghosting = 0.28f, mainsHum = 0.4f,
            tubeLag = 0.65f, tubeReg = 0.0f, scanlines = 0.78f, curvature = 0.38f, bloom = 0.60f, crtContrast = 0.76f
        ),
        AnalogTvParameters(
            id = "pxl2000", presetName = "PXL-2000 Toy Cam ('87)", badge = "120×90 · Audio Tape",
            vcrFormat = "pxl2000", tapeSpeedMode = "EP", standard = 3, tubeMode = 3, phosphorMode = 2, maskMode = 2,
            chromaBleed = 0.0f, flagging = 0.24f, tapeNoise = 0.38f, flutter = 0.28f, edgeRinging = 0.68f,
            dotCrawl = 0.0f, snow = 0.28f, ghosting = 0.14f, tubeLag = 0.25f, tubeReg = 0.0f,
            scanlines = 0.50f, curvature = 0.20f, bloom = 0.32f, crtContrast = 0.82f
        ),
        AnalogTvParameters(
            id = "p31_scope", presetName = "P31 Green Vector CRT", badge = "P31 Phosphor",
            vcrFormat = "svhs", tapeSpeedMode = "SP", standard = 0, tubeMode = 0, phosphorMode = 1, maskMode = 0,
            chromaBleed = 0.25f, flagging = 0.10f, tapeNoise = 0.18f, flutter = 0.10f, edgeRinging = 0.60f,
            dotCrawl = 0.20f, snow = 0.15f, ghosting = 0.15f, tubeLag = 0.68f, tubeReg = 0.0f,
            scanlines = 0.72f, curvature = 0.28f, bloom = 0.62f, crtContrast = 0.75f
        )
    )
}