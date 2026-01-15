package com.example.smartpark;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ListaParkings extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ParkingAdapter adapter;
    private List<Parking> listaParkings = new ArrayList<>();
    private List<Parking> listaFiltrada = new ArrayList<>();
    private FirebaseFirestore db;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_parkings);

        findViewById(R.id.volver4).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recyclerParkings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ParkingAdapter(listaFiltrada);
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchViewParkings);
        db = FirebaseFirestore.getInstance();

        cargarParkings();
        configurarBusqueda();
    }

    /** 🔹 Carga todos los parkings de Firestore */
    private void cargarParkings() {
        db.collection("parkings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaParkings.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Parking p = doc.toObject(Parking.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            listaParkings.add(p);
                        }
                    }

                    listaFiltrada.clear();
                    listaFiltrada.addAll(listaParkings);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /** 🔹 Configura la búsqueda */
    private void configurarBusqueda() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarParkings(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarParkings(newText);
                return true;
            }
        });
    }

    /** 🔹 Filtra la lista localmente por nombre */
    private void filtrarParkings(String texto) {
        String query = texto.toLowerCase().trim();
        listaFiltrada.clear();

        if (query.isEmpty()) {
            listaFiltrada.addAll(listaParkings);
        } else {
            for (Parking p : listaParkings) {
                if (p.getNombre() != null && p.getNombre().toLowerCase().contains(query)) {
                    listaFiltrada.add(p);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}