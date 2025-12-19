package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Reserva extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reserva);

        Button volverBtn = findViewById(R.id.btn_Volver);
        volverBtn.setOnClickListener(view -> {
            finish();
        });

        // Tab "Home"
        LinearLayout homeTab = findViewById(R.id.home);
        homeTab.setOnClickListener(view -> {
            Intent intent = new Intent(Reserva.this, HomePage.class);
            startActivity(intent);
        });

        // Botón "Cuenta"
        LinearLayout cuentaTab = findViewById(R.id.cuenta);
        cuentaTab.setOnClickListener(view -> {
            Intent intent = new Intent(Reserva.this, Perfil.class);
            startActivity(intent);
        });
    }
}
