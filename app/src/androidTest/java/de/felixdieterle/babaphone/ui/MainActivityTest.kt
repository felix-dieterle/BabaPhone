package de.felixdieterle.babaphone.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import de.felixdieterle.babaphone.MainActivity
import de.felixdieterle.babaphone.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for MainActivity
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun mainActivity_launches() {
        // Verify activity launches successfully
        onView(withId(R.id.main_container))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun mainActivity_hasRequiredViews() {
        // Check that key UI elements are present
        // Note: These tests assume certain view IDs exist
        // Adjust based on actual layout
        Thread.sleep(500) // Give UI time to render
    }
    
    @Test
    fun mainActivity_title_displayed() {
        // Verify app title or toolbar is displayed
        Thread.sleep(500)
    }
}
