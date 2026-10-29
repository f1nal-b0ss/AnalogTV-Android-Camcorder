package com.retro.analogtv

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LiveCamcorderGlRenderer(
    private val fragmentShaderSource: String,
    private val onSurfaceTextureReady: (SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {

    @Volatile var params: AnalogTvParameters = AnalogTvPresets.looks.first()
    @Volatile var stompScramble: Float = 0f
    @Volatile var stompImpulse: Float = 0f
    @Volatile var degaussPhase: Float = 0f

    private var oesTextureId: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var programId: Int = 0
    private var viewWidth: Int = 720
    private var viewHeight: Int = 540
    private val startTimeMs: Long = System.currentTimeMillis()

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
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        oesTextureId = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        val vShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource)
        programId = GLES30.glCreateProgram().also { prog ->
            GLES30.glAttachShader(prog, vShader)
            GLES30.glAttachShader(prog, fShader)
            GLES30.glLinkProgram(prog)
        }

        surfaceTexture = SurfaceTexture(oesTextureId).also { st ->
            st.setDefaultBufferSize(720, 540)
            onSurfaceTextureReady(st)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width.coerceAtLeast(1)
        viewHeight = height.coerceAtLeast(1)
        GLES30.glViewport(0, 0, viewWidth, viewHeight)
    }

    override fun onDrawFrame(gl: GL10?) {
        try {
            surfaceTexture?.updateTexImage()
        } catch (_: Exception) {
        }

        if (degaussPhase > 0.01f) {
            degaussPhase = (degaussPhase - 0.016f).coerceAtLeast(0f)
        }

        GLES30.glClearColor(0.03f, 0.03f, 0.04f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        if (programId == 0) return
        GLES30.glUseProgram(programId)

        val p = params
        val timeSec = ((System.currentTimeMillis() - startTimeMs) % 3600000L) / 1000.0f

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        setUniform1i("uCameraTex", 0)
        setUniform1f("uTime", timeSec)
        GLES30.glUniform2f(GLES30.glGetUniformLocation(programId, "uResolution"), viewWidth.toFloat(), viewHeight.toFloat())
        setUniform1f("uZoom", p.zoomRatio)
        setUniform1i("uStandard", p.standard)
        setUniform1f("uChromaBleed", p.chromaBleed)
        setUniform1f("uTapeNoise", p.tapeNoise)
        setUniform1f("uFlagging", p.flagging)
        setUniform1f("uTracking", p.tracking)
        setUniform1f("uPauseBar", p.pauseBar)
        setUniform1f("uMainsHum", p.mainsHum)
        setUniform1f("uTubeLag", p.tubeLag)
        setUniform1f("uTubeReg", p.tubeReg)
        setUniform1f("uGhosting", p.ghosting)
        setUniform1f("uCurvature", p.curvature)
        setUniform1f("uScanlines", p.scanlines)
        setUniform1f("uStompScramble", stompScramble)
        setUniform1f("uStompImpulse", stompImpulse)
        setUniform1f("uDegaussPhase", degaussPhase)

        quadVertices.position(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 16, quadVertices)
        GLES30.glEnableVertexAttribArray(0)
        quadVertices.position(2)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 16, quadVertices)
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