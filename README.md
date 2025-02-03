# Fall Detection System 🦸‍♂️📱
A mobile application that detects falls using an accelerometer and gyroscope sensors. The system sends an alert and a notification when a fall is detected, and it also publishes the event to an MQTT broker.

## Features ✨
* Detects falls using accelerometer and gyroscope.
* Sends an MQTT message to a broker on fall detection.
* Sends real-time push notifications on the user's device.
* Simulates email and notification alerts (for demonstration purposes).
* Efficient fall detection with debouncing to avoid false positives.
  
## Technologies Used 💻
* Android (Kotlin)
* MQTT (Message Queuing Telemetry Transport)
* Sensors: Accelerometer, Gyroscope
* Push Notifications
* MQTT Broker: HiveMQ

## Setup 🛠️
1. Clone the repository:

        git clone https://github.com/yourusername/fall-detection.git

2. Open the project in Android Studio and run the app.
3. Modify the MQTT broker address if needed.
4. Build and run the application on your Android device.
   
## How it Works 🔍
The app continuously monitors the device’s accelerometer and gyroscope sensors. If the device experiences a significant change in acceleration, followed by a rotation (detected by the gyroscope), a fall is detected and confirmed. The app then sends a notification and an MQTT message to a predefined topic (fall/detection).

Sensor Thresholds:

* Acceleration Threshold: 30.0 m/s² (indicates a fall).
* Gyroscope Threshold: 8.0 rad/s (confirms the fall based on rotation).

---

# Sistema de Detecção de Queda 🦸‍♂️📱
Um aplicativo móvel que detecta quedas usando os sensores de acelerômetro e giroscópio. O sistema envia um alerta e uma notificação quando uma queda é detectada, e também publica o evento em um broker MQTT.

## Funcionalidades ✨
* Detecta quedas usando acelerômetro e giroscópio.
* Envia uma mensagem MQTT para o broker quando uma queda é detectada.
* Envia notificações push em tempo real no dispositivo do usuário.
* Simula o envio de e-mail e alertas de notificação (para fins de demonstração).
* Detecção eficiente de quedas com debounce para evitar falsos positivos.

## Tecnologias Utilizadas 💻
* Android (Kotlin)
* MQTT (Message Queuing Telemetry Transport)
* Sensores: Acelerômetro, Giroscópio
* Notificações Push
* Broker MQTT: HiveMQ
  
## Como Configurar 🛠️
1. Clone o repositório:

        git clone https://github.com/yourusername/fall-detection.git

2. Abra o projeto no Android Studio e execute o app.
3. Modifique o endereço do broker MQTT, se necessário.
4. Construa e execute o aplicativo no seu dispositivo Android.
   
## Como Funciona 🔍
O aplicativo monitora continuamente os sensores de acelerômetro e giroscópio do dispositivo. Se o dispositivo experimentar uma mudança significativa na aceleração, seguida de uma rotação (detectada pelo giroscópio), uma queda é detectada e confirmada. O aplicativo então envia uma notificação e uma mensagem MQTT para um tópico pré-definido (fall/detection).

Limiares dos Sensores:

* Limite de Aceleração: 30.0 m/s² (indica uma queda).
* Limite de Giroscópio: 8.0 rad/s (confirma a queda com base na rotação).
