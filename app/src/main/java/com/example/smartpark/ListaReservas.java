package com.example.smartpark;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ListaReservas extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ReservaUsuario> listaReservas = new ArrayList<>();
    private List<ReservaUsuario> listaFiltrada = new ArrayList<>();
    private ReservaAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_reservas);

        // Botón Volver
        findViewById(R.id.btnVolverPreguntas).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerReservas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReservaAdapter(listaFiltrada);
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchViewReservas);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            cargarReservasUsuario();
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        configurarBusqueda();
    }

    /** 🔹 Carga todas las reservas del usuario */
    private void cargarReservasUsuario() {
        db.collection("usuarios")
                .document(user.getUid())
                .collection("reservas")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaReservas.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ReservaUsuario r = doc.toObject(ReservaUsuario.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            listaReservas.add(r);
                        }
                    }

                    // Al principio mostramos todo
                    listaFiltrada.clear();
                    listaFiltrada.addAll(listaReservas);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show()
                );
    }

    /** 🔹 Configura el listener de búsqueda */
    private void configurarBusqueda() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarReservas(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarReservas(newText);
                return true;
            }
        });
    }

    /** 🔹 Filtra la lista en tiempo real */
    private void filtrarReservas(String texto) {
        String query = texto.toLowerCase().trim();
        listaFiltrada.clear();

        if (query.isEmpty()) {
            // Mostrar todo si no hay texto
            listaFiltrada.addAll(listaReservas);
        } else {
            for (ReservaUsuario reserva : listaReservas) {
                if (reserva.getNombreParking() != null &&
                        reserva.getNombreParking().toLowerCase().contains(query)) {
                    listaFiltrada.add(reserva);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}