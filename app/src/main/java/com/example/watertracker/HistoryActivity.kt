package com.example.watertracker

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val container = findViewById<LinearLayout>(R.id.historyContainer)

        val waterPrefs = getSharedPreferences("water_prefs", MODE_PRIVATE)
        val settingsPrefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)

        val glassSize = settingsPrefs.getInt("glass_size", 250)
        val dailyGoal = settingsPrefs.getInt("daily_goal", 16)

        val cal = Calendar.getInstance()
        val formatter = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

        repeat(7) {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.time)

            val ml = waterPrefs.getInt("history_$dateKey", 0)
            val glasses = if (glassSize > 0) ml / glassSize else 0
            val success = glasses >= dailyGoal

            val row = TextView(this).apply {
                text = "${formatter.format(cal.time)}  |  $glasses / $dailyGoal glasses  ${if (success) "✅" else "❌"}"
                textSize = 16f
                setPadding(0, 12, 0, 12)
            }

            container.addView(row)
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
    }
}
