package com.example.watertracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaterService : Service() {

    companion object {
        const val ACTION_ADD_WATER = "ADD_WATER"
        const val CHANNEL_ID = "water_tracker_channel"
    }

    private var waterMl = 0
    private var glasses = 0
    private var lastDate = ""

    private val handler = Handler(Looper.getMainLooper())
    private val reminderRunnable = Runnable {
        showReminderNotification()
    }
    private fun calculateStreak(): Int {
    val prefs = applicationContext.getSharedPreferences("water_prefs", MODE_PRIVATE)
    val settings = applicationContext.getSharedPreferences("settings_prefs", MODE_PRIVATE)

    val glassSize = settings.getInt("glass_size", 250)
    val goal = settings.getInt("daily_goal", 16)

    var streak = 0
    var failedDays = 0

    for (date in last7Days()) {
        val ml = prefs.getInt("history_$date", -1)

        if (ml == -1) {
            failedDays++
        } else {
            val glasses = ml / glassSize
            if (glasses < goal) {
                failedDays++
            }
        }

        if (failedDays > 1) break
        streak++
    }

    // Save streak so MainActivity can show it
    prefs.edit().putInt("streak", streak).apply()

    return streak
}

    // ---------- LIFECYCLE ----------

    override fun onCreate() {
        super.onCreate()
        loadData()
        resetIfNewDay()
        startForeground(1, createNotification())
        calculateStreak()
        if (remindersEnabled()) {
            scheduleReminder()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ADD_WATER) {
            addWater()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---------- CORE LOGIC ----------

    private fun addWater() {
        val glassSize = glassSize()
        waterMl += glassSize
        glasses = waterMl / glassSize
        calculateStreak()
        saveData()
        updateNotification()
        scheduleReminder()
    }

    private fun resetIfNewDay() {
        val today = today()
        if (lastDate != today) {
            waterMl = 0
            glasses = 0
            lastDate = today
            saveData()
        }
    }

    // ---------- STORAGE ----------

   private fun saveData() {
    val prefs = applicationContext.getSharedPreferences("water_prefs", MODE_PRIVATE)

    prefs.edit()
        .putInt("water_ml", waterMl)
        .putString("date", lastDate)
        // save daily history
        .putInt("history_$lastDate", waterMl)
        .apply()
        calculateStreak()

}


    private fun loadData() {
        val prefs = applicationContext.getSharedPreferences("water_prefs", MODE_PRIVATE)
        waterMl = prefs.getInt("water_ml", 0)
        lastDate = prefs.getString("date", today()) ?: today()
        glasses = waterMl / glassSize()
    }

    // ---------- SETTINGS HELPERS ----------

    private fun glassSize(): Int {
        return applicationContext
            .getSharedPreferences("settings_prefs", MODE_PRIVATE)
            .getInt("glass_size", 250)
    }

    private fun dailyGoal(): Int {
        return applicationContext
            .getSharedPreferences("settings_prefs", MODE_PRIVATE)
            .getInt("daily_goal", 16)
    }

    private fun reminderDelayMinutes(): Long {
        return applicationContext
            .getSharedPreferences("settings_prefs", MODE_PRIVATE)
            .getInt("reminder_delay", 45)
            .toLong()
    }

    private fun remindersEnabled(): Boolean {
        return applicationContext
            .getSharedPreferences("settings_prefs", MODE_PRIVATE)
            .getBoolean("reminders_enabled", true)
    }
    
    // ---------- REMINDER ----------

    private fun scheduleReminder() {
        handler.removeCallbacks(reminderRunnable)
        if (remindersEnabled()) {
            handler.postDelayed(
                reminderRunnable,
                reminderDelayMinutes() * 60 * 1000
            )
        }
    }

    private fun showReminderNotification() {
        val manager = getSystemService(NotificationManager::class.java)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("💧 Time to drink water")
            .setContentText("Tap +250 ml when you drink")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        manager.notify(2, notification)
    }

    // ---------- NOTIFICATION ----------

    private fun createNotification(): Notification {
        createChannel()

        val addIntent = Intent(this, WaterService::class.java).apply {
            action = ACTION_ADD_WATER
        }

        val pendingIntent = PendingIntent.getService(
            this,
            0,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("💧 Water Tracker")
            .setContentText(
                "Water Today: $glasses / ${dailyGoal()} glasses ($waterMl ml)"
            )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .addAction(0, "+250 ml", pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, createNotification())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Tracker",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    // ---------- HELPERS ----------

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
    }
    private fun last7Days(): List<String> {
    val dates = mutableListOf<String>()
    val cal = java.util.Calendar.getInstance()

    for (i in 0 until 7) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(cal.time)
        dates.add(date)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
    }
    return dates
}

}
