package com.example.smartpark;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CambiarContrasenya extends AppCompatActivity {

    private EditText editTextNueva, editTextConfirmar;
    private Button btnConfirm, volverBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cambiar_contrasenya);

        editTextNueva = findViewById(R.id.editText1);
        editTextConfirmar = findViewById(R.id.editText2);
        btnConfirm = findViewById(R.id.button2);
        volverBtn = findViewById(R.id.button3);

        mAuth = FirebaseAuth.getInstance();

        // Botón "Volver"
        volverBtn.setOnClickListener(view -> finish());

        // Botón "Confirmar cambio de contraseña"
        btnConfirm.setOnClickListener(view -> {
            String nuevaPass = editTextNueva.getText().toString().trim();
            String confirmarPass = editTextConfirmar.getText().toString().trim();

            if (nuevaPass.isEmpty() || confirmarPass.isEmpty()) {
                Toast.makeText(this, "Rellena ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!nuevaPass.equals(confirmarPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nuevaPass.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // Actualizamos la contraseña
                currentUser.updatePassword(nuevaPass)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                            finish(); // vuelve a la pantalla anterior
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al actualizar contraseña: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
