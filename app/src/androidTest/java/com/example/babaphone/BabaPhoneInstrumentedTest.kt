package com.example.babaphone

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test for BabaPhone
 */
@RunWith(AndroidJUnit4::class)
class BabaPhoneInstrumentedTest {
    
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.babaphone", appContext.packageName)
    }
}
