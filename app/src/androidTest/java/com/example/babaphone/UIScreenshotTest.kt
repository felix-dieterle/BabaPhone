package com.example.babaphone

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * UI Screenshot Test - Captures screenshots of the app from different states/viewpoints
 * 
 * This test simulates viewing the app from different perspectives like a 3D model:
 * - Parent mode view
 * - Child mode view
 * - Monitoring active state
 * - Different sensitivity/volume settings
 */
@RunWith(AndroidJUnit4::class)
class UIScreenshotTest {
    
    private lateinit var device: UiDevice
    private lateinit var screenshotDir: File
    
    @get:Rule
    val permissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO
        )
    }
    
    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Create screenshots directory
        val context = ApplicationProvider.getApplicationContext<Context>()
        screenshotDir = File(context.getExternalFilesDir(null), "screenshots")
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs()
        }
    }
    
    @After
    fun cleanup() {
        // Clean up screenshots after test (they'll be copied by CI)
        // Keep for debugging: screenshotDir.deleteRecursively()
    }
    
    @Test
    fun captureScreenshots_ParentMode_DefaultState() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready - verify main UI element is displayed
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Thread.sleep(500)  // Brief wait for animations to complete
            
            // Capture screenshot of Parent Mode (default)
            takeScreenshot("01_parent_mode_default")
        }
    }
    
    @Test
    fun captureScreenshots_ChildMode_DefaultState() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Switch to Child Mode
            onView(withId(R.id.childModeRadio)).perform(click())
            
            // Verify child mode UI is displayed
            onView(withId(R.id.audioLevelLabel)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Thread.sleep(300)  // Brief wait for UI transition
            
            // Capture screenshot of Child Mode
            takeScreenshot("02_child_mode_default")
        }
    }
    
    @Test
    fun captureScreenshots_ParentMode_WithDeviceList() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Ensure we're in Parent Mode (should be default)
            onView(withId(R.id.parentModeRadio)).perform(click())
            Thread.sleep(300)  // Brief wait for UI transition
            
            // Capture screenshot showing device list area
            takeScreenshot("03_parent_mode_device_list")
        }
    }
    
    @Test
    fun captureScreenshots_ChildMode_AudioLevelIndicator() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Switch to Child Mode to show audio level indicator
            onView(withId(R.id.childModeRadio)).perform(click())
            
            // Verify audio level indicator is displayed
            onView(withId(R.id.audioLevelBar)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Thread.sleep(300)  // Brief wait for UI transition
            
            // Capture screenshot showing audio level indicator
            takeScreenshot("04_child_mode_audio_indicator")
        }
    }
    
    @Test
    fun captureScreenshots_ParentMode_Landscape() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Rotate to landscape
            device.setOrientationLeft()
            Thread.sleep(500)  // Wait for rotation animation
            
            // Verify UI is still displayed after rotation
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Capture screenshot in landscape orientation
            takeScreenshot("05_parent_mode_landscape")
            
            // Rotate back to portrait
            device.setOrientationNatural()
            Thread.sleep(300)
        }
    }
    
    @Test
    fun captureScreenshots_ChildMode_Landscape() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Switch to Child Mode
            onView(withId(R.id.childModeRadio)).perform(click())
            
            // Verify child mode UI is displayed
            onView(withId(R.id.audioLevelLabel)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Rotate to landscape
            device.setOrientationLeft()
            Thread.sleep(500)  // Wait for rotation animation
            
            // Capture screenshot in landscape orientation
            takeScreenshot("06_child_mode_landscape")
            
            // Rotate back to portrait
            device.setOrientationNatural()
            Thread.sleep(300)
        }
    }
    
    @Test
    fun captureScreenshots_DifferentSensitivityLevels() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Switch to Child Mode to show sensitivity controls
            onView(withId(R.id.childModeRadio)).perform(click())
            
            // Verify sensitivity controls are displayed
            onView(withId(R.id.sensitivitySeekBar)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Thread.sleep(300)  // Brief wait for UI transition
            
            // Capture with default sensitivity
            takeScreenshot("07_sensitivity_default")
            
            // Note: SeekBar interaction would require more complex setup
            // This captures the default state which shows the controls
        }
    }
    
    @Test
    fun captureScreenshots_AllUIElements() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait for UI to be ready
            onView(withId(R.id.titleText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            
            // Capture full UI with all elements visible (Parent Mode shows most elements)
            takeScreenshot("08_full_ui_parent_mode")
            
            // Switch to Child Mode for complete coverage
            onView(withId(R.id.childModeRadio)).perform(click())
            
            // Verify child mode UI is displayed
            onView(withId(R.id.audioLevelLabel)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Thread.sleep(300)  // Brief wait for UI transition
            
            takeScreenshot("09_full_ui_child_mode")
        }
    }
    
    /**
     * Helper function to take a screenshot
     * Screenshots are saved to the app's external files directory
     * and will be collected by the CI workflow
     */
    private fun takeScreenshot(name: String) {
        val screenshotFile = File(screenshotDir, "$name.png")
        device.takeScreenshot(screenshotFile)
        
        // Log screenshot path for CI to find
        println("Screenshot saved: ${screenshotFile.absolutePath}")
    }
}
