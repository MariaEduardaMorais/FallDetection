package com.example.falldetection

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import kotlin.math.sqrt

class FallDetectionManager(
    context: Context,
    private val updateMqttStatus: (String) -> Unit // Callback para atualizar o status MQTT
) : SensorEventListener, MqttCallback {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val mqttClient = MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId(), MemoryPersistence())

    init {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        connectMQTT()
    }

    private fun connectMQTT() {
        val options = MqttConnectOptions()
        options.isCleanSession = true
        mqttClient.connect(options)
        mqttClient.setCallback(this) // Define o callback para receber mensagens
        mqttClient.subscribe("fall/detection") // Inscreve-se no tópico
        updateMqttStatus("Conectado ao MQTT")
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
        if (magnitude > 15.0f) detectFall("Queda detectada! Aceleração: $magnitude")
    }

    private fun handleGyroscope(values: FloatArray) {
        val magnitude = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        if (magnitude > 2.0f) detectFall("Queda detectada! Giroscópio: $magnitude")
    }

    private fun detectFall(message: String) {
        Log.d("FallDetection", message)
        publishMQTT(message)
        simulateEmailAndNotification(message)
    }

    private fun publishMQTT(message: String) {
        val mqttMessage = MqttMessage(message.toByteArray())
        mqttClient.publish("fall/detection", mqttMessage)
    }

    private fun simulateEmailAndNotification(message: String) {
        Log.d("EmailSimulation", "E-mail enviado com sucesso: $message")
        Log.d("NotificationSimulation", "Notificação de queda recebida: $message")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun unregister() {
        sensorManager.unregisterListener(this)
        mqttClient.disconnect()
        updateMqttStatus("Desconectado do MQTT")
    }

    // Callbacks do MQTT
    override fun connectionLost(cause: Throwable?) {
        updateMqttStatus("Conexão MQTT perdida")
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        val receivedMessage = String(message?.payload ?: byteArrayOf())
        updateMqttStatus("Mensagem recebida: $receivedMessage")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
    }
}