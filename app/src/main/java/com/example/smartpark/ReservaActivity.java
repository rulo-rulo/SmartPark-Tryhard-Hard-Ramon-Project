package com.example.smartpark;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserva);

        txtTitulo = findViewById(R.id.txtTituloReserva);
        txtPlazasLibres = findViewById(R.id.txtPlazasLibres);
        calendarView = findViewById(R.id.calendarView);
        btnConfirmar = findViewById(R.id.btnConfirmarReserva);

        db = FirebaseFirestore.getInstance();

        // Recibe los datos del parking desde el intent
        parkingId = getIntent().getStringExtra("parkingId");
        nombreParking = getIntent().getStringExtra("nombreParking");
        plazasTotales = getIntent().getIntExtra("plazasTotales", 0);

        txtTitulo.setText("Reservar plaza en " + nombreParking);

        // Por defecto, fecha de hoy
        fechaSeleccionada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        // Mostrar plazas para la fecha inicial
        actualizarPlazasLibres(fechaSeleccionada);

        // Cambia la fecha seleccionada cuando el usuario elige otro día
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            actualizarPlazasLibres(fechaSeleccionada);
        });

        btnConfirmar.setOnClickListener(v -> confirmarReserva());
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
        }).addOnFailureListener(e -> {
            txtPlazasLibres.setText("Error al cargar plazas");
        });
    }

    private void confirmarReserva() {
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // 🔹 1. Comprobar si el usuario ya tiene una reserva para ese día
        db.collection("usuarios")
                .document(uid)
                .collection("reservas")
                .whereEqualTo("fecha", fechaSeleccionada)
                .whereEqualTo("estado", "activa")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Ya tiene una reserva ese día
                        Toast.makeText(this, "Ya tienes una reserva activa para este día.", Toast.LENGTH_SHORT).show();
                    } else {
                        // No tiene reserva -> continuar con la creación
                        crearReserva(uid);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al comprobar reservas del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void crearReserva(String uid) {
        DocumentReference reservaDiaRef = db.collection("parkings")
                .document(parkingId)
                .collection("reservas")
                .document(fechaSeleccionada);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(reservaDiaRef);

            long reservadas = snapshot.exists() ? snapshot.getLong("plazasReservadas") : 0;

            if (reservadas < plazasTotales) {
                transaction.set(reservaDiaRef,
                        new ReservaDia(reservadas + 1),
                        com.google.firebase.firestore.SetOptions.merge());
            } else {
                throw new RuntimeException("No hay plazas disponibles para ese día");
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Reserva confirmada para " + fechaSeleccionada, Toast.LENGTH_SHORT).show();
            actualizarPlazasLibres(fechaSeleccionada);

            // 🔹 Guardar también la reserva en el usuario
            Map<String, Object> reservaUsuario = new HashMap<>();
            reservaUsuario.put("parkingId", parkingId);
            reservaUsuario.put("nombreParking", nombreParking);
            reservaUsuario.put("fecha", fechaSeleccionada);
            reservaUsuario.put("estado", "activa");
            reservaUsuario.put("timestamp", new Date());

            db.collection("usuarios")
                    .document(uid)
                    .collection("reservas")
                    .add(reservaUsuario)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Reserva guardada en tu perfil", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al guardar reserva en usuario", Toast.LENGTH_SHORT).show()
                    );

        }).addOnFailureListener(e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Clase auxiliar para crear o actualizar el documento de reservas
    public static class ReservaDia {
        private long plazasReservadas;

        public ReservaDia() {}

        public ReservaDia(long plazasReservadas) {
            this.plazasReservadas = plazasReservadas;
        }

        public long getPlazasReservadas() {
            return plazasReservadas;
        }
    }
}

/*  ᘛ⁐ᕐᐷ  <--- Pelusín  */
