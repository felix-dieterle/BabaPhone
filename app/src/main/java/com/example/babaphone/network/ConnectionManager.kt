package com.example.babaphone.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log

/**
 * Manages network connection state and determines the best connection mode
 */
class ConnectionManager(private val context: Context) {
    
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    
    companion object {
        private const val TAG = "ConnectionManager"
    }
    
    enum class ConnectionMode {
        WIFI,           // Connected to WiFi network
        HOTSPOT,        // Using mobile hotspot
        MOBILE_DATA,    // Using mobile data (requires backend)
        NONE            // No connection available
    }
    
    interface ConnectionStateListener {
        fun onConnectionModeChanged(mode: ConnectionMode)
    }
    
    private var connectionStateListener: ConnectionStateListener? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    /**
     * Get current connection mode
     */
    fun getCurrentConnectionMode(): ConnectionMode {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return ConnectionMode.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) 
                ?: return ConnectionMode.NONE
            
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    // Check if we're connected to a hotspot or regular WiFi
                    if (isConnectedToHotspot()) {
                        ConnectionMode.HOTSPOT
                    } else {
                        ConnectionMode.WIFI
                    }
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    ConnectionMode.MOBILE_DATA
                }
                else -> ConnectionMode.NONE
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return ConnectionMode.NONE
            
            @Suppress("DEPRECATION")
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    if (isConnectedToHotspot()) {
                        ConnectionMode.HOTSPOT
                    } else {
                        ConnectionMode.WIFI
                    }
                }
                ConnectivityManager.TYPE_MOBILE -> ConnectionMode.MOBILE_DATA
                else -> ConnectionMode.NONE
            }
        }
    }
    
    /**
     * Check if WiFi is available (not necessarily connected)
     */
    fun isWifiAvailable(): Boolean {
        return wifiManager.isWifiEnabled
    }
    
    /**
     * Check if we're connected to a WiFi network (not hotspot)
     */
    fun isConnectedToWifi(): Boolean {
        return getCurrentConnectionMode() == ConnectionMode.WIFI
    }
    
    /**
     * Check if we're connected to a hotspot
     * This is a best-effort detection based on SSID patterns
     */
    private fun isConnectedToHotspot(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, we can't easily get SSID due to privacy restrictions
            // We'll assume if WiFi is on but not connected to known networks, it might be hotspot
            // This is a limitation we'll document
            return false // Cannot reliably detect on newer Android versions
        } else {
            @Suppress("DEPRECATION")
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid.replace("\"", "")
            
            // Check if SSID matches hotspot patterns
            return ssid.startsWith("BabaPhone-") || 
                   ssid.contains("AndroidAP") ||
                   ssid.contains("iPhone")
        }
    }
    
    /**
     * Check if mobile data is available
     */
    fun isMobileDataAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) 
                ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.type == ConnectivityManager.TYPE_MOBILE
        }
    }
    
    /**
     * Determine the recommended connection mode based on current state
     */
    fun getRecommendedConnectionMode(isChildMode: Boolean): ConnectionMode {
        val currentMode = getCurrentConnectionMode()
        
        return when {
            // If already connected to WiFi, use WiFi mode
            currentMode == ConnectionMode.WIFI -> ConnectionMode.WIFI
            
            // If no WiFi but in child mode, recommend hotspot
            !isWifiAvailable() && isChildMode -> ConnectionMode.HOTSPOT
            
            // If mobile data available, recommend mobile data (future implementation)
            isMobileDataAvailable() -> ConnectionMode.MOBILE_DATA
            
            // Otherwise, no connection
            else -> ConnectionMode.NONE
        }
    }
    
    /**
     * Set listener for connection state changes
     */
    fun setConnectionStateListener(listener: ConnectionStateListener) {
        connectionStateListener = listener
    }
    
    /**
     * Start monitoring network state changes
     */
    fun startMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Network available")
                    connectionStateListener?.onConnectionModeChanged(getCurrentConnectionMode())
                }
                
                override fun onLost(network: Network) {
                    Log.d(TAG, "Network lost")
                    connectionStateListener?.onConnectionModeChanged(getCurrentConnectionMode())
                }
                
                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    Log.d(TAG, "Network capabilities changed")
                    connectionStateListener?.onConnectionModeChanged(getCurrentConnectionMode())
                }
            }
            
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        }
    }
    
    /**
     * Stop monitoring network state changes
     */
    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
        networkCallback = null
    }
}
