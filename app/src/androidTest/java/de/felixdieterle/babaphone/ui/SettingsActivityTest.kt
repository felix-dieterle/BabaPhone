package de.felixdieterle.babaphone.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.felixdieterle.babaphone.SettingsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for SettingsActivity
 * These tests verify the settings activity launches and basic functionality
 */
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(SettingsActivity::class.java)
    
    @Test
    fun settingsActivity_launches() {
        // Verify settings activity launches successfully
        // ActivityScenarioRule ensures the activity is created
        activityRule.scenario.onActivity { activity ->
            assert(activity != null)
        }
    }
    
    @Test
    fun settingsActivity_isNotFinishing() {
        // Verify activity is not finishing after launch
        activityRule.scenario.onActivity { activity ->
            assert(!activity.isFinishing)
        }
    }
}
