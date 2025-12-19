package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CambiarContrasenyaPrimero extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cambiar_contrasenya_primero);

        Button volverBtn = findViewById(R.id.button3);
        volverBtn.setOnClickListener(view -> {
            finish();
        });

        Button confirmarBtn = findViewById(R.id.btnConfirm);
        confirmarBtn.setOnClickListener(view -> {
            Intent intent = new Intent(CambiarContrasenyaPrimero.this, CambiarContrasenya.class);
            startActivity(intent);
        });


    }
}
