package com.example.smartpark;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private List<Parking> lista;

    public ParkingAdapter(List<Parking> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        Parking p = lista.get(position);
        holder.txtNombre.setText(p.getNombre());
        holder.txtDireccion.setText(p.getDireccion());
        holder.txtPlazas.setText("Plazas: " + p.getPlazas());

        // --- Botón MAPA ---
        holder.btnMapa.setOnClickListener(v -> {
            try {
                double lat = p.getLatitud();
                double lon = p.getLongitud();

                // URL universal que abre Google Maps o navegador
                String url = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // asegura que se abra desde el adaptador

                v.getContext().startActivity(mapIntent);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(v.getContext(), "Error al abrir el mapa", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Botón RESERVAR ---
        holder.btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReservaActivity.class);
            intent.putExtra("parkingId", p.getId());
            intent.putExtra("nombreParking", p.getNombre());
            intent.putExtra("plazasTotales", p.getPlazas());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDireccion, txtPlazas;
        Button btnMapa, btnReservar;

        ParkingViewHolder(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtDireccion = itemView.findViewById(R.id.txtDireccion);
            txtPlazas = itemView.findViewById(R.id.txtPlazas);
            btnMapa = itemView.findViewById(R.id.btnMapa);
            btnReservar = itemView.findViewById(R.id.btnReservar);
        }
    }
}
