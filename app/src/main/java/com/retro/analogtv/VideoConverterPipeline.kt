package com.retro.analogtv

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

@OptIn(UnstableApi::class)
class VideoConverterPipeline(private val context: Context) {

    fun convertVideo(
        inputUri: Uri,
        params: AnalogTvParameters,
        videoEffects: List<Effect>,
        onCompleted: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        val outputFile = File(
            context.getExternalFilesDir(null),
            "AnalogTV_${params.presetName.replace(" ", "_")}_${System.currentTimeMillis()}.mp4"
        )

        val mediaItem = MediaItem.fromUri(inputUri)
        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setEffects(Effects(emptyList(), videoEffects))
            .build()

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    onCompleted(outputFile)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    onError(exportException.localizedMessage ?: "Export failed")
                }
            })
            .build()

        transformer.start(editedMediaItem, outputFile.absolutePath)
    }
}
