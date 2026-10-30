package com.retro.analogtv

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LiveCamcorderGlRenderer(
    private val fragmentShaderSource: String,
    private val onSurfaceTextureReady: (SurfaceTexture) -> Unit,
    private val onVideoFrameCaptured: (Bitmap) -> Unit,
    private val onSnapshotCaptured: (Bitmap) -> Unit
) : GLSurfaceView.Renderer {

    @Volatile var params: AnalogTvParameters = AnalogTvPresets.looks.first()
    @Volatile var sensorRot: Int = 1        // 1 = 90 CW upright fix for rear camera in portrait
    @Volatile var mirrorX: Int = 0          // 1 for front selfie camera
    @Volatile var sourceMode: Int = 0       // 0 = Camera/Video, 1 = SMPTE Test Card, 2 = Outrun Synth
    @Volatile var transportMode: Int = 0    // 0 = Play, 1 = Pause, 2 = Cue x5, 3 = Rev x5
    @Volatile var stompScramble: Float = 0f
    @Volatile var stompImpulse: Float = 0f
    @Volatile var degaussPhase: Float = 0f
    @Volatile var crtPowerOn: Boolean = true
    @Volatile var wipeEnabled: Boolean = false
    @Volatile var wipeAutoSweep: Boolean = true
    @Volatile var wipePercent: Float = 50f
    @Volatile var isRecordingVideo: Boolean = false
    @Volatile var snapshotRequested: Boolean = false

    private var shutdownProg: Float = 0f
    private var oesTextureId: Int = 0
    private var osdTextureId: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var programId: Int = 0
    private var viewWidth: Int = 640
    private var viewHeight: Int = 480
    private val startTimeMs: Long = System.currentTimeMillis()
    private var lastOsdUpdateSec: Long = -1L
    private var lastOsdHeader: String = ""
    private var lastRecordCaptureMs: Long = 0L

    // Offscreen 640x480 FBO for clean 4:3 MP4 Video Recording & PNG Snapshots
    private val recW = 640
    private val recH = 480
    private var recFboId: Int = 0
    private var recTexId: Int = 0
    private val pixelBuf: ByteBuffer = ByteBuffer.allocateDirect(recW * recH * 4).order(ByteOrder.nativeOrder())
    private val argbPixels = IntArray(recW * recH)

    // Pre-encoder Fujitsu MB88303 OSD Bitmap (480x360)
    private val osdBitmap = Bitmap.createBitmap(480, 360, Bitmap.Config.ARGB_8888)
    private val osdCanvas = Canvas(osdBitmap)
    private val osdBoxPaint = Paint().apply {
        color = Color.argb(210, 6, 8, 12)
        style = Paint.Style.FILL
    }
    private val osdTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(248, 250, 252)
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = 18f
    }

    private val quadVertices: FloatBuffer = ByteBuffer
        .allocateDirect(16 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(
                floatArrayOf(
                    -1f, -1f, 0f, 1f,
                     1f, -1f, 1f, 1f,
                    -1f,  1f, 0f, 0f,
                     1f,  1f, 1f, 0f
                )
            )
            position(0)
        }

    // FBO vertices (inverted Y so glReadPixels is already upright or predictable)
    private val fboVertices: FloatBuffer = ByteBuffer
        .allocateDirect(16 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(
                floatArrayOf(
                    -1f, -1f, 0f, 0f,
                     1f, -1f, 1f, 0f,
                    -1f,  1f, 0f, 1f,
                     1f,  1f, 1f, 1f
                )
            )
            position(0)
        }

    private val vertexShaderCode = """#version 300 es
        layout(location = 0) in vec2 aPosition;
        layout(location = 1) in vec2 aTexCoord;
        out vec2 vTexCoord;
        void main() {
            vTexCoord = aTexCoord;
            gl_Position = vec4(aPosition, 0.0, 1.0);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val textures = IntArray(2)
        GLES30.glGenTextures(2, textures, 0)
        oesTextureId = textures[0]
        osdTextureId = textures[1]

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, osdTextureId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        setupRecordingFbo()

        val vShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource)
        programId = GLES30.glCreateProgram().also { prog ->
            GLES30.glAttachShader(prog, vShader)
            GLES30.glAttachShader(prog, fShader)
            GLES30.glLinkProgram(prog)
        }

        updateOsdTexture(0L, force = true)

        surfaceTexture = SurfaceTexture(oesTextureId).also { st ->
            st.setDefaultBufferSize(720, 540)
            onSurfaceTextureReady(st)
        }
    }

    private fun setupRecordingFbo() {
        val fbos = IntArray(1)
        val texs = IntArray(1)
        GLES30.glGenFramebuffers(1, fbos, 0)
        GLES30.glGenTextures(1, texs, 0)
        recFboId = fbos[0]
        recTexId = texs[0]

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, recTexId)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, recW, recH, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, recFboId)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, recTexId, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    private fun updateOsdTexture(elapsedSec: Long, force: Boolean = false) {
        val p = params
        val headerKey = "${p.osdEnabled}_${p.osdVobBox}_${p.osdTitle}_${p.tapeSpeedMode}_${p.zoomRatio}_${transportMode}_${isRecordingVideo}"
        if (!force && elapsedSec == lastOsdUpdateSec && headerKey == lastOsdHeader) return
        lastOsdUpdateSec = elapsedSec
        lastOsdHeader = headerKey

        osdCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (p.osdEnabled) {
            val modeLabel = when {
                isRecordingVideo -> "● REC"
                transportMode == 1 -> "PAUSE ⏸"
                transportMode == 2 -> "CUE ×5 ⏩"
                transportMode == 3 -> "REV ×5 ⏪"
                else -> "PLAY ▶"
            }
            val zoomStr = if (p.zoomRatio > 1.05f) " ${"%.1f".format(p.zoomRatio)}X" else ""
            val topLeft = "$modeLabel ${p.tapeSpeedMode}$zoomStr"
            val mm = (elapsedSec / 60L) % 60L
            val ss = elapsedSec % 60L
            val topRight = "0:%02d:%02d".format(mm, ss)
            val bottomLeft = p.osdTitle.ifBlank { "OCT 29 1985" }

            drawOsdCell(topLeft, 24f, 38f, p.osdVobBox, if (isRecordingVideo) Color.rgb(255, 90, 90) else Color.rgb(248, 250, 252))
            drawOsdCell(topRight, 355f, 38f, p.osdVobBox, Color.rgb(248, 250, 252))
            drawOsdCell(bottomLeft, 24f, 336f, p.osdVobBox, Color.rgb(248, 250, 252))
        }

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, osdTextureId)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, osdBitmap, 0)
    }

    private fun drawOsdCell(text: String, x: Float, y: Float, vobBox: Boolean, textColor: Int) {
        val w = osdTextPaint.measureText(text) + 14f
        if (vobBox) {
            osdCanvas.drawRect(x - 7f, y - 19f, x - 7f + w, y + 6f, osdBoxPaint)
        }
        osdTextPaint.color = textColor
        osdCanvas.drawText(text, x, y, osdTextPaint)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width.coerceAtLeast(1)
        viewHeight = height.coerceAtLeast(1)
        GLES30.glViewport(0, 0, viewWidth, viewHeight)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (transportMode != 1) {
            try {
                surfaceTexture?.updateTexImage()
            } catch (_: Exception) {
            }
        }

        if (degaussPhase > 0.01f) {
            degaussPhase = (degaussPhase - 0.016f).coerceAtLeast(0f)
        }
        if (!crtPowerOn) {
            shutdownProg = (shutdownProg + 0.03f).coerceAtMost(1f)
        } else if (shutdownProg > 0f) {
            shutdownProg = (shutdownProg - 0.04f).coerceAtLeast(0f)
        }

        val nowMs = System.currentTimeMillis()
        val timeSec = ((nowMs - startTimeMs) % 3600000L) / 1000.0f
        updateOsdTexture((nowMs - startTimeMs) / 1000L)

        // 1. Render to screen viewport
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glViewport(0, 0, viewWidth, viewHeight)
        drawVhsQuad(timeSec, viewWidth.toFloat(), viewHeight.toFloat(), quadVertices)

        // 2. If recording MP4 (~24 fps) or snapshot requested, render to 640x480 FBO and capture Bitmap
        val needRecFrame = isRecordingVideo && (nowMs - lastRecordCaptureMs >= 41L)
        val needSnap = snapshotRequested
        if ((needRecFrame || needSnap) && recFboId != 0) {
            if (needRecFrame) lastRecordCaptureMs = nowMs
            if (needSnap) snapshotRequested = false

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, recFboId)
            GLES30.glViewport(0, 0, recW, recH)
            drawVhsQuad(timeSec, recW.toFloat(), recH.toFloat(), fboVertices)

            pixelBuf.position(0)
            GLES30.glReadPixels(0, 0, recW, recH, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixelBuf)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
            GLES30.glViewport(0, 0, viewWidth, viewHeight)

            // Convert RGBA bytes to ARGB_8888 Bitmap
            pixelBuf.position(0)
            val bytes = ByteArray(recW * recH * 4)
            pixelBuf.get(bytes)
            var srcIdx = 0
            for (i in 0 until (recW * recH)) {
                val r = bytes[srcIdx].toInt() and 0xFF
                val g = bytes[srcIdx + 1].toInt() and 0xFF
                val b = bytes[srcIdx + 2].toInt() and 0xFF
                argbPixels[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                srcIdx += 4
            }
            val bmp = Bitmap.createBitmap(argbPixels, recW, recH, Bitmap.Config.ARGB_8888)
            if (needRecFrame) {
                onVideoFrameCaptured(bmp)
            }
            if (needSnap) {
                onSnapshotCaptured(bmp)
            }
        }
    }

    private fun drawVhsQuad(timeSec: Float, w: Float, h: Float, verts: FloatBuffer) {
        GLES30.glClearColor(0.02f, 0.024f, 0.035f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        if (programId == 0) return

        GLES30.glUseProgram(programId)
        val p = params

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        setUniform1i("uCameraTex", 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, osdTextureId)
        setUniform1i("uOsdTex", 1)

        setUniform1f("uTime", timeSec)
        GLES30.glUniform2f(GLES30.glGetUniformLocation(programId, "uResolution"), w, h)
        setUniform1f("uZoom", p.zoomRatio)
        setUniform1i("uSensorRot", sensorRot)
        setUniform1i("uMirrorX", mirrorX)
        setUniform1i("uSourceMode", sourceMode)

        setUniform1i("uStandard", p.standard)
        setUniform1i("uTubeMode", p.tubeMode)
        setUniform1i("uPhosphorMode", p.phosphorMode)
        setUniform1i("uMaskMode", p.maskMode)
        setUniform1i("uIsPxl2000", if (p.vcrFormat == "pxl2000") 1 else 0)
        setUniform1i("uTransportMode", transportMode)
        setUniform1f("uTbcAtten", if (p.tbcLocked) 0.18f else 1.0f)

        setUniform1f("uChromaBleed", p.chromaBleed)
        setUniform1f("uFlagging", p.flagging)
        setUniform1f("uTapeNoise", p.tapeNoise)
        setUniform1f("uFlutter", p.flutter)
        setUniform1f("uTracking", p.tracking)
        setUniform1f("uEdgeRinging", p.edgeRinging)
        setUniform1f("uDotCrawl", p.dotCrawl)
        setUniform1f("uSnow", p.snow)
        setUniform1f("uGhosting", p.ghosting)
        setUniform1f("uHanover", p.hanover)
        setUniform1f("uVHold", p.vHold)
        setUniform1f("uMainsHum", p.mainsHum)
        setUniform1f("uTubeLag", p.tubeLag)
        setUniform1f("uTubeReg", p.tubeReg)
        setUniform1f("uScanlines", p.scanlines)
        setUniform1f("uCurvature", p.curvature)
        setUniform1f("uBloom", p.bloom)
        setUniform1f("uCrtContrast", p.crtContrast)

        setUniform1f("uStompScramble", stompScramble)
        setUniform1f("uStompImpulse", stompImpulse)
        setUniform1f("uDegaussPhase", degaussPhase)
        setUniform1f("uShutdownProg", shutdownProg)

        val effWipe = if (!wipeEnabled) {
            -1.0f
        } else if (wipeAutoSweep) {
            0.50f + 0.40f * kotlin.math.sin(timeSec * 0.85f)
        } else {
            (wipePercent / 100.0f).coerceIn(0.05f, 0.95f)
        }
        setUniform1f("uWipePos", effWipe)

        verts.position(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 16, verts)
        GLES30.glEnableVertexAttribArray(0)
        verts.position(2)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 16, verts)
        GLES30.glEnableVertexAttribArray(1)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun setUniform1f(name: String, value: Float) {
        val loc = GLES30.glGetUniformLocation(programId, name)
        if (loc >= 0) GLES30.glUniform1f(loc, value)
    }

    private fun setUniform1i(name: String, value: Int) {
        val loc = GLES30.glGetUniformLocation(programId, name)
        if (loc >= 0) GLES30.glUniform1i(loc, value)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        return shader
    }
}