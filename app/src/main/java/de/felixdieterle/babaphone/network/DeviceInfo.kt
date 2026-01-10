package de.felixdieterle.babaphone.network

/**
 * Information about a discovered device
 */
data class DeviceInfo(
    val name: String,      // Device name for display
    val address: String,   // IP address
    val port: Int,         // Port number
    val deviceId: String = "" // Unique device ID (for mobile data mode)
)
