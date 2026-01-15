package com.example.smartpark;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReservaActivity extends AppCompatActivity {

    private TextView txtTitulo, txtPlazasLibres;
    private CalendarView calendarView;
    private Button btnConfirmar;

    private String parkingId;
    private String nombreParking;
    private int plazasTotales;
    private String fechaSeleccionada = "";

    private FirebaseFirestore db;

    private static final String CANAL_ID = "reservas_parking";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva);

        txtTitulo = findViewById(R.id.txtTituloReserva);
        txtPlazasLibres = findViewById(R.id.txtPlazasLibres);
        calendarView = findViewById(R.id.calendarView);
        btnConfirmar = findViewById(R.id.btnConfirmarReserva);

        db = FirebaseFirestore.getInstance();

        // 🔔 Crear canal de notificaciones (obligatorio Android 8+)
        crearCanalNotificaciones();

        parkingId = getIntent().getStringExtra("parkingId");
        nombreParking = getIntent().getStringExtra("nombreParking");
        plazasTotales = getIntent().getIntExtra("plazasTotales", 0);

        txtTitulo.setText("Reservar plaza en " + nombreParking);

        fechaSeleccionada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        actualizarPlazasLibres(fechaSeleccionada);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            fechaSeleccionada = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year, month + 1, dayOfMonth
            );
            actualizarPlazasLibres(fechaSeleccionada);
        });

        btnConfirmar.setOnClickListener(v -> confirmarReserva());
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "Reservas de parking",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canal.setDescription("Avisos sobre tus reservas");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }

    private void actualizarPlazasLibres(String fecha) {
        DocumentReference reservaDiaRef = db.collection("parkings")
                .document(parkingId)
                .collection("reservas")
                .document(fecha);

        reservaDiaRef.get().addOnSuccessListener(doc -> {
            long reservadas = 0;
            if (doc.exists() && doc.contains("plazasReservadas")) {
                reservadas = doc.getLong("plazasReservadas");
            }
            long libres = plazasTotales - reservadas;
            txtPlazasLibres.setText("Plazas libres: " + libres + " / " + plazasTotales);
        });
    }

    private void confirmarReserva() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        db.collection("usuarios")
                .document(uid)
                .collection("reservas")
                .whereEqualTo("fecha", fechaSeleccionada)
                .whereEqualTo("estado", "activa")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this,
                                "Ya tienes una reserva ese día",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        crearReserva(uid);
                    }
                });
    }

    private void crearReserva(String uid) {

        DocumentReference reservaDiaRef = db.collection("parkings")
                .document(parkingId)
                .collection("reservas")
                .document(fechaSeleccionada);

        db.runTransaction(transaction -> {

            DocumentSnapshot snapshot = transaction.get(reservaDiaRef);
            long reservadas = snapshot.exists() ?
                    snapshot.getLong("plazasReservadas") : 0;

            if (reservadas >= plazasTotales) {
                throw new RuntimeException("No hay plazas disponibles");
            }

            Map<String, Object> update = new HashMap<>();
            update.put("plazasReservadas", reservadas + 1);
            transaction.set(reservaDiaRef, update,
                    com.google.firebase.firestore.SetOptions.merge());

            return null;

        }).addOnSuccessListener(aVoid -> {

            Toast.makeText(this,
                    "Reserva confirmada para " + fechaSeleccionada,
                    Toast.LENGTH_SHORT).show();

            actualizarPlazasLibres(fechaSeleccionada);

            mostrarNotificacionReservaCreada();

            programarNotificaciones();

            // Guardar reserva en el usuario
            Map<String, Object> reservaUsuario = new HashMap<>();
            reservaUsuario.put("parkingId", parkingId);
            reservaUsuario.put("nombreParking", nombreParking);
            reservaUsuario.put("fecha", fechaSeleccionada);
            reservaUsuario.put("estado", "activa");
            reservaUsuario.put("timestamp", new Date());

            db.collection("usuarios")
                    .document(uid)
                    .collection("reservas")
                    .add(reservaUsuario);

        }).addOnFailureListener(e ->
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }


    private void mostrarNotificacionReservaCreada() {

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificacion =
                new NotificationCompat.Builder(this, CANAL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Reserva confirmada 🚗")
                        .setContentText("Reserva en " + nombreParking +
                                " para el día " + fechaSeleccionada)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(),
                notificacion.build());
    }

    private void programarNotificaciones() {

        // Programar notificación para el día siguiente
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaReserva = sdf.parse(fechaSeleccionada);
            if (fechaReserva != null) {
                // Sumar 1 día → fecha de fin
                long triggerTime = fechaReserva.getTime() + (24 * 60 * 60 * 1000);

                Intent notiIntent = new Intent(this, ReservaNotificationReceiver.class);
                notiIntent.putExtra("nombreParking", nombreParking);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        (int) System.currentTimeMillis(),
                        notiIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setExact(
                            android.app.AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void programarAlarma(AlarmManager alarmManager,
                                 long tiempo,
                                 String mensaje) {

        Intent intent = new Intent(this, ReservaNotificationReceiver.class);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("fecha", fechaSeleccionada);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tiempo,
                pendingIntent
        );
    }
}

/*  ᘛ⁐ᕐᐷ  <--- Pelusín  */