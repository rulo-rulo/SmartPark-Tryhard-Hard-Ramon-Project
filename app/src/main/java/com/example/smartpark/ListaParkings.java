package com.example.smartpark;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.lista_parkings);

        recyclerView = findViewById(R.id.recyclerParkings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParkingAdapter(listaParkings);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        cargarParkings();
    }

    private void cargarParkings() {
        db.collection("parkings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaParkings.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Parking p = doc.toObject(Parking.class);
                        p.setId(doc.getId());
                        listaParkings.add(p);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
