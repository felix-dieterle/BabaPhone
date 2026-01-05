package com.example.babaphone

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.babaphone.adapter.DeviceAdapter
import com.example.babaphone.databinding.ActivityMainBinding
import com.example.babaphone.network.DeviceInfo
import com.example.babaphone.service.AudioMonitorService

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var monitorService: AudioMonitorService? = null
    private var isServiceBound = false
    private var isMonitoring = false
    private lateinit var deviceAdapter: DeviceAdapter
    private var selectedDevice: DeviceInfo? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioMonitorService.LocalBinder
            monitorService = binder.getService()
            isServiceBound = true
            
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
        
        checkAndRequestPermissions()
        setupUI()
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
                }
                R.id.childModeRadio -> {
                    binding.devicesLabel.visibility = View.GONE
                    binding.devicesRecyclerView.visibility = View.GONE
                    binding.audioLevelLabel.visibility = View.VISIBLE
                    binding.audioLevelBar.visibility = View.VISIBLE
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
        
        binding.sensitivitySeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                monitorService?.setSensitivity(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
        
        binding.volumeSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                monitorService?.setVolume(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
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
    }
}
