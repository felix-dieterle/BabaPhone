package de.felixdieterle.babaphone.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Manager for mobile data connections using backend server
 * 
 * This class handles:
 * - Device registration with backend server
 * - Device discovery through backend
 * - Signaling for connection establishment
 * - Audio relay as fallback when P2P connection fails
 */
class MobileDataManager(
    private val context: Context,
    private val backendUrl: String = "https://babaphone-backend.example.com"
) {
    companion object {
        private const val TAG = "MobileDataManager"
        private const val HEARTBEAT_INTERVAL = 60000L // 1 minute
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private var deviceId: String = ""
    private var isRegistered = false
    private var heartbeatRunnable: Runnable? = null
    
    interface DeviceDiscoveryCallback {
        fun onDevicesDiscovered(devices: List<DeviceInfo>)
        fun onError(error: String)
    }
    
    interface SignalCallback {
        fun onSignalReceived(signal: Signal)
        fun onError(error: String)
    }
    
    interface AudioRelayCallback {
        fun onAudioReceived(audioData: ByteArray)
        fun onError(error: String)
    }
    
    data class Signal(
        val fromDeviceId: String,
        val toDeviceId: String,
        val signalType: String,
        val data: JsonObject?
    )
    
    init {
        // Generate or retrieve device ID
        val prefs = context.getSharedPreferences("BabaPhonePrefs", Context.MODE_PRIVATE)
        deviceId = prefs.getString("device_id", null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", newId).apply()
            newId
        }
    }
    
    /**
     * Register device with backend server
     */
    fun registerDevice(deviceType: String, deviceName: String, callback: (Boolean, String?) -> Unit) {
        val json = JsonObject().apply {
            addProperty("device_id", deviceId)
            addProperty("device_type", deviceType)
            addProperty("device_name", deviceName)
        }
        
        val body = gson.toJson(json).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$backendUrl/api/register.php")
            .post(body)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to register device", e)
                callback(false, "Network error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        isRegistered = true
                        startHeartbeat()
                        Log.i(TAG, "Device registered successfully")
                        callback(true, null)
                    } else {
                        Log.e(TAG, "Registration failed: ${it.code}")
                        callback(false, "Server error: ${it.code}")
                    }
                }
            }
        })
    }
    
    /**
     * Start sending heartbeats to keep device registered
     */
    private fun startHeartbeat() {
        heartbeatRunnable = Runnable {
            sendHeartbeat()
        }
        Handler(Looper.getMainLooper()).postDelayed(
            heartbeatRunnable!!,
            HEARTBEAT_INTERVAL
        )
    }
    
    /**
     * Send heartbeat to server
     */
    private fun sendHeartbeat() {
        if (!isRegistered) return
        
        val json = JsonObject().apply {
            addProperty("device_id", deviceId)
        }
        
        val body = gson.toJson(json).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$backendUrl/api/register.php")
            .put(body)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "Heartbeat failed", e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        Log.d(TAG, "Heartbeat sent")
                        // Schedule next heartbeat
                        Handler(Looper.getMainLooper()).postDelayed(
                            heartbeatRunnable!!,
                            HEARTBEAT_INTERVAL
                        )
                    }
                }
            }
        })
    }
    
    /**
     * Discover available devices from backend
     */
    fun discoverDevices(deviceType: String, callback: DeviceDiscoveryCallback) {
        val request = Request.Builder()
            .url("$backendUrl/api/discover.php?device_type=$deviceType")
            .get()
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to discover devices", e)
                callback.onError("Network error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val body = it.body?.string()
                        try {
                            val json = gson.fromJson(body, JsonObject::class.java)
                            val devicesArray = json.getAsJsonArray("devices")
                            val devices = mutableListOf<DeviceInfo>()
                            
                            devicesArray.forEach { deviceElement ->
                                val device = deviceElement.asJsonObject
                                devices.add(
                                    DeviceInfo(
                                        name = device.get("device_name").asString,
                                        address = device.get("ip_address").asString,
                                        port = 8888, // Default port
                                        deviceId = device.get("device_id").asString
                                    )
                                )
                            }
                            
                            callback.onDevicesDiscovered(devices)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse devices", e)
                            callback.onError("Parse error: ${e.message}")
                        }
                    } else {
                        callback.onError("Server error: ${it.code}")
                    }
                }
            }
        })
    }
    
    /**
     * Send signaling data to another device
     */
    fun sendSignal(toDeviceId: String, signalType: String, data: JsonObject?, callback: (Boolean) -> Unit) {
        val json = JsonObject().apply {
            addProperty("from_device_id", deviceId)
            addProperty("to_device_id", toDeviceId)
            addProperty("signal_type", signalType)
            if (data != null) {
                add("data", data)
            }
        }
        
        val body = gson.toJson(json).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$backendUrl/api/signal.php")
            .post(body)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send signal", e)
                callback(false)
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    callback(it.isSuccessful)
                }
            }
        })
    }
    
    /**
     * Poll for pending signals
     */
    fun pollSignals(callback: SignalCallback) {
        val request = Request.Builder()
            .url("$backendUrl/api/signal.php?device_id=$deviceId")
            .get()
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val body = it.body?.string()
                        try {
                            val json = gson.fromJson(body, JsonObject::class.java)
                            val signalsArray = json.getAsJsonArray("signals")
                            
                            signalsArray.forEach { signalElement ->
                                val signalJson = signalElement.asJsonObject
                                val signal = Signal(
                                    fromDeviceId = signalJson.get("from_device_id").asString,
                                    toDeviceId = signalJson.get("to_device_id").asString,
                                    signalType = signalJson.get("signal_type").asString,
                                    data = if (signalJson.has("data")) signalJson.getAsJsonObject("data") else null
                                )
                                callback.onSignalReceived(signal)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse signals", e)
                            callback.onError("Parse error: ${e.message}")
                        }
                    }
                }
            }
        })
    }
    
    /**
     * Relay audio data through backend (fallback mode)
     */
    fun relayAudio(toDeviceId: String, audioData: ByteArray, callback: (Boolean) -> Unit) {
        val base64Audio = Base64.encodeToString(audioData, Base64.NO_WRAP)
        
        val json = JsonObject().apply {
            addProperty("from_device_id", deviceId)
            addProperty("to_device_id", toDeviceId)
            addProperty("audio_data", base64Audio)
        }
        
        val body = gson.toJson(json).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$backendUrl/api/relay.php")
            .post(body)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to relay audio", e)
                callback(false)
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    callback(it.isSuccessful)
                }
            }
        })
    }
    
    /**
     * Poll for relayed audio packets
     */
    fun pollAudioPackets(callback: AudioRelayCallback) {
        val request = Request.Builder()
            .url("$backendUrl/api/relay.php?device_id=$deviceId")
            .get()
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val body = it.body?.string()
                        try {
                            val json = gson.fromJson(body, JsonObject::class.java)
                            val packetsArray = json.getAsJsonArray("packets")
                            
                            packetsArray.forEach { packetElement ->
                                val packet = packetElement.asJsonObject
                                val audioData = Base64.decode(
                                    packet.get("audio_data").asString,
                                    Base64.NO_WRAP
                                )
                                callback.onAudioReceived(audioData)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse audio packets", e)
                            callback.onError("Parse error: ${e.message}")
                        }
                    }
                }
            }
        })
    }
    
    /**
     * Unregister device from backend
     */
    fun unregisterDevice(callback: (Boolean) -> Unit) {
        heartbeatRunnable?.let {
            Handler(Looper.getMainLooper()).removeCallbacks(it)
        }
        
        val json = JsonObject().apply {
            addProperty("device_id", deviceId)
        }
        
        val body = gson.toJson(json).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$backendUrl/api/register.php")
            .delete(body)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to unregister device", e)
                callback(false)
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    isRegistered = false
                    callback(it.isSuccessful)
                }
            }
        })
    }
    
    fun getDeviceId(): String = deviceId
    fun isDeviceRegistered(): Boolean = isRegistered
}
