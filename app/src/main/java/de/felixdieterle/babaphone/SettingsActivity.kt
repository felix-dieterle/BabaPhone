package de.felixdieterle.babaphone

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.felixdieterle.babaphone.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        
        sharedPreferences = getSharedPreferences("BabaPhonePrefs", Context.MODE_PRIVATE)
        
        // Load saved settings
        val sensitivity = sharedPreferences.getInt(PREF_SENSITIVITY, 50)
        val volume = sharedPreferences.getInt(PREF_VOLUME, 80)
        val mobileDataEnabled = sharedPreferences.getBoolean(PREF_MOBILE_DATA_ENABLED, false)
        val backendUrl = sharedPreferences.getString(PREF_BACKEND_URL, DEFAULT_BACKEND_URL) ?: DEFAULT_BACKEND_URL
        
        binding.sensitivitySeekBar.progress = sensitivity
        binding.volumeSeekBar.progress = volume
        binding.sensitivityValue.text = "$sensitivity%"
        binding.volumeValue.text = "$volume%"
        binding.mobileDataSwitch.isChecked = mobileDataEnabled
        binding.backendUrlInput.setText(backendUrl)
        
        // Set up listeners
        binding.sensitivitySeekBar.setOnSeekBarChangeListener(
            createSeekBarListener(binding.sensitivityValue, PREF_SENSITIVITY)
        )
        
        binding.volumeSeekBar.setOnSeekBarChangeListener(
            createSeekBarListener(binding.volumeValue, PREF_VOLUME)
        )
        
        // Mobile data switch
        binding.mobileDataSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(PREF_MOBILE_DATA_ENABLED, isChecked).apply()
        }
        
        // Backend URL input
        binding.backendUrlInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val url = binding.backendUrlInput.text.toString()
                sharedPreferences.edit().putString(PREF_BACKEND_URL, url).apply()
            }
        }
    }
    
    private fun createSeekBarListener(
        valueTextView: android.widget.TextView,
        preferenceKey: String
    ) = object : android.widget.SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
            valueTextView.text = "$progress%"
        }
        override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
            seekBar?.let {
                sharedPreferences.edit().putInt(preferenceKey, it.progress).apply()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    companion object {
        const val PREF_SENSITIVITY = "sensitivity"
        const val PREF_VOLUME = "volume"
        const val PREF_MOBILE_DATA_ENABLED = "mobile_data_enabled"
        const val PREF_BACKEND_URL = "backend_url"
        const val DEFAULT_BACKEND_URL = "https://babaphone-backend.example.com"
    }
}