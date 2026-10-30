package com.retro.analogtv

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Surface
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class RecordedTapeItem(
    val file: File,
    val title: String,
    val presetBadge: String,
    val durationSec: Int,
    val isVideo: Boolean,
    val galleryUri: Uri? = null
)

class VhsMp4Recorder(
    private val context: Context,
    private val width: Int = 640,
    private val height: Int = 480,
    private val fps: Int = 24
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val isRecordingAtom = AtomicBoolean(false)

    private var mediaCodec: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var mediaMuxer: MediaMuxer? = null
    private var trackIndex: Int = -1
    private var muxerStarted: Boolean = false
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L

    fun isRecording(): Boolean = isRecordingAtom.get()

    fun startRecording(presetName: String, onStarted: () -> Unit, onError: (String) -> Unit) {
        if (isRecordingAtom.get()) return
        executor.execute {
            try {
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
                if (!dir.exists()) dir.mkdirs()
                val safePreset = presetName.replace(Regex("[^A-Za-z0-9]"), "_")
                val file = File(dir, "AnalogTV_VHS_${safePreset}_${System.currentTimeMillis() % 100000}.mp4")
                outputFile = file

                val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                    setInteger(MediaFormat.KEY_BIT_RATE, 3_200_000)
                    setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                }

                val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
                codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                val surface = codec.createInputSurface()
                codec.start()

                val muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

                mediaCodec = codec
                inputSurface = surface
                mediaMuxer = muxer
                trackIndex = -1
                muxerStarted = false
                startTimeMs = System.currentTimeMillis()
                isRecordingAtom.set(true)
                onStarted()
            } catch (e: Exception) {
                isRecordingAtom.set(false)
                onError(e.localizedMessage ?: "Recorder start failed")
            }
        }
    }

    fun encodeFrame(frameBitmap: Bitmap) {
        if (!isRecordingAtom.get()) return
        executor.execute {
            if (!isRecordingAtom.get()) return@execute
            try {
                val surface = inputSurface ?: return@execute
                val canvas = surface.lockCanvas(null)
                canvas.drawBitmap(frameBitmap, 0f, 0f, null)
                surface.unlockAndPost(canvas)
                drainEncoder(endOfStream = false)
            } catch (_: Exception) {
            }
        }
    }

    fun stopRecording(presetName: String, badge: String, onFinished: (RecordedTapeItem?) -> Unit) {
        if (!isRecordingAtom.getAndSet(false)) {
            onFinished(null)
            return
        }
        executor.execute {
            val durSec = (((System.currentTimeMillis() - startTimeMs) / 1000L).toInt()).coerceAtLeast(1)
            try {
                drainEncoder(endOfStream = true)
            } catch (_: Exception) {
            }
            try {
                mediaCodec?.stop()
            } catch (_: Exception) {
            }
            try {
                mediaCodec?.release()
            } catch (_: Exception) {
            }
            try {
                inputSurface?.release()
            } catch (_: Exception) {
            }
            try {
                if (muxerStarted) {
                    mediaMuxer?.stop()
                }
                mediaMuxer?.release()
            } catch (_: Exception) {
            }
            mediaCodec = null
            inputSurface = null
            mediaMuxer = null

            val file = outputFile
            if (file != null && file.exists() && file.length() > 512L) {
                val galleryUri = saveVideoToGallery(context, file)
                onFinished(
                    RecordedTapeItem(
                        file = file,
                        title = file.name,
                        presetBadge = badge,
                        durationSec = durSec,
                        isVideo = true,
                        galleryUri = galleryUri
                    )
                )
            } else {
                onFinished(null)
            }
        }
    }

    private fun drainEncoder(endOfStream: Boolean) {
        val codec = mediaCodec ?: return
        val muxer = mediaMuxer ?: return
        if (endOfStream) {
            try {
                codec.signalEndOfInputStream()
            } catch (_: Exception) {
            }
        }
        val bufferInfo = MediaCodec.BufferInfo()
        var loops = 0
        while (loops < 60) {
            loops++
            val status = codec.dequeueOutputBuffer(bufferInfo, if (endOfStream) 10_000L else 0L)
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!muxerStarted) {
                    trackIndex = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
            } else if (status >= 0) {
                val encodedData = codec.getOutputBuffer(status)
                if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                }
                codec.releaseOutputBuffer(status, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    companion object {
        fun saveVideoToGallery(context: Context, videoFile: File): Uri? {
            return try {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.name)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/AnalogTV")
                        put(MediaStore.Video.Media.IS_PENDING, 1)
                    }
                }
                val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null
                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(videoFile).use { input ->
                        input.copyTo(out)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                uri
            } catch (_: Exception) {
                null
            }
        }

        fun saveBitmapToGallery(context: Context, bitmap: Bitmap, presetName: String): RecordedTapeItem? {
            return try {
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
                if (!dir.exists()) dir.mkdirs()
                val safePreset = presetName.replace(Regex("[^A-Za-z0-9]"), "_")
                val file = File(dir, "AnalogTV_Frame_${safePreset}_${System.currentTimeMillis() % 100000}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AnalogTV")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }
                }
                RecordedTapeItem(
                    file = file,
                    title = file.name,
                    presetBadge = "PNG FRAME",
                    durationSec = 0,
                    isVideo = false,
                    galleryUri = uri
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}