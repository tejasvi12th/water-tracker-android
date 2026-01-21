package com.example.watertracker

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class WaterService : Service() {

    companion object {
        const val ACTION_ADD_WATER = "ADD_WATER"
        const val CHANNEL_ID = "water_tracker_channel"

        const val GLASS_ML = 250
        const val DAILY_GOAL_GLASSES = 16
        const val REMINDER_DELAY_MINUTES = 45L
    }

    private var waterMl = 0
    private var glasses = 0
    private var lastDate = ""

    private val handler = Handler(Looper.getMainLooper())
    private val reminderRunnable = Runnable {
        showReminderNotification()
    }

    override fun onCreate() {
        super.onCreate()
        loadData()
        resetIfNewDay()
        startForeground(1, createNotification())
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
        waterMl += GLASS_ML
        glasses = waterMl / GLASS_ML
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
        val prefs = getSharedPreferences("water_prefs", MODE_PRIVATE)
        prefs.edit()
            .putInt("water_ml", waterMl)
            .putString("date", lastDate)
            .apply()
    }

    private fun loadData() {
        val prefs = getSharedPreferences("water_prefs", MODE_PRIVATE)
        waterMl = prefs.getInt("water_ml", 0)
        lastDate = prefs.getString("date", today()) ?: today()
        glasses = waterMl / GLASS_ML
    }

    // ---------- REMINDER ----------

    private fun scheduleReminder() {
        handler.removeCallbacks(reminderRunnable)
        handler.postDelayed(
            reminderRunnable,
            REMINDER_DELAY_MINUTES * 2 * 1000
        )
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
                "Water Today: $glasses / $DAILY_GOAL_GLASSES glasses ($waterMl ml)"
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
}
