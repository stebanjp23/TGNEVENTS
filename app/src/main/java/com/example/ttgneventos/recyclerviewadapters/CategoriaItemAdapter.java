package com.example.ttgneventos.recyclerviewadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ttgneventos.R;
import com.example.ttgneventos.pojo.Categoria;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public final class CategoriaItemAdapter extends RecyclerView.Adapter<CategoriaItemAdapter.CategoriaItem>{
    private List<Categoria> categorias;
    private List<Categoria> categoriasFull;

    public CategoriaItemAdapter(List<Categoria> categorias) {
        this.categorias = categorias;
        this.categoriasFull = new ArrayList<>(categorias);
    }

    @NonNull
    @Override
    public CategoriaItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapterview_categoria_item, parent, false);
        return new CategoriaItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaItemAdapter.CategoriaItem holder, int position) {
        Categoria c = categorias.get(position);
        holder.nombre.setOnFocusChangeListener(null);

        holder.nombre.setText(c.getNombre());

        holder.bt_editar.setOnClickListener(v -> {
            String nuevoNombre = holder.nombre.getText().toString().trim();

            // 1. Validaciones previas
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nuevoNombre.equals(c.getNombre())) {
                Toast.makeText(holder.itemView.getContext(), "No hay cambios que guardar", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Confirmar actualización")
                    .setMessage("¿Deseas cambiar el nombre de '" + c.getNombre() + "' a '" + nuevoNombre + "'?")
                    .setPositiveButton("Actualizar", (dialog, which) -> {
                        actualizarCategoriaEnFirestore(c.getUid(), nuevoNombre, holder.itemView);
                        c.setNombre(nuevoNombre); // Actualización local
                        holder.nombre.clearFocus();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        holder.nombre.setText(c.getNombre());
                    })
                    .show();
        });

        holder.bt_eliminar.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Eliminar categoría")
                    .setMessage("¿Estás seguro de borrar " + c.getNombre() + "?")
                    .setPositiveButton("Sí, borrar", (dialog, which) -> {
                        eliminarUsuario(c.getUid(), holder.getAdapterPosition(), holder.itemView);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
    private void actualizarCategoriaEnFirestore(String uid, String nuevoNombre, View view) {
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("Categorias").document(uid)
                .update("nombre", nuevoNombre)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(view.getContext(), "Categoría actualizada", Toast.LENGTH_SHORT).show();
                    // Actualizamos la lista de respaldo para que el filtro funcione con el nuevo nombre
                    this.categoriasFull = new ArrayList<>(this.categorias);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(view.getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                });
    }
    private void eliminarUsuario(String uid, int position, View view) {
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("Categorias").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    categorias.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, categorias.size());
                    Toast.makeText(view.getContext(), "Categoria eliminada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(view.getContext(), "Error al borrar", Toast.LENGTH_SHORT).show());
    }

    public void filtrar(String texto) {
        String filterPattern = texto.toLowerCase().trim();
        categorias.clear();
        if (filterPattern.isEmpty()) {
            categorias.addAll(categoriasFull);
        } else {
            for (Categoria c : categoriasFull) {
                if (c.getNombre().toLowerCase().contains(filterPattern)) {
                    categorias.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public void actualizarDatos(List<Categoria> nuevaLista) {
        this.categorias = nuevaLista;
        this.categoriasFull = new ArrayList<>(nuevaLista);
        notifyDataSetChanged();
    }

    public static class CategoriaItem extends RecyclerView.ViewHolder {
        public EditText nombre;
        public ImageButton bt_eliminar;
        public ImageButton bt_editar;

        public CategoriaItem(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtCategoria);
            bt_eliminar = itemView.findViewById(R.id.elimnar_categoria);
            bt_editar = itemView.findViewById(R.id.editar_categoria);
        }
    }
}
