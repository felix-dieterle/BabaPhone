package com.example.babaphone

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.babaphone.databinding.ActivitySettingsBinding

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
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        // Load saved settings
        val sensitivity = sharedPreferences.getInt(PREF_SENSITIVITY, 50)
        val volume = sharedPreferences.getInt(PREF_VOLUME, 80)
        
        binding.sensitivitySeekBar.progress = sensitivity
        binding.volumeSeekBar.progress = volume
        binding.sensitivityValue.text = "$sensitivity%"
        binding.volumeValue.text = "$volume%"
        
        // Set up listeners
        binding.sensitivitySeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.sensitivityValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                seekBar?.let {
                    sharedPreferences.edit().putInt(PREF_SENSITIVITY, it.progress).apply()
                }
            }
        })
        
        binding.volumeSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.volumeValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                seekBar?.let {
                    sharedPreferences.edit().putInt(PREF_VOLUME, it.progress).apply()
                }
            }
        })
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    companion object {
        const val PREF_SENSITIVITY = "sensitivity"
        const val PREF_VOLUME = "volume"
    }
}
