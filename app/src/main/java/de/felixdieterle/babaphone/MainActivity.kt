package de.felixdieterle.babaphone

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import de.felixdieterle.babaphone.adapter.DeviceAdapter
import de.felixdieterle.babaphone.databinding.ActivityMainBinding
import de.felixdieterle.babaphone.network.ConnectionManager
import de.felixdieterle.babaphone.network.DeviceInfo
import de.felixdieterle.babaphone.network.HotspotManager
import de.felixdieterle.babaphone.service.AudioMonitorService

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var monitorService: AudioMonitorService? = null
    private var isServiceBound = false
    private var isMonitoring = false
    private lateinit var deviceAdapter: DeviceAdapter
    private var selectedDevice: DeviceInfo? = null
    private lateinit var sharedPreferences: SharedPreferences
    
    private lateinit var connectionManager: ConnectionManager
    private lateinit var hotspotManager: HotspotManager
    private var isHotspotActive = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioMonitorService.LocalBinder
            monitorService = binder.getService()
            isServiceBound = true
            
            // Apply saved settings to the service
            loadAndApplySettings()
            
            // Set up audio level callback for child mode
            monitorService?.setAudioLevelCallback { level ->
                runOnUiThread {
                    binding.audioLevelBar.progress = (level * 100).toInt()
                }
            }
            
            // Set up device discovery callback for parent mode
            monitorService?.setDeviceDiscoveryCallback(
                onDeviceFound = { device ->
                    runOnUiThread {
                        deviceAdapter.addDevice(device)
                    }
                },
                onDeviceLost = { device ->
                    runOnUiThread {
                        deviceAdapter.removeDevice(device)
                    }
                }
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            monitorService = null
            isServiceBound = false
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.grant_permissions), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPreferences = getSharedPreferences("BabaPhonePrefs", Context.MODE_PRIVATE)
        
        // Initialize managers
        connectionManager = ConnectionManager(this)
        hotspotManager = HotspotManager(this)
        
        checkAndRequestPermissions()
        setupUI()
        setupNetworkManagers()
        loadAndApplySettings()
    }
    
    private fun setupUI() {
        // Set up device list adapter
        deviceAdapter = DeviceAdapter(mutableListOf()) { device ->
            selectedDevice = device
            Toast.makeText(this, "Selected: ${device.name}", Toast.LENGTH_SHORT).show()
        }
        binding.devicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceAdapter
        }
        
        // Show/hide UI elements based on mode
        binding.modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.parentModeRadio -> {
                    binding.devicesLabel.visibility = View.VISIBLE
                    binding.devicesRecyclerView.visibility = View.VISIBLE
                    binding.audioLevelLabel.visibility = View.GONE
                    binding.audioLevelBar.visibility = View.GONE
                    binding.modeIcon.setImageResource(R.drawable.ic_parent_mode)
                }
                R.id.childModeRadio -> {
                    binding.devicesLabel.visibility = View.GONE
                    binding.devicesRecyclerView.visibility = View.GONE
                    binding.audioLevelLabel.visibility = View.VISIBLE
                    binding.audioLevelBar.visibility = View.VISIBLE
                    binding.modeIcon.setImageResource(R.drawable.ic_child_mode)
                }
            }
        }
        
        binding.startStopButton.setOnClickListener {
            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Reload settings when returning from settings activity
        loadAndApplySettings()
        // Start monitoring connection state
        connectionManager.startMonitoring()
        updateConnectionStatus()
    }
    
    override fun onPause() {
        super.onPause()
        // Stop monitoring connection state when paused
        connectionManager.stopMonitoring()
    }
    
    private fun setupNetworkManagers() {
        // Set up connection manager listener
        connectionManager.setConnectionStateListener(object : ConnectionManager.ConnectionStateListener {
            override fun onConnectionModeChanged(mode: ConnectionManager.ConnectionMode) {
                runOnUiThread {
                    updateConnectionStatus()
                }
            }
        })
        
        // Set up hotspot manager listener
        hotspotManager.setHotspotStateListener(object : HotspotManager.HotspotStateListener {
            override fun onHotspotEnabled(config: HotspotManager.HotspotConfig) {
                runOnUiThread {
                    isHotspotActive = true
                    showHotspotInfo(config)
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.hotspot_active),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            override fun onHotspotDisabled() {
                runOnUiThread {
                    isHotspotActive = false
                    hideHotspotInfo()
                }
            }
            
            override fun onHotspotFailed(errorCode: Int) {
                runOnUiThread {
                    val errorMsg = when (errorCode) {
                        HotspotManager.ERROR_NOT_SUPPORTED -> getString(R.string.hotspot_not_supported)
                        HotspotManager.ERROR_NO_CHANNEL -> getString(R.string.hotspot_error_no_channel)
                        HotspotManager.ERROR_GENERIC -> getString(R.string.hotspot_error_generic)
                        HotspotManager.ERROR_INCOMPATIBLE_MODE -> getString(R.string.hotspot_error_incompatible)
                        HotspotManager.ERROR_TETHERING_DISALLOWED -> getString(R.string.hotspot_error_tethering)
                        HotspotManager.ERROR_SECURITY -> getString(R.string.hotspot_error_security)
                        else -> getString(R.string.hotspot_error_unknown)
                    }
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.hotspot_failed, errorMsg),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
    
    private fun updateConnectionStatus() {
        val mode = connectionManager.getCurrentConnectionMode()
        val modeText = when (mode) {
            ConnectionManager.ConnectionMode.WIFI -> getString(R.string.wifi_mode)
            ConnectionManager.ConnectionMode.HOTSPOT -> getString(R.string.hotspot_mode)
            ConnectionManager.ConnectionMode.MOBILE_DATA -> getString(R.string.mobile_data_mode)
            ConnectionManager.ConnectionMode.NONE -> getString(R.string.no_connection)
        }
        binding.connectionModeText.text = getString(R.string.connection_mode) + " " + modeText
    }
    
    private fun showHotspotInfo(config: HotspotManager.HotspotConfig) {
        binding.hotspotInfoCard.visibility = View.VISIBLE
        binding.hotspotSsidText.text = "SSID: ${config.ssid}"
        binding.hotspotPasswordText.text = "Password: ${config.password}"
    }
    
    private fun hideHotspotInfo() {
        binding.hotspotInfoCard.visibility = View.GONE
    }
    
    private fun loadAndApplySettings() {
        val sensitivity = sharedPreferences.getInt(SettingsActivity.PREF_SENSITIVITY, 50)
        val volume = sharedPreferences.getInt(SettingsActivity.PREF_VOLUME, 80)
        
        // Apply settings to service if bound
        monitorService?.setSensitivity(sensitivity / 100f)
        monitorService?.setVolume(volume / 100f)
    }
    
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    private fun startMonitoring() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, getString(R.string.grant_permissions), Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
            return
        }
        
        val isParentMode = binding.parentModeRadio.isChecked
        
        // Check connection and handle hotspot mode
        val currentConnectionMode = connectionManager.getCurrentConnectionMode()
        
        if (currentConnectionMode == ConnectionManager.ConnectionMode.NONE && !isParentMode) {
            // Child mode with no WiFi - try to create hotspot
            if (hotspotManager.isHotspotSupported()) {
                Toast.makeText(this, getString(R.string.creating_hotspot), Toast.LENGTH_SHORT).show()
                val deviceName = Build.MODEL.replace(" ", "-")
                hotspotManager.startHotspot(deviceName)
                // Continue with monitoring even if hotspot creation is pending
            } else {
                Toast.makeText(this, getString(R.string.hotspot_not_supported), Toast.LENGTH_LONG).show()
                return
            }
        }
        
        // For parent mode, check if a device is selected
        if (isParentMode && selectedDevice == null && deviceAdapter.itemCount == 0) {
            Toast.makeText(this, getString(R.string.searching_devices), Toast.LENGTH_SHORT).show()
        }
        
        val intent = Intent(this, AudioMonitorService::class.java).apply {
            putExtra("MODE", if (isParentMode) "PARENT" else "CHILD")
            selectedDevice?.let {
                putExtra("DEVICE_ADDRESS", it.address)
                putExtra("DEVICE_PORT", it.port)
                putExtra("DEVICE_NAME", it.name)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        isMonitoring = true
        updateUI()
    }
    
    private fun stopMonitoring() {
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        
        stopService(Intent(this, AudioMonitorService::class.java))
        monitorService = null
        isMonitoring = false
        selectedDevice = null
        deviceAdapter.clear()
        
        // Stop hotspot if it was created
        if (isHotspotActive) {
            hotspotManager.stopHotspot()
        }
        
        updateUI()
    }
    
    private fun updateUI() {
        if (isMonitoring) {
            binding.startStopButton.text = getString(R.string.stop_monitoring)
            binding.statusText.text = getString(R.string.listening)
            binding.modeRadioGroup.isEnabled = false
            binding.parentModeRadio.isEnabled = false
            binding.childModeRadio.isEnabled = false
        } else {
            binding.startStopButton.text = getString(R.string.start_monitoring)
            binding.statusText.text = getString(R.string.disconnected)
            binding.modeRadioGroup.isEnabled = true
            binding.parentModeRadio.isEnabled = true
            binding.childModeRadio.isEnabled = true
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, 
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
        // Stop hotspot if active
        if (isHotspotActive) {
            hotspotManager.stopHotspot()
        }
        connectionManager.stopMonitoring()
    }
}
