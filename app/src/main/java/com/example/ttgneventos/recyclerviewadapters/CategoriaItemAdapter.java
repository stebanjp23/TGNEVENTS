package com.example.ttgneventos.recyclerviewadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ttgneventos.R;
import com.example.ttgneventos.pojo.Categoria;
import com.example.ttgneventos.pojo.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoriaItemAdapter extends RecyclerView.Adapter<CategoriaItemAdapter.CategoriaItem>{
    private List<Categoria> categorias;
    private List<Categoria> categoriasFull;

    public CategoriaItemAdapter(List<Categoria> categorias) {
        this.categorias = categorias;
        this.categoriasFull = categoriasFull;
    }

    @NonNull
    @Override
    public CategoriaItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapterview_usuarios_item, parent, false);
        return new CategoriaItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaItemAdapter.CategoriaItem holder, int position) {
        Categoria c = categorias.get(position);
        holder.nombre.setText(c.getNombre());
        holder.bt_eliminar.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Eliminar usuario")
                    .setMessage("¿Estás seguro de borrar a " + c.getNombre() + "?")
                    .setPositiveButton("Sí, borrar", (dialog, which) -> {
                        eliminarUsuario(c.getUid(), holder.getAdapterPosition(), holder.itemView);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
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
        public TextView nombre;
        public ImageButton bt_eliminar;

        public CategoriaItem(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtCategoria);
            bt_eliminar = itemView.findViewById(R.id.elimnar_user);
        }
    }
}
