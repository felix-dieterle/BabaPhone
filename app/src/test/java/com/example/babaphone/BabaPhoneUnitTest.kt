package com.example.babaphone

import org.junit.Test
import org.junit.Assert.*

/**
 * Basic unit tests for BabaPhone
 */
class BabaPhoneUnitTest {
    
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun sensitivity_rangeIsValid() {
        val sensitivity = 0.5f
        assertTrue(sensitivity >= 0f && sensitivity <= 1f)
    }
    
    @Test
    fun volume_rangeIsValid() {
        val volume = 0.8f
        assertTrue(volume >= 0f && volume <= 1f)
    }
}
