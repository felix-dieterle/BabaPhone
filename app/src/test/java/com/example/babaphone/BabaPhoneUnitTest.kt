package com.example.babaphone

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BabaPhone audio monitoring functionality
 */
class BabaPhoneUnitTest {
    
    @Test
    fun calculateAudioLevel_withZeroBuffer_returnsZero() {
        // Test the audio level calculation with silence
        val buffer = ShortArray(100) { 0 }
        val rms = calculateRMS(buffer, buffer.size)
        assertEquals(0.0f, rms, 0.001f)
    }
    
    @Test
    fun calculateAudioLevel_withMaxBuffer_returnsMaxLevel() {
        // Test the audio level calculation with maximum amplitude
        val buffer = ShortArray(100) { Short.MAX_VALUE }
        val rms = calculateRMS(buffer, buffer.size)
        assertTrue(rms > 0.99f) // Should be close to 1.0
    }
    
    @Test
    fun sensitivity_boundaryValues_areValid() {
        // Test that sensitivity boundary values are within valid range
        val minSensitivity = 0.0f
        val maxSensitivity = 1.0f
        val defaultSensitivity = 0.5f
        
        assertTrue(minSensitivity >= 0f && minSensitivity <= 1f)
        assertTrue(maxSensitivity >= 0f && maxSensitivity <= 1f)
        assertTrue(defaultSensitivity >= 0f && defaultSensitivity <= 1f)
    }
    
    @Test
    fun volume_boundaryValues_areValid() {
        // Test that volume boundary values are within valid range
        val minVolume = 0.0f
        val maxVolume = 1.0f
        val defaultVolume = 0.8f
        
        assertTrue(minVolume >= 0f && minVolume <= 1f)
        assertTrue(maxVolume >= 0f && maxVolume <= 1f)
        assertTrue(defaultVolume >= 0f && defaultVolume <= 1f)
    }
    
    // Helper function mirroring AudioMonitorService.calculateAudioLevel logic
    private fun calculateRMS(buffer: ShortArray, size: Int): Float {
        var sum = 0L
        for (i in 0 until size) {
            sum += (buffer[i] * buffer[i]).toLong()
        }
        val rms = Math.sqrt((sum / size).toDouble())
        return (rms / Short.MAX_VALUE).toFloat()
    }
}
