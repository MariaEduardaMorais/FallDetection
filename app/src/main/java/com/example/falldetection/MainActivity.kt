package com.example.falldetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.example.falldetection.ui.theme.FallDetectionTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.widget.TextView

class MainActivity : ComponentActivity() {
    private lateinit var fallDetectionManager: FallDetectionManager
    private lateinit var mqttStatusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mqttStatusTextView = findViewById(R.id.mqttStatusTextView)

        // Inicializa o FallDetectionManager e passa a função para atualizar o TextView
        fallDetectionManager = FallDetectionManager(this) { message ->
            runOnUiThread {
                mqttStatusTextView.text = "Status MQTT: $message"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fallDetectionManager.unregister()
    }
}