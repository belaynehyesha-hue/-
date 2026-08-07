package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    fun startRecording(outputFile: File): Boolean {
        try {
            stopRecording()
            
            // Safe instantiation of MediaRecorder for various API levels
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "የድምጽ ቀረጻ መጀመር አልተቻለም (Failed to start recording): ${e.message}", e)
            return false
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "የድምጽ ቀረጻ ማቆም አልተቻለም (Failed to stop recording): ${e.message}", e)
        } finally {
            mediaRecorder = null
        }
    }

    fun startPlayback(filePath: String, onComplete: () -> Unit): Boolean {
        try {
            stopPlayback()
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    onComplete()
                    stopPlayback()
                }
            }
            mediaPlayer = player
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "ድምጽ ማጫወት አልተቻለም (Failed to play audio): ${e.message}", e)
            return false
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "ድምጽ ማቆም አልተቻለም (Failed to stop playback): ${e.message}", e)
        } finally {
            mediaPlayer = null
        }
    }
}
