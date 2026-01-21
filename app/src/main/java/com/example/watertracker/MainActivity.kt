package com.example.watertracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(this, WaterService::class.java))

        val text = findViewById<TextView>(R.id.textView)
        text.text = "Water Tracker is running 💧"
    }
}
