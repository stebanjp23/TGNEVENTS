package com.tgneventos.recyclerviewadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tgneventos.R;
import com.tgneventos.pojo.Event;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GestionEventoItemAdapter extends RecyclerView.Adapter<GestionEventoItemAdapter.EventoItem> {

    public interface OnEditClickListener {
        void onEdit(Event event);
    }

    public interface OnDeleteClickListener {
        void onDelete(Event event);
    }

    private final OnEditClickListener onEditClickListener;
    private final OnDeleteClickListener onDeleteClickListener;
    private List<Event> eventos;
    private List<Event> eventosFull;

    public GestionEventoItemAdapter(
            List<Event> eventos,
            OnEditClickListener onEditClickListener,
            OnDeleteClickListener onDeleteClickListener
    ) {
        this.eventos = eventos;
        this.eventosFull = new ArrayList<>(eventos);
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public EventoItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapterview_evento_gestion_item, parent, false);
        return new EventoItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoItem holder, int position) {
        Event event = eventos.get(position);

        holder.titulo.setText(event.getTitle());

        if (event.getDateTime() != null) {
            holder.fechaHora.setText(event.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            holder.fechaHora.setText("Fecha sin definir");
        }

        String ubicacionCategoria = valueOrDash(event.getLocation()) + " - " + valueOrDash(event.getCategory());
        holder.ubicacionCategoria.setText(ubicacionCategoria);

        holder.precio.setText("Entrada: " + event.getPrice() + " EUR");

        holder.editar.setOnClickListener(v -> onEditClickListener.onEdit(event));
        holder.eliminar.setOnClickListener(v -> onDeleteClickListener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    public void filtrar(String texto) {
        String filtro = texto.toLowerCase(Locale.ROOT).trim();
        eventos.clear();

        if (filtro.isEmpty()) {
            eventos.addAll(eventosFull);
        } else {
            for (Event event : eventosFull) {
                String title = safeLower(event.getTitle());
                String category = safeLower(event.getCategory());
                String location = safeLower(event.getLocation());
                String description = safeLower(event.getDescription());

                if (title.contains(filtro)
                        || category.contains(filtro)
                        || location.contains(filtro)
                        || description.contains(filtro)) {
                    eventos.add(event);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void actualizarDatos(List<Event> nuevaLista) {
        this.eventos = nuevaLista;
        this.eventosFull = new ArrayList<>(nuevaLista);
        notifyDataSetChanged();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String valueOrDash(String value) {
        return (value == null || value.trim().isEmpty()) ? "-" : value.trim();
    }

    public static final class EventoItem extends RecyclerView.ViewHolder {
        final TextView titulo;
        final TextView fechaHora;
        final TextView ubicacionCategoria;
        final TextView precio;
        final ImageButton editar;
        final ImageButton eliminar;

        public EventoItem(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txtTituloEvento);
            fechaHora = itemView.findViewById(R.id.txtFechaHoraEvento);
            ubicacionCategoria = itemView.findViewById(R.id.txtUbicacionEvento);
            precio = itemView.findViewById(R.id.txtPrecioEvento);
            editar = itemView.findViewById(R.id.editar_evento);
            eliminar = itemView.findViewById(R.id.eliminar_evento);
        }
    }
}
