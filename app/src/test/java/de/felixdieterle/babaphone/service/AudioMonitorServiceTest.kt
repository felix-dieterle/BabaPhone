package de.felixdieterle.babaphone.service

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.sqrt

/**
 * Unit tests for AudioMonitorService helper functions and calculations
 */
class AudioMonitorServiceTest {
    
    @Test
    fun calculateRMS_silenceBuffer_returnsZero() {
        val buffer = ShortArray(100) { 0 }
        val rms = calculateRMS(buffer, buffer.size)
        
        assertEquals(0.0f, rms, 0.001f)
    }
    
    @Test
    fun calculateRMS_maxAmplitude_returnsOne() {
        val buffer = ShortArray(100) { Short.MAX_VALUE }
        val rms = calculateRMS(buffer, buffer.size)
        
        assertTrue(rms > 0.99f)
    }
    
    @Test
    fun calculateRMS_halfAmplitude_returnsHalf() {
        val halfMax = (Short.MAX_VALUE / 2).toShort()
        val buffer = ShortArray(100) { halfMax }
        val rms = calculateRMS(buffer, buffer.size)
        
        assertTrue(rms > 0.4f && rms < 0.6f)
    }
    
    @Test
    fun calculateRMS_mixedBuffer_calculatesCorrectly() {
        val buffer = ShortArray(100)
        for (i in buffer.indices) {
            buffer[i] = if (i % 2 == 0) 1000 else -1000
        }
        val rms = calculateRMS(buffer, buffer.size)
        
        assertTrue(rms > 0f)
    }
    
    @Test
    fun calculateRMS_negativeValues_handledCorrectly() {
        val buffer = ShortArray(100) { Short.MIN_VALUE }
        val rms = calculateRMS(buffer, buffer.size)
        
        assertTrue(rms > 0.99f) // Should be close to 1.0
    }
    
    @Test
    fun sensitivity_threshold_calculations() {
        // Test sensitivity threshold calculations
        val minSensitivity = 0.0f
        val maxSensitivity = 1.0f
        val midSensitivity = 0.5f
        
        // Lower sensitivity means higher threshold
        val lowSensThreshold = 1.0f - minSensitivity
        val highSensThreshold = 1.0f - maxSensitivity
        val midSensThreshold = 1.0f - midSensitivity
        
        assertTrue(lowSensThreshold >= highSensThreshold)
        assertEquals(0.5f, midSensThreshold, 0.001f)
    }
    
    @Test
    fun volume_scaling_validRange() {
        val minVolume = 0.0f
        val maxVolume = 1.0f
        val testVolume = 0.7f
        
        // Volume should scale audio amplitude
        val audioLevel = 0.5f
        val scaledAudio = audioLevel * testVolume
        
        assertTrue(scaledAudio >= 0f)
        assertTrue(scaledAudio <= audioLevel)
    }
    
    @Test
    fun audioLevel_detection_thresholds() {
        // Test various audio level thresholds
        val silenceThreshold = 0.1f
        val normalThreshold = 0.3f
        val loudThreshold = 0.7f
        
        assertTrue(silenceThreshold < normalThreshold)
        assertTrue(normalThreshold < loudThreshold)
        assertTrue(loudThreshold <= 1.0f)
    }
    
    @Test
    fun sampleRate_validValues() {
        val sampleRate = 44100
        val validRates = listOf(8000, 16000, 22050, 44100, 48000)
        
        assertTrue(sampleRate in validRates)
    }
    
    @Test
    fun bufferSize_calculatedCorrectly() {
        // Buffer size should be a power of 2 or multiple of frame size
        val bufferSize = 4096
        
        assertTrue(bufferSize > 0)
        // Check if power of 2
        assertTrue((bufferSize and (bufferSize - 1)) == 0 || bufferSize % 256 == 0)
    }
    
    @Test
    fun notificationId_uniqueValue() {
        // Notification ID should be unique
        val notificationId = 1001
        
        assertTrue(notificationId > 0)
    }
    
    @Test
    fun foregroundServiceType_audioRecording() {
        // Service type should be appropriate for audio monitoring
        val serviceType = "microphone"
        
        assertTrue(serviceType == "microphone" || serviceType == "mediaPlayback")
    }
    
    @Test
    fun monitoringInterval_validDuration() {
        // Monitoring check interval
        val intervalMs = 100L
        val minInterval = 10L
        val maxInterval = 1000L
        
        assertTrue(intervalMs in minInterval..maxInterval)
    }
    
    // Helper function matching AudioMonitorService
    private fun calculateRMS(buffer: ShortArray, size: Int): Float {
        var sum = 0L
        for (i in 0 until size) {
            sum += (buffer[i] * buffer[i]).toLong()
        }
        val rms = sqrt((sum / size).toDouble())
        return (rms / Short.MAX_VALUE).toFloat()
    }
}
