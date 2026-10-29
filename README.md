# AnalogTV 3.0 Camcorder for Android 📼

![AnalogTV 3.0 Android Camcorder Banner](assets/banner.jpg)

[![Build Android APK](https://img.shields.io/badge/GitHub_Actions-Build_APK-f59e0b?logo=githubactions&logoColor=white)](.github/workflows/build-apk.yml)
[![Android API](https://img.shields.io/badge/Android-API_26%2B_(8.0%2B)-3ddc84?logo=android&logoColor=white)](app/build.gradle.kts)
[![OpenGL ES](https://img.shields.io/badge/GPU-OpenGL_ES_3.0_Signal_Sim-38bdf8)](app/src/main/res/raw/analog_vhs_fragment.glsl)
[![License: MIT](https://img.shields.io/badge/License-MIT-slate)](LICENSE)

> **A complete television & VHS camcorder signal chain in software — built for live Android shooting.**
> Inspired by [Alastair Bor's AnalogTV 3.0](https://analogtv.net/) ([Reddit announcement](https://www.reddit.com/r/iosapps/comments/1xfuicj/analogtv_30_for_iphone_and_ipad_is_out_the/)). No static pixel overlays: every live camera frame passes through a real-time **Camera Tube → YIQ/YUV Composite Encoder → Helical-Scan VCR Tape Deck → RF Tuner → CRT Phosphor** GPU shader pipeline while recording hardware-encoded `.mp4` video and DSP-degraded VHS audio.

---

## ✨ Live Camcorder Highlights

1. **Zero-Latency CameraX + OpenGL ES 3.0 Viewfinder (`LiveCamcorderGlRenderer.kt`)**
   - Streams the Android camera's `SurfaceTexture` (`GL_TEXTURE_EXTERNAL_OES`) at 60 fps through an OpenGL ES 3.0 ping-pong FBO pipeline.
   - Simultaneously renders to the on-screen CRT viewfinder and an hardware `MediaCodec` (`video/avc` H.264) input `Surface` for instant `.mp4` recording with zero CPU frame readback.
2. **Authentic Camera Tube Physics**
   - **Vidicon (Sb₂S₃)**: Illumination-dependent temporal charge lag that trails glowing comet tails behind streetlights and headlights.
   - **Plumbicon / Saticon**: 3-tube prism registration drift with independent Red/Blue channel offset.
   - **Image Orthicon**: Secondary-electron dark halation around extreme highlights.
   - **Early 1980s CCD**: Vertical shift-register smear stripes through bright lights.
3. **Panasonic PK-958 / Fujitsu MB88303 Pre-Encoder OSD Titler**
   - Renders authentic 5×7 dot-matrix camcorder titles, `REC ●` tally, `SP/LP/EP` tape speed, and live timecode **before** composite modulation so the text itself exhibits dot crawl, chroma bleed, and tracking glitches.
4. **AnalogCam 9-Pedal Live Stompbox Board**
   - Perform live physical signal glitches while recording: **Tune**, **V-Hold**, **Snow**, **Impulse**, **Scramble (SSAVI)**, **Power**, **Degauss**, **Magnet**, and **Ghost**.
5. **Real-Time Microphone Tape Wear DSP (`AnalogAudioTapeRecorder.kt`)**
   - Captures live microphone audio via `AudioRecord`, applies a capstan wow & flutter variable-delay LFO, 150 Hz–7 kHz linear VHS tape & CRT cabinet EQ, 50/60 Hz intercarrier buzz, and oxide dropout crackle before encoding to AAC (`MediaMuxer`).

---

## 📦 Getting the Native `.apk`

### Method 1 — Automatic GitHub Actions APK Build (No Local Setup Needed)
This repository includes [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml):
1. Push this repository to GitHub.
2. Open the **Actions** tab → **Build & Release Android Camcorder APK**.
3. Download **`AnalogTV-Camcorder-v3.0-debug.apk`** directly from the workflow artifacts or the automated **GitHub Releases** page, and deploy the included `docs/` site to **GitHub Pages**.

### Method 2 — Build Locally via Android Studio / Gradle CLI
```bash
git clone https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder.git
cd AnalogTV-Android-Camcorder
./gradlew assembleDebug
# Output APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🗂️ Repository Structure

```text
AnalogTV-Android-Camcorder/
├── .github/workflows/
│   └── build-apk.yml                        # Automated CI/CD APK compiler + GitHub Pages publisher
├── assets/
│   └── banner.jpg                           # Hero studio camcorder & CRT banner image
├── docs/
│   ├── index.html                           # GitHub Pages landing page & live web simulator
│   └── banner.jpg
├── app/
│   ├── build.gradle.kts                     # CameraX, Media3, Compose, OpenGL ES dependencies
│   └── src/main/
│       ├── AndroidManifest.xml              # Camera, Record Audio & OpenGL ES 3.0 permissions
│       ├── res/raw/analog_vhs_fragment.glsl # 9-stage Composite/VHS/CRT GLSL ES 3.0 shader
│       └── java/com/retro/analogtv/
│           ├── MainActivity.kt              # Jetpack Compose Live Camcorder Viewfinder HUD + Stompboxes
│           ├── AnalogTvParameters.kt        # Presets (Home Movie, Night Tram, Camcorder Hi8, SECAM...)
│           ├── LiveCamcorderGlRenderer.kt   # CameraX OES -> Ping-Pong FBO -> Screen & MediaCodec encoder
│           ├── AnalogAudioTapeRecorder.kt   # Live Microphone -> Tape Wear Wow/Flutter DSP -> AAC Muxer
│           └── VideoConverterPipeline.kt    # Media3 Transformer gallery video converter
├── build.gradle.kts
└── settings.gradle.kts
```
