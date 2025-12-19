package com.example.smartpark;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private ReservaAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_reservas);

        findViewById(R.id.btnVolverPreguntas).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recyclerReservas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReservaAdapter(listaReservas);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            cargarReservasUsuario();
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

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
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show()
                );
    }
}
