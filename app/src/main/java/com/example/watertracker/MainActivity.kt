package com.example.watertracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start the foreground service (required for persistent notification)
        startForegroundService(Intent(this, WaterService::class.java))

        // Wire buttons
        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.historyButton).setOnClickListener {
            Toast.makeText(this, "Weekly history coming soon", Toast.LENGTH_SHORT).show()
        }

        // Initial UI update
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI in case settings changed
        updateUI()
    }

    private fun updateUI() {
        val waterPrefs = getSharedPreferences("water_prefs", MODE_PRIVATE)
        val settingsPrefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)

        val waterMl = waterPrefs.getInt("water_ml", 0)
        val glassSize = settingsPrefs.getInt("glass_size", 250)
        val dailyGoal = settingsPrefs.getInt("daily_goal", 16)

        val glasses = if (glassSize > 0) waterMl / glassSize else 0

        findViewById<TextView>(R.id.glassText).text = "$glasses / $dailyGoal glasses"
        findViewById<TextView>(R.id.mlText).text = "$waterMl ml"

        // Streak is placeholder for now (we'll compute it from history later)
        val streak = waterPrefs.getInt("streak", 0)
        findViewById<TextView>(R.id.streakText).text = "🔥 Streak: $streak days"
    }
}
