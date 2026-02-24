package com.example.ttgneventos.model;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ttgneventos.pojo.Usuario;
import com.example.ttgneventos.util.IniciarMenu;
import com.example.ttgneventos.R;
import com.example.ttgneventos.recyclerviewadapters.UsuarioItemAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public final class GestionUsuarios extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UsuarioItemAdapter adapter;
    private List<Usuario> listaUsuarios;
    private FirebaseFirestore db;

    private SearchView buscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestion_usuarios);
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        IniciarMenu.setupDrawer(this, drawer, navView, toolbar, getIntent().getBooleanExtra("Es_admin", false));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Inicializar vistas y Firebase
        recyclerView = findViewById(R.id.vista_lista_usuarios); // Cambia el ID por el de tu XML
        db = FirebaseFirestore.getInstance();
        listaUsuarios = new ArrayList<>();

        // 2. Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuarioItemAdapter(listaUsuarios);
        recyclerView.setAdapter(adapter);

        // 3. Cargar datos
        cargarUsuariosDeFirestore();

        // 4. Configurar SearchView
        buscar = findViewById(R.id.buscar_usuario);
        SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        };
        buscar.setOnQueryTextListener(listener);

    }

    private void cargarUsuariosDeFirestore() {
        // Usamos "usuarios" en minúscula como en tu Login
        db.collection("Usuarios").get() //NO OLVIDAR, LA COLECCIÓN ES CON "U" EN MAYUSCULAS. NO MINUSCULAS!!!
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        listaUsuarios.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Usuario u = doc.toObject(Usuario.class);
                            if (u != null) {
                                // IMPORTANTE: Seteamos el UID del documento al objeto
                                // para que el Spinner pueda actualizar luego
                                u.setUid(doc.getId());
                                listaUsuarios.add(u);

                            }
                            adapter.notifyDataSetChanged();
                        }

                    }
                    adapter.actualizarDatos(listaUsuarios);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FIRESTORE", "Error", e);
                });
    }


}