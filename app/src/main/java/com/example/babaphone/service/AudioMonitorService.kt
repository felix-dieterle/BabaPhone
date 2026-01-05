package com.example.babaphone.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.babaphone.MainActivity
import com.example.babaphone.R
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class AudioMonitorService : Service() {
    
    private val binder = LocalBinder()
    private var isRunning = AtomicBoolean(false)
    private var sensitivity = 0.5f
    private var volume = 0.8f
    private var mode = "CHILD"
    
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingThread: Thread? = null
    
    companion object {
        private const val CHANNEL_ID = "AudioMonitorChannel"
        private const val NOTIFICATION_ID = 1
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): AudioMonitorService = this@AudioMonitorService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mode = intent?.getStringExtra("MODE") ?: "CHILD"
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        if (!isRunning.get()) {
            startAudioMonitoring()
        }
        
        return START_STICKY
    }
    
    private fun startAudioMonitoring() {
        isRunning.set(true)
        
        if (mode == "CHILD") {
            startChildMode()
        } else {
            startParentMode()
        }
    }
    
    private fun startChildMode() {
        // Child mode: Record audio and stream it
        recordingThread = thread(start = true) {
            try {
                // Check for RECORD_AUDIO permission
                if (ContextCompat.checkSelfPermission(
                        this@AudioMonitorService,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission not granted, stop the service
                    stopSelf()
                    return@thread
                }
                
                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT
                )
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioRecord = AudioRecord.Builder()
                        .setAudioSource(MediaRecorder.AudioSource.MIC)
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AUDIO_FORMAT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(CHANNEL_CONFIG)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        bufferSize
                    )
                }
                
                audioRecord?.startRecording()
                
                val buffer = ShortArray(bufferSize / 2)
                
                while (isRunning.get()) {
                    val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readResult > 0) {
                        // Check audio level against sensitivity
                        val audioLevel = calculateAudioLevel(buffer, readResult)
                        if (audioLevel > sensitivity) {
                            // In a real implementation, this would stream to parent device
                            // For now, we just process it locally
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
            }
        }
    }
    
    private fun startParentMode() {
        // Parent mode: Receive and play audio
        recordingThread = thread(start = true) {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AUDIO_FORMAT
                )
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioTrack = AudioTrack.Builder()
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AUDIO_FORMAT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    audioTrack = AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AUDIO_FORMAT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                    )
                }
                
                audioTrack?.play()
                
                // In a real implementation, this would receive audio from child devices
                // For now, we just keep the service running
                while (isRunning.get()) {
                    Thread.sleep(100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
            }
        }
    }
    
    private fun calculateAudioLevel(buffer: ShortArray, size: Int): Float {
        var sum = 0L
        for (i in 0 until size) {
            sum += (buffer[i] * buffer[i]).toLong()
        }
        val rms = Math.sqrt((sum / size).toDouble())
        return (rms / Short.MAX_VALUE).toFloat()
    }
    
    fun setSensitivity(value: Float) {
        sensitivity = value
    }
    
    fun setVolume(value: Float) {
        volume = value
        audioTrack?.setVolume(volume)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning.set(false)
        recordingThread?.join(1000)
        
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio monitoring service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.monitoring_active))
            .setContentText(getString(R.string.audio_monitoring))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
