package com.example.smartpark;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CambiarNombre extends AppCompatActivity {

    private EditText editNombre;
    private Button btnConfirm, volverBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.cambiar_nombre);

        editNombre = findViewById(R.id.editEmail); // aquí se ingresa el nuevo nombre
        btnConfirm = findViewById(R.id.btnConfirm);
        volverBtn = findViewById(R.id.atras);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Botón "Volver"
        volverBtn.setOnClickListener(view -> finish());

        // Botón "Confirmar cambio de nombre"
        btnConfirm.setOnClickListener(view -> {
            String nuevoNombre = editNombre.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "Introduce un nombre válido", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();

                // Actualizamos el nombre en Firestore
                db.collection("usuarios").document(uid)
                        .update("nombre", nuevoNombre)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Nombre actualizado correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
