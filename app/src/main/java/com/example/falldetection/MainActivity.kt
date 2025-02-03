package com.example.falldetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*

class MainActivity : ComponentActivity() {
    private lateinit var fallDetectionManager: FallDetectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FallDetectionApp()
        }

        // Inicializa o FallDetectionManager e passa a função para atualizar a UI
        fallDetectionManager = FallDetectionManager(this) {
            // Atualiza a interface quando o status do MQTT muda
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fallDetectionManager.unregister()
    }
}

@Composable
fun FallDetectionApp() {
    // Layout principal
    val mqttStatus = "Conectando..." // Exemplo de status, pode ser atualizado dinamicamente

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sistema de Detecção de Quedas",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Exibe o status MQTT
            Text(
                text = "Status do MQTT: $mqttStatus",
                style = MaterialTheme.typography.bodyMedium,
                color = if (mqttStatus.contains("Conectado")) Color.Green else Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Aqui você pode adicionar mais componentes de feedback visual ou interações
            Button(
                onClick = {
                    // Pode adicionar ações aqui, por exemplo, para testar a detecção de queda
                }
            ) {
                Text("Testar Detecção")
            }
        }
    }
}