package com.example.falldetection

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var fallDetectionManager: FallDetectionManager
    private lateinit var mqttStatusTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mqttStatusTextView = findViewById(R.id.mqttStatusTextView)

        fallDetectionManager = FallDetectionManager(this) { status ->
            runOnUiThread {
                mqttStatusTextView.text = "Status do MQTT: $status"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fallDetectionManager.unregister()
    }
}
