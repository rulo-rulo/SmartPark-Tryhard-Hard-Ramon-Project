package com.example.smartpark;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class ParkingMqttActivity extends AppCompatActivity implements MqttCallback {

    private static final String TAG = "MQTT";

    private final String broker = "ssl://960eb9d46fe4440688dc4ac93fd49159.s1.eu.hivemq.cloud:8883";
    private final String user = "Mari123";
    private final String pass = "Mari12345";
    private final String topicEstado = "m5/parking/estado";
    private final String topicCmd = "m5/parking/cmd";

    private MqttClient client;

    private TextView txtPlaza;
    private TextView txtLed;
    private TextView txtConn;
    private Button btnToggle;
    private int estadoLed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_mqtt);

        txtPlaza = findViewById(R.id.txtPlaza);
        txtLed = findViewById(R.id.txtLed);
        txtConn = findViewById(R.id.txtConnection);
        btnToggle = findViewById(R.id.btnToggleLed);

        String clientId = "Android-" + System.currentTimeMillis();

        try {
            client = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(user);
            options.setPassword(pass.toCharArray());
            options.setSocketFactory(getSocketFactory());
            options.setKeepAliveInterval(60);

            client.setCallback(this);
            client.connect(options);

            txtConn.setText("MQTT: conectado");

            client.subscribe(topicEstado, 1);

        } catch (Exception e) {
            txtConn.setText("MQTT: error");
            Log.e(TAG, "Error MQTT", e);
        }

        btnToggle.setOnClickListener(view -> {
            int nuevo = (estadoLed == 0) ? 1 : 0;
            enviarComandoLed(nuevo);
        });
    }

    private SSLSocketFactory getSocketFactory() throws Exception {
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, null, new SecureRandom());
        return context.getSocketFactory();
    }

    // ----------------------------------------------------------
    // Enviar JSON como Kotlin {"led":1}
    // ----------------------------------------------------------
    private void enviarComandoLed(int valor) {
        try {
            if (client == null || !client.isConnected()) {
                Log.e(TAG, "Cliente MQTT no conectado");
                return;
            }

            // Aquí estaba tu error → antes enviabas SOLO "1" o "0"
            String json = "{\"led\":" + valor + "}";

            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(1);
            client.publish(topicCmd, message);

            estadoLed = valor;

            runOnUiThread(() ->
                    btnToggle.setText((valor == 1) ? "Apagar LED" : "Encender LED")
            );

            Log.d(TAG, "Comando enviado: " + json);

        } catch (Exception e) {
            Log.e(TAG, "Error publicando comando MQTT", e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            Log.d(TAG, "Mensaje recibido: " + payload);

            JSONObject json = new JSONObject(payload);
            int led = json.optInt("led", 0);
            int plaza = json.optInt("plaza", 0);

            estadoLed = led;

            runOnUiThread(() -> {
                txtPlaza.setText(plaza == 1 ? "Plaza: OCUPADA" : "Plaza: LIBRE");
                txtLed.setText(led == 1 ? "LED: ENCENDIDO" : "LED: APAGADO");
                btnToggle.setText(led == 1 ? "Apagar LED" : "Encender LED");
            });

        } catch (Exception e) {
            Log.e(TAG, "Error procesando mensaje", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        runOnUiThread(() -> txtConn.setText("MQTT: desconectado"));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}