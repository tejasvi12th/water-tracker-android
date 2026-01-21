package com.example.watertracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start foreground service
        startForegroundService(Intent(this, WaterService::class.java))

        val glassText = findViewById<TextView>(R.id.glassText)
        val mlText = findViewById<TextView>(R.id.mlText)
        val streakText = findViewById<TextView>(R.id.streakText)

       val waterPrefs = getSharedPreferences("water_prefs", MODE_PRIVATE)
       val settingsPrefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)

       val waterMl = waterPrefs.getInt("water_ml", 0)

val glassSize = settingsPrefs.getInt("glass_size", 250)
val dailyGoal = settingsPrefs.getInt("daily_goal", 16)

val glasses = waterMl / glassSize

        glassText.text = "$glasses / $dailyGoal glasses"
        mlText.text = "$waterMl ml"

        // Placeholder streak (real logic comes later)
        streakText.text = "🔥 Streak: 0 days"

        // Buttons (we'll wire them next)
        findViewById<Button>(R.id.settingsButton).setOnClickListener {
    startActivity(Intent(this, SettingsActivity::class.java))
}


        findViewById<Button>(R.id.historyButton).setOnClickListener {
            // History screen coming next
        }
    }
}
