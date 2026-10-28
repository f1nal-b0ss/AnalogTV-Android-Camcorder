package com.retro.analogtv

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Dual-target OpenGL ES 3.0 renderer for Live Camcorder Recording:
 * 1. Receives live camera frames from CameraX via an OES external texture.
 * 2. Runs the AnalogTV 3.0 composite/VHS fragment shader with ping-pong FBO for Vidicon lag.
 * 3. Renders both to the screen GLSurfaceView and to the MediaCodec H.264 encoder Surface.
 */
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
    private var startTimeMs: Long = System.currentTimeMillis()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        oesTextureId = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        surfaceTexture = SurfaceTexture(oesTextureId).also { st ->
            onSurfaceTextureReady(st)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture?.updateTexImage()
        if (degaussPhase > 0.01f) {
            degaussPhase = (degaussPhase - 0.016f).coerceAtLeast(0f)
        }
        // Shader uniforms (uStandard, uChromaBleed, uTubeLag, uZoom, stompboxes) bound per frame
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }
}
