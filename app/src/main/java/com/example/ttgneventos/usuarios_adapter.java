package com.example.ttgneventos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView; // IMPORTANTE
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore; // IMPORTANTE

import java.util.List;

public class usuarios_adapter extends RecyclerView.Adapter<usuarios_adapter.ViewHolder>{
    private List<Usuario> usuarios;

    public usuarios_adapter(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usuarios_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario u = usuarios.get(position);
        holder.nombre.setText(u.getNombre());
        holder.correo.setText(u.getCorreo());


        String[] opciones_permisos = {"Estandar", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(),
                android.R.layout.simple_spinner_item, opciones_permisos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.desplegable.setAdapter(adapter);

        // Seteamos la selección actual sin disparar el listener todavía
        holder.desplegable.setSelection(u.isAdmin() ? 1 : 0, false);

        holder.desplegable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                boolean nuevoEstado = (pos == 1);

                // Solo actualizamos si el estado ha cambiado realmente en Firestore
                if (nuevoEstado != u.isAdmin()) {
                    actualizarPermisoEnFirestore(u.getUid(), nuevoEstado, holder.itemView);
                    u.setAdmin(nuevoEstado); // Actualizamos el objeto local
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- Botón Eliminar ---
        holder.bt_eliminar.setOnClickListener(v -> {
            // Un aviso antes de borrar siempre viene bien
            new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Eliminar usuario")
                    .setMessage("¿Estás seguro de borrar a " + u.getNombre() + "?")
                    .setPositiveButton("Sí, borrar", (dialog, which) -> {
                        eliminarUsuario(u.getUid(), holder.getAdapterPosition(), holder.itemView);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void eliminarUsuario(String uid, int position, View view) {
        if (uid == null) return;

        // IMPORTANTE: Usamos "Usuario" (con mayúscula como descubriste antes)
        FirebaseFirestore.getInstance().collection("Usuarios").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    usuarios.remove(position); // Lo quitamos de la lista que tiene el adaptador
                    notifyItemRemoved(position); // Animación de borrado
                    notifyItemRangeChanged(position, usuarios.size()); // Refresca posiciones
                    Toast.makeText(view.getContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(view.getContext(), "Error al borrar", Toast.LENGTH_SHORT).show());
    }


    private void actualizarPermisoEnFirestore(String uid, boolean esAdmin, View view) {
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("Usuarios").document(uid)
                .update("admin", esAdmin)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(view.getContext(), "Permisos actualizados", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(view.getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre;
        public TextView correo;
        public Spinner desplegable;

        public ImageButton bt_eliminar;


        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtNombre);
            correo = itemView.findViewById(R.id.txtCorreo);
            desplegable = itemView.findViewById(R.id.op_permisos);
            bt_eliminar = itemView.findViewById(R.id.elimnar_user);
        }
    }
}