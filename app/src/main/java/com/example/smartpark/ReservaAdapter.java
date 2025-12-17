package com.example.smartpark;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {
    private List<ReservaUsuario> lista;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ReservaAdapter(List<ReservaUsuario> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        ReservaUsuario r = lista.get(position);

        holder.txtNombre.setText(r.getNombreParking());
        holder.txtFecha.setText("Fecha: " + r.getFecha());

        // Botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> eliminarReserva(r, position, v));

        // Botón Farolillo
        holder.btnFarolillo.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ParkingMqttActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtFecha;
        Button btnEliminar, btnFarolillo;

        ReservaViewHolder(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreParkingReserva);
            txtFecha = itemView.findViewById(R.id.txtFechaReserva);
            btnEliminar = itemView.findViewById(R.id.btnEliminarReserva);
            btnFarolillo = itemView.findViewById(R.id.btnFarolillo);
        }
    }

    private void eliminarReserva(ReservaUsuario reserva, int position, View v) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference reservaRef = db.collection("usuarios")
                .document(uid)
                .collection("reservas")
                .document(reserva.getId());

        reservaRef.delete()
                .addOnSuccessListener(aVoid -> {
                    DocumentReference reservaDiaRef = db.collection("parkings")
                            .document(reserva.getParkingId())
                            .collection("reservas")
                            .document(reserva.getFecha());

                    reservaDiaRef.update("plazasReservadas", FieldValue.increment(-1));

                    lista.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(v.getContext(), "Reserva eliminada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(v.getContext(), "Error al eliminar reserva", Toast.LENGTH_SHORT).show()
                );
    }
}
