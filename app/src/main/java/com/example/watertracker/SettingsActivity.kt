package com.example.watertracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)

        val glassSizeInput = findViewById<EditText>(R.id.glassSizeInput)
        val dailyGoalInput = findViewById<EditText>(R.id.dailyGoalInput)
        val reminderDelayInput = findViewById<EditText>(R.id.reminderDelayInput)
        val reminderSwitch = findViewById<Switch>(R.id.reminderSwitch)

        // Load saved values
        glassSizeInput.setText(prefs.getInt("glass_size", 250).toString())
        dailyGoalInput.setText(prefs.getInt("daily_goal", 16).toString())
        reminderDelayInput.setText(prefs.getInt("reminder_delay", 45).toString())
        reminderSwitch.isChecked = prefs.getBoolean("reminders_enabled", true)

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            prefs.edit()
                .putInt("glass_size", glassSizeInput.text.toString().toInt())
                .putInt("daily_goal", dailyGoalInput.text.toString().toInt())
                .putInt("reminder_delay", reminderDelayInput.text.toString().toInt())
                .putBoolean("reminders_enabled", reminderSwitch.isChecked)
                .apply()

            finish()
        }
    }
}
