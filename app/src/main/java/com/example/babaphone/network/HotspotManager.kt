package com.example.babaphone.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import java.lang.reflect.Method

/**
 * Manages WiFi hotspot creation and configuration
 * Note: Hotspot APIs have changed significantly across Android versions
 */
class HotspotManager(private val context: Context) {
    
    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    
    companion object {
        private const val TAG = "HotspotManager"
        private const val HOTSPOT_SSID_PREFIX = "BabaPhone-"
    }
    
    data class HotspotConfig(
        val ssid: String,
        val password: String,
        val isActive: Boolean
    )
    
    interface HotspotStateListener {
        fun onHotspotEnabled(config: HotspotConfig)
        fun onHotspotDisabled()
        fun onHotspotFailed(error: String)
    }
    
    private var hotspotStateListener: HotspotStateListener? = null
    private var localOnlyHotspot: Any? = null // WifiManager.LocalOnlyHotspotReservation on API 26+
    private var currentHotspotConfig: HotspotConfig? = null
    
    /**
     * Set listener for hotspot state changes
     */
    fun setHotspotStateListener(listener: HotspotStateListener) {
        hotspotStateListener = listener
    }
    
    /**
     * Start a local-only hotspot (API 26+) or fall back to reflection-based approach
     * @param deviceName The device name to use in the SSID
     * @return true if hotspot creation was initiated, false otherwise
     */
    @SuppressLint("MissingPermission")
    fun startHotspot(deviceName: String): Boolean {
        val ssid = "$HOTSPOT_SSID_PREFIX$deviceName"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startLocalOnlyHotspot(ssid)
        } else {
            // For older versions, we'll just log that it's not supported
            // Reflection-based approaches are unreliable and deprecated
            Log.w(TAG, "Hotspot creation not supported on API < 26")
            hotspotStateListener?.onHotspotFailed(
                "Hotspot-Modus wird erst ab Android 8.0 (API 26) unterstützt"
            )
            false
        }
    }
    
    /**
     * Start local-only hotspot (API 26+)
     */
    @SuppressLint("MissingPermission")
    private fun startLocalOnlyHotspot(ssid: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }
        
        try {
            // For API 26+, we use LocalOnlyHotspot
            // Note: This creates a temporary hotspot that doesn't interfere with data
            @Suppress("DEPRECATION")
            wifiManager.startLocalOnlyHotspot(
                object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        Log.d(TAG, "Local-only hotspot started")
                        localOnlyHotspot = reservation
                        
                        @Suppress("DEPRECATION")
                        val config = reservation?.wifiConfiguration
                        if (config != null) {
                            @Suppress("DEPRECATION")
                            val password = config.preSharedKey ?: "no_password"
                            @Suppress("DEPRECATION")
                            val actualSsid = config.SSID ?: ssid
                            
                            currentHotspotConfig = HotspotConfig(
                                ssid = actualSsid,
                                password = password,
                                isActive = true
                            )
                            
                            hotspotStateListener?.onHotspotEnabled(currentHotspotConfig!!)
                        } else {
                            // For newer APIs, we can't get the password
                            // The system manages it automatically
                            currentHotspotConfig = HotspotConfig(
                                ssid = "BabaPhone-Hotspot",
                                password = "System-managed",
                                isActive = true
                            )
                            hotspotStateListener?.onHotspotEnabled(currentHotspotConfig!!)
                        }
                    }
                    
                    override fun onStopped() {
                        Log.d(TAG, "Local-only hotspot stopped")
                        currentHotspotConfig = null
                        localOnlyHotspot = null
                        hotspotStateListener?.onHotspotDisabled()
                    }
                    
                    override fun onFailed(reason: Int) {
                        Log.e(TAG, "Local-only hotspot failed: $reason")
                        val errorMsg = when (reason) {
                            ERROR_NO_CHANNEL -> "Kein verfügbarer Kanal"
                            ERROR_GENERIC -> "Allgemeiner Fehler"
                            ERROR_INCOMPATIBLE_MODE -> "Inkompatibler Modus"
                            ERROR_TETHERING_DISALLOWED -> "Tethering nicht erlaubt"
                            else -> "Unbekannter Fehler ($reason)"
                        }
                        currentHotspotConfig = null
                        localOnlyHotspot = null
                        hotspotStateListener?.onHotspotFailed(errorMsg)
                    }
                },
                null // Handler, null = main thread
            )
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception starting hotspot", e)
            hotspotStateListener?.onHotspotFailed("Berechtigung fehlt: ${e.message}")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error starting hotspot", e)
            hotspotStateListener?.onHotspotFailed("Fehler beim Starten: ${e.message}")
            return false
        }
    }
    
    /**
     * Stop the hotspot
     */
    fun stopHotspot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                @Suppress("DEPRECATION")
                (localOnlyHotspot as? WifiManager.LocalOnlyHotspotReservation)?.close()
                localOnlyHotspot = null
                currentHotspotConfig = null
                hotspotStateListener?.onHotspotDisabled()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping hotspot", e)
            }
        }
    }
    
    /**
     * Check if hotspot is currently active
     */
    fun isHotspotActive(): Boolean {
        return currentHotspotConfig?.isActive == true
    }
    
    /**
     * Get current hotspot configuration
     */
    fun getCurrentHotspotConfig(): HotspotConfig? {
        return currentHotspotConfig
    }
    
    /**
     * Check if device supports hotspot functionality
     */
    fun isHotspotSupported(): Boolean {
        // Local-only hotspot is supported from API 26+
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
    
    /**
     * Get a user-friendly message about hotspot support
     */
    fun getHotspotSupportMessage(): String {
        return if (isHotspotSupported()) {
            "Hotspot-Modus verfügbar"
        } else {
            "Hotspot-Modus erfordert Android 8.0 oder höher"
        }
    }
}
