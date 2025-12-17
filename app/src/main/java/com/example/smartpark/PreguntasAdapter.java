package com.example.smartpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PreguntasAdapter extends RecyclerView.Adapter<PreguntasAdapter.ViewHolder> {

    private List<Pregunta> lista;

    public PreguntasAdapter(List<Pregunta> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pregunta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pregunta p = lista.get(position);
        holder.txtPregunta.setText(p.pregunta);
        holder.txtRespuesta.setText(p.respuesta);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtPregunta, txtRespuesta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPregunta = itemView.findViewById(R.id.txtPregunta);
            txtRespuesta = itemView.findViewById(R.id.txtRespuesta);
        }
    }
}