package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {
    EditText editTextNombre, editTextCorreo, editTextContrasena, editTextConfirmarContrasena;
    Button btnRegistro;
    FirebaseAuth mAuth;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        editTextNombre = findViewById(R.id.registroNombre);
        editTextCorreo = findViewById(R.id.registroCorreo);
        editTextContrasena = findViewById(R.id.registroContrasena);
        editTextConfirmarContrasena = findViewById(R.id.registroConfirmarContrasena);
        btnRegistro = findViewById(R.id.btnRegistro);
        textView = findViewById(R.id.gotoLogin);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button volverBtn = findViewById(R.id.volver4);
        volverBtn.setOnClickListener(view -> {
            finish();
        });


        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre, correo, contrasena, confirmarContrasena;
                nombre = editTextNombre.getText().toString();
                correo = editTextCorreo.getText().toString();
                contrasena = editTextContrasena.getText().toString();
                confirmarContrasena = editTextConfirmarContrasena.getText().toString();

                // Validaciones
                if(TextUtils.isEmpty(nombre)) {
                    Toast.makeText(RegisterActivity.this, "Introduce tu nombre", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(correo)) {
                    Toast.makeText(RegisterActivity.this, "Introduce tu correo electrónico", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(contrasena)) {
                    Toast.makeText(RegisterActivity.this, "Introduce la contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(confirmarContrasena)) {
                    Toast.makeText(RegisterActivity.this, "Confirma tu contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validar que las contraseñas coincidan
                if(!contrasena.equals(confirmarContrasena)) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validar longitud mínima de contraseña (opcional pero recomendado)
                if(contrasena.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        // Crear mapa con los datos del usuario
                                        Map<String, Object> datosUsuario = new HashMap<>();
                                        datosUsuario.put("nombre", nombre);
                                        datosUsuario.put("correo", correo);
                                        datosUsuario.put("uid", user.getUid());
                                        datosUsuario.put("rfid", "");

                                        // Para un ID incremental "manual"
                                        db.collection("usuarios")
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    int id = querySnapshot.size() + 1;
                                                    datosUsuario.put("id", id);

                                                    db.collection("usuarios")
                                                            .document(user.getUid())
                                                            .set(datosUsuario)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

                                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(RegisterActivity.this, "Error al guardar datos en Firestore", Toast.LENGTH_SHORT).show();
                                                                Log.e("FirestoreError", e.getMessage());
                                                            });
                                                });
                                    }
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Autenticación fallida.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }
}