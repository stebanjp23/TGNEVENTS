package com.example.ttgneventos.model;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
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

import com.example.ttgneventos.pojo.Categoria;
import com.example.ttgneventos.pojo.Usuario;
import com.example.ttgneventos.recyclerviewadapters.CategoriaItemAdapter;
import com.example.ttgneventos.recyclerviewadapters.UsuarioItemAdapter;
import com.example.ttgneventos.util.IniciarMenu;
import com.example.ttgneventos.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GestionCategorias extends AppCompatActivity {
    FloatingActionButton fabAñadir;
    private RecyclerView recyclerView;
    private CategoriaItemAdapter adapter;
    private List<Categoria> listaCategorias;
    private FirebaseFirestore db;
    private SearchView buscar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestion_categorias);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();
        MenuItem itemAdmin = menu.findItem(R.id.administracion);
        itemAdmin.setVisible(getIntent().getBooleanExtra("Es_admin", false));

        IniciarMenu.setupDrawer(this, drawer, navView, toolbar, getIntent().getBooleanExtra("Es_admin", false));
        IniciarMenu.actualizarEmailEnHeader(navView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Inicializar vistas y Firebase
        recyclerView = findViewById(R.id.vista_lista_categorias); // Cambia el ID por el de tu XML
        db = FirebaseFirestore.getInstance();
        listaCategorias = new ArrayList<>();

        // 2. Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoriaItemAdapter(listaCategorias);
        recyclerView.setAdapter(adapter);

        // 3. Cargar datos
        cargarCategoriasDeFirestore();

        // 4. Configurar SearchView
        buscar = findViewById(R.id.buscar_categoria);
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

        fabAñadir = findViewById(R.id.añadir_categoria);
        fabAñadir.setOnClickListener(v -> {
            mostrarDialogoNuevaCategoria();
        });
    }
    private void mostrarDialogoNuevaCategoria() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Ej: Conferencias, Talleres...");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nueva Categoría")
                .setMessage("Introduce el nombre de la categoría:")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = input.getText().toString().trim();
                    if (!nombre.isEmpty()) {
                        guardarCategoriaEnFirestore(nombre);
                    } else {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void guardarCategoriaEnFirestore(String nombre) {
        // Creamos un Map o el objeto POJO sin preocuparnos del UID
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombre(nombre);

        db.collection("Categorias")
                .add(nuevaCategoria)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Categoría añadida: " + nombre, Toast.LENGTH_SHORT).show();
                    cargarCategoriasDeFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarCategoriasDeFirestore() {
        db.collection("Categorias").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        listaCategorias.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Categoria c = doc.toObject(Categoria.class);
                            if (c != null) {
                                c.setUid(doc.getId());
                                listaCategorias.add(c);

                            }
                            adapter.notifyDataSetChanged();
                        }

                    }
                    adapter.actualizarDatos(listaCategorias);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FIRESTORE", "Error", e);
                });
    }
}