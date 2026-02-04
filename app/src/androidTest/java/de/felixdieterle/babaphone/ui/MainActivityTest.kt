package de.felixdieterle.babaphone.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.felixdieterle.babaphone.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for MainActivity
 * These tests verify the activity launches and basic functionality
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun mainActivity_launches() {
        // Verify activity launches successfully
        // ActivityScenarioRule ensures the activity is created
        activityRule.scenario.onActivity { activity ->
            assert(activity != null)
        }
    }
    
    @Test
    fun mainActivity_isNotFinishing() {
        // Verify activity is not finishing after launch
        activityRule.scenario.onActivity { activity ->
            assert(!activity.isFinishing)
        }
    }
}
