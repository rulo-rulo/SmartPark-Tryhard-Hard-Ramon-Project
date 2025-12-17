package com.example.smartpark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Perfil extends AppCompatActivity {

    private TextView nombre, correo;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);

        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.correo);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            db.collection("usuarios").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombreUsuario = documentSnapshot.getString("nombre");
                            String correoUsuario = documentSnapshot.getString("correo");

                            nombre.setText(nombreUsuario);
                            correo.setText(correoUsuario);
                        } else {
                            nombre.setText("Usuario no encontrado");
                            correo.setText("");
                        }
                    })
                    .addOnFailureListener(e -> {
                        nombre.setText("Error al obtener usuario");
                        correo.setText("");
                    });
        } else {
            nombre.setText("No hay usuario autenticado");
            correo.setText("");
        }

        Button volverBtn = findViewById(R.id.volver2);
        volverBtn.setOnClickListener(view -> {
            finish();
        });

        // Botón "Cambiar Nombre" → CambiarNombre
        Button cambiarNombreBtn = findViewById(R.id.cambiarNombre);
        cambiarNombreBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, CambiarNombre.class);
            startActivity(intent);
        });

        // Botón "Cambiar Correo" → CambiarCorreo
        Button cambiarCorreoBtn = findViewById(R.id.cambiarCorreo);
        cambiarCorreoBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, CambiarCorreo.class);
            startActivity(intent);
        });

        // Botón "Cambiar Contraseña" → CambiarContrasena
        Button cambiarContrasenaBtn = findViewById(R.id.cambiarContrasena);
        cambiarContrasenaBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, CambiarContrasenyaPrimero.class);
            startActivity(intent);
        });

        // Tab "Home"
        LinearLayout homeTab = findViewById(R.id.home);
        homeTab.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, HomePage.class);
            startActivity(intent);
        });

        // Botón "Cuenta"
        LinearLayout cuentaTab = findViewById(R.id.cuenta);
        cuentaTab.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, Perfil.class);
            startActivity(intent);
        });

        // Tab "Mapas"
        LinearLayout mapasTab = findViewById(R.id.mapas);
        mapasTab.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, Mapa.class);
            startActivity(intent);
        });

        Button misReservasBtn = findViewById(R.id.misReservas);
        misReservasBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Perfil.this, ListaReservas.class);
            startActivity(intent);
        });

        // Botón de cerrar sesión
        Button cerrarSesionBtn = findViewById(R.id.cerrarSesion);
        cerrarSesionBtn.setOnClickListener(view -> {
            // Cerrar sesión en Firebase
            mAuth.signOut();

            // Redirigir a IniciarSesionActivity
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            // Evitar que el usuario vuelva al perfil con el botón atrás
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });
    }

    public void lanzarAcercaDe(View view){
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }

    public void lanzarPreguntas(View view){
        Intent i = new Intent(this, PreguntasFrecuentesActivity.class);
        startActivity(i);
    }
}