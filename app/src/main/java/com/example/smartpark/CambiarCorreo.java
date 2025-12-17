package com.example.smartpark;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;

public class CambiarCorreo extends AppCompatActivity {

    private EditText editEmail;
    private Button btnConfirm, volverBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cambiar_correo);

        editEmail = findViewById(R.id.editEmail);
        btnConfirm = findViewById(R.id.btnConfirm);
        volverBtn = findViewById(R.id.atras);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        volverBtn.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> {
            String nuevoCorreo = editEmail.getText().toString().trim();
            FirebaseUser user = mAuth.getCurrentUser();

            if (user == null) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nuevoCorreo.isEmpty()) {
                Toast.makeText(this, "Introduce un nuevo correo", Toast.LENGTH_SHORT).show();
                return;
            }

            String actualEmail = user.getEmail();
            String contrasenaTemporal = "contraseñaDelUsuario";

            AuthCredential credential = EmailAuthProvider.getCredential(actualEmail, contrasenaTemporal);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // Cambiamos el correo en Firebase Auth
                        user.verifyBeforeUpdateEmail(nuevoCorreo)
                                .addOnSuccessListener(unused -> {
                                    // Actualiza también en Firestore
                                    db.collection("usuarios").document(user.getUid())
                                            .update("correo", nuevoCorreo)
                                            .addOnSuccessListener(aVoid2 -> {
                                                Toast.makeText(this, "Correo actualizado. Verifica el nuevo email.", Toast.LENGTH_LONG).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Error al actualizar Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al actualizar Auth: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error de reautenticación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
