package com.example.babaphone.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class NetworkDiscoveryManager(private val context: Context) {
    
    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }
    
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private val discoveredDevices = mutableListOf<DeviceInfo>()
    
    companion object {
        private const val SERVICE_TYPE = "_babaphone._tcp"
        private const val TAG = "NetworkDiscovery"
        const val DEFAULT_PORT = 8888
    }
    
    interface DeviceDiscoveryListener {
        fun onDeviceFound(device: DeviceInfo)
        fun onDeviceLost(device: DeviceInfo)
    }
    
    private var deviceListener: DeviceDiscoveryListener? = null
    
    fun setDeviceDiscoveryListener(listener: DeviceDiscoveryListener) {
        deviceListener = listener
    }
    
    // Parent mode: Discover child devices
    fun startDiscovery() {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }
            
            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${service.serviceName}")
                if (service.serviceType == SERVICE_TYPE) {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e(TAG, "Resolve failed: $errorCode")
                        }
                        
                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            Log.d(TAG, "Service resolved: ${serviceInfo.serviceName}")
                            val device = DeviceInfo(
                                name = serviceInfo.serviceName,
                                address = serviceInfo.host.hostAddress ?: "",
                                port = serviceInfo.port
                            )
                            discoveredDevices.add(device)
                            deviceListener?.onDeviceFound(device)
                        }
                    })
                }
            }
            
            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${service.serviceName}")
                val lostDevice = discoveredDevices.find { it.name == service.serviceName }
                lostDevice?.let {
                    discoveredDevices.remove(it)
                    deviceListener?.onDeviceLost(it)
                }
            }
            
            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped: $serviceType")
            }
            
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code: $errorCode")
                stopDiscovery()
            }
            
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed: Error code: $errorCode")
                stopDiscovery()
            }
        }
        
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting discovery", e)
        }
    }
    
    fun stopDiscovery() {
        try {
            discoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping discovery", e)
        }
        discoveryListener = null
        discoveredDevices.clear()
    }
    
    // Child mode: Register device as discoverable
    fun registerService(deviceName: String, port: Int = DEFAULT_PORT) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = deviceName
            serviceType = SERVICE_TYPE
            setPort(port)
        }
        
        registrationListener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Registration failed: $errorCode")
            }
            
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Unregistration failed: $errorCode")
            }
            
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service registered: ${serviceInfo.serviceName}")
            }
            
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service unregistered")
            }
        }
        
        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering service", e)
        }
    }
    
    fun unregisterService() {
        try {
            registrationListener?.let { nsdManager.unregisterService(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering service", e)
        }
        registrationListener = null
    }
    
    fun getDiscoveredDevices(): List<DeviceInfo> = discoveredDevices.toList()
}
