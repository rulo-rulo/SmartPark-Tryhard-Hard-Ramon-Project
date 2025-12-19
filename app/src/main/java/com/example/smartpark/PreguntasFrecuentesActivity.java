package com.example.smartpark;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.graphics.shapes.Feature;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PreguntasFrecuentesActivity extends AppCompatActivity {

    RecyclerView recycler;
    List<Pregunta> preguntas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preguntas_frecuentes);


        findViewById(R.id.btnVolverPreguntas).setOnClickListener(v -> finish());
        recycler = findViewById(R.id.recyclerPreguntas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        preguntas = new ArrayList<>();
        preguntas.add(new Pregunta("¿Cómo reservo un parking?", "Selecciona un parking en el mapa y pulsa en 'Reservar'."));
        preguntas.add(new Pregunta("¿Puedo cancelar una reserva?", "Sí, desde el apartado 'Mis Reservas'."));
        preguntas.add(new Pregunta("¿Cómo veo los parkings que tengo reservados?", "En el menú principal selecciona 'Mis reservas'."));
        preguntas.add(new Pregunta("¿Necesito cuenta?", "Sí, es necesario registrarse para guardar reservas."));
        preguntas.add(new Pregunta("¿Puedo aparcar mi bicicleta?", "No, esta aplicación es exclusiva para barcos."));
        preguntas.add(new Pregunta("¿Puedo dormir en mi plaza si no tengo coche?", "Sí, mientras tengas tu plaza reservada y no excedas el limite de tiempo puedes hacer lo que quieras en tu plaza"));
        preguntas.add(new Pregunta("¿Puedo aparcar mi camión en una plaza para coches?", "No."));
        preguntas.add(new Pregunta("¿Que pasa si hay un peruano en mi plaza?", "¿Que?"));

        recycler.setAdapter(new PreguntasAdapter(preguntas));
    }
}