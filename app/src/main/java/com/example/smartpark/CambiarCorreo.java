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

    private EditText editEmail, editContrasena;
    private Button btnConfirm, volverBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cambiar_correo);

        editEmail = findViewById(R.id.editEmail);
        editContrasena = findViewById(R.id.contraseñaCorreo);
        btnConfirm = findViewById(R.id.btnConfirm);
        volverBtn = findViewById(R.id.atras);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        volverBtn.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> {
            String nuevoCorreo = editEmail.getText().toString().trim();
            String password = editContrasena.getText().toString().trim();
            FirebaseUser user = mAuth.getCurrentUser();

            if (user == null) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nuevoCorreo.isEmpty()) {
                Toast.makeText(this, "Introduce un nuevo correo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Si el usuario tiene login con Google (no password)
            if (user.getProviderData().get(1).getProviderId().equals("google.com")) {
                // Reautenticar con Google Credential
                user.getIdToken(true).addOnSuccessListener(result -> {
                    String token = result.getToken();
                    com.google.firebase.auth.AuthCredential credential =
                            com.google.firebase.auth.GoogleAuthProvider.getCredential(token, null);

                    user.reauthenticate(credential)
                            .addOnSuccessListener(aVoid -> actualizarCorreo(user, nuevoCorreo))
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error de reautenticación (Google): " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                });
            } else {
                // Usuarios con email y contraseña normales
                if (password.isEmpty()) {
                    Toast.makeText(this, "Introduce tu contraseña actual", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                user.reauthenticate(credential)
                        .addOnSuccessListener(aVoid -> actualizarCorreo(user, nuevoCorreo))
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error de reautenticación: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
            }
        });
    }

    private void actualizarCorreo(FirebaseUser user, String nuevoCorreo) {
        user.verifyBeforeUpdateEmail(nuevoCorreo)
                .addOnSuccessListener(unused -> {
                    db.collection("usuarios").document(user.getUid())
                            .update("correo", nuevoCorreo)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Correo actualizado. Verifica el nuevo email.", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al actualizar Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar Auth: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
