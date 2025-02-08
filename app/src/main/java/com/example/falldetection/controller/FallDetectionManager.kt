package com.example.falldetection.controller

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.falldetection.MainActivity
import com.example.falldetection.R
import com.example.falldetection.databaselite.FallDatabase
import com.example.falldetection.entity.FallEntity
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.net.ssl.SSLSocketFactory
import kotlin.math.sqrt

class FallDetectionManager(
    private val context: Context,
    private val updateMqttStatus: (String) -> Unit
) : SensorEventListener, MqttCallback {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val mqttClient = MqttClient("ssl://broker.hivemq.com:8883", MqttClient.generateClientId(), MemoryPersistence())

    // Variáveis para controle de debounce e sensibilidade
    private var lastFallDetectionTime: Long = 0
    private val debounceTime = 5000 // 5 segundos entre detecções
    private val accelerationThreshold = 20.0f // Limite de aceleração para detectar queda
    private val gyroscopeThreshold = 8.0f // Limite de giroscópio para confirmar queda

    private var possibleFallTime: Long = 0
    private var detectedAcceleration = false

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "fall_detection_channel"

    init {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        createNotificationChannel()
        connectMQTT()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fall Detection",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de detecção de queda"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun connectMQTT() {
        val options = MqttConnectOptions()
        options.isCleanSession = true

        try {
            val sslSocketFactory = SSLSocketFactory.getDefault()
            options.socketFactory = sslSocketFactory

            mqttClient.connect(options)
            mqttClient.setCallback(this)
            mqttClient.subscribe("fall/detection")
            updateMqttStatus("Conectado ao MQTT com SSL/TLS")
        } catch (e: Exception) {
            Log.e("MQTT", "Erro ao conectar com SSL: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAcceleration(it.values)
                Sensor.TYPE_GYROSCOPE -> handleGyroscope(it.values)
            }
        }
    }

    private fun handleAcceleration(values: FloatArray) {
        val magnitude = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        if (magnitude > accelerationThreshold) {
            possibleFallTime = System.currentTimeMillis()
            detectedAcceleration = true
        }
    }

    private fun handleGyroscope(values: FloatArray) {
        val magnitude = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        if (detectedAcceleration && magnitude > gyroscopeThreshold) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - possibleFallTime < debounceTime) {
                "Queda confirmada! Aceleração e giroscópio detectaram impacto.".detectFall()
                detectedAcceleration = false
            }
        }
    }

    private fun String.detectFall() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFallDetectionTime > debounceTime) {
            lastFallDetectionTime = currentTime
            Log.d("FallDetection", this)
            publishMQTT(this)
            showNotification(this)
        }
    }

    private fun publishMQTT(message: String) {
        val mqttMessage = MqttMessage(message.toByteArray())
        mqttClient.publish("fall/detection", mqttMessage)
    }

    private fun showNotification(message: String) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Alerta de Queda!")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun unregister() {
        sensorManager.unregisterListener(this)
        mqttClient.disconnect()
        updateMqttStatus("Desconectado do MQTT")
    }

    override fun connectionLost(cause: Throwable?) {
        updateMqttStatus("Conexão MQTT perdida")
        reconnectMQTT()
    }

    private fun reconnectMQTT() {
        val retryDelay = 5000L
        Thread {
            try {
                Thread.sleep(retryDelay)
                while (!mqttClient.isConnected) {
                    try {
                        mqttClient.connect(MqttConnectOptions().apply { isCleanSession = true })
                        mqttClient.setCallback(this)
                        mqttClient.subscribe("fall/detection")
                        updateMqttStatus("Reconectado ao MQTT")
                        break
                    } catch (e: Exception) {
                        Log.e("MQTT", "Falha ao reconectar: ${e.message}")
                        Thread.sleep(retryDelay)
                    }
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }.start()
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        val receivedMessage = String(message?.payload ?: byteArrayOf())
        updateMqttStatus("Mensagem recebida: $receivedMessage")

        val fallDatabase = FallDatabase.getDatabase(context)
        val fallDao = fallDatabase.fallDao()

        Thread {
            fallDao.insertFall(FallEntity(timestamp = System.currentTimeMillis(), message = receivedMessage))
        }.start()
    }

    fun getFallHistory(context: Context): List<FallEntity> {
        val fallDatabase = FallDatabase.getDatabase(context)
        val fallDao = fallDatabase.fallDao()
        return fallDao.getAllFalls()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
}
