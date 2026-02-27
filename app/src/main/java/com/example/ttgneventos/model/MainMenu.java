package com.example.ttgneventos.model;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ttgneventos.pojo.Event;
import com.example.ttgneventos.R;
import com.example.ttgneventos.recyclerviewadapters.EventItemAdapter;
import com.example.ttgneventos.util.IniciarMenu;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MainMenu extends AppCompatActivity
{
    // ID references
    private ImageButton _filterButton;

    private FirebaseFirestore db = null;

    // 1. VARIABLE PARA GUARDAR EL ESTADO ACTUAL
    private boolean esAdminInicial;
    private com.google.firebase.firestore.ListenerRegistration userListener;
    private FloatingActionButton _limpiar_filtros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        // --- Referencias UI ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        // --- Configuración Menú y Header ---
        Menu menu = navView.getMenu();
        MenuItem itemAdmin = menu.findItem(R.id.administracion);
        itemAdmin.setVisible(getIntent().getBooleanExtra("Es_admin", false));

        IniciarMenu.setupDrawer(this, drawer, navView, toolbar, getIntent().getBooleanExtra("Es_admin", false));
        IniciarMenu.actualizarEmailEnHeader(navView);

        // --- Lógica de Limpieza Unificada ---
        View.OnClickListener accionLimpiar = v -> {
            getIntent().removeExtra("Filters");
            recreate();
        };

        // --- Insets ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        esAdminInicial = getIntent().getBooleanExtra("Es_admin", false);

        _filterButton = findViewById(R.id.filterScreenButton);
        _filterButton.setOnClickListener(v -> {
            Intent intentFilters = new Intent(this, Filters.class);
            intentFilters.putExtra("Es_admin", esAdminInicial);
            startActivity(intentFilters);
        });

        setupUserPermissionListener();

        // --- Configuración RecyclerView ---
        RecyclerView eventDisplay = findViewById(R.id.eventDisplay);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        eventDisplay.setLayoutManager(layoutManager);

        List<Object> flattenedList = new ArrayList<>();
        EventItemAdapter adapter = new EventItemAdapter(flattenedList);
        eventDisplay.setAdapter(adapter);

        // CONFIGURACIÓN DE TU BOTÓN FLOTANTE
        _limpiar_filtros = findViewById(R.id.limpiar_filtros); // Usamos el ID del XML
        _limpiar_filtros.setOnClickListener(accionLimpiar);

        // Si existen filtros, mostramos tu botón flotante
        if (getIntent().hasExtra("Filters")) {
            _limpiar_filtros.show();
        } else {
            _limpiar_filtros.hide();
        }

        Filters.FilterObject filters = (Filters.FilterObject) getIntent().getSerializableExtra("Filters");

        // --- Carga de Firestore ---
        db = FirebaseFirestore.getInstance();
        db.collection("Events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            View layoutNoResults = findViewById(R.id.layoutNoResults);

            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Event event = document.toObject(Event.class);
                if (event == null) continue;

                if (filters != null) {
                    // (Tus filtros de fechas, ciudad, categoría, keywords...)
                    if (filters.getStartDate() != null && event.getDate().isBefore(filters.getStartDate()) ||
                            filters.getEndDate() != null && event.getDate().isAfter(filters.getEndDate())) continue;

                    if (filters.getCiudad() != null && (event.getLocation() == null || !event.getLocation().equalsIgnoreCase(filters.getCiudad()))) continue;

                    if (filters.getCategoria() != null && (event.getCategory() == null || !event.getCategory().equalsIgnoreCase(filters.getCategoria()))) continue;

                    if (filters.getKeywords() != null && !filters.getKeywords().isEmpty()) {
                        String searchInput = filters.getKeywords().get(0).toLowerCase();
                        String eventName = (event.getTitle() != null) ? event.getTitle().toLowerCase() : "";
                        String eventDesc = (event.getDescription() != null) ? event.getDescription().toLowerCase() : "";
                        if (!eventName.contains(searchInput) && !eventDesc.contains(searchInput)) continue;
                    }
                }
                events.add(event);
            }

            // 2. Gestión de la UI según resultados
            if (events.isEmpty()) {
                // Mostramos solo el mensaje de "No hay resultados"
                layoutNoResults.setVisibility(View.VISIBLE);
                eventDisplay.setVisibility(View.GONE);

                // Mantenemos el FAB visible para que el usuario pueda resetear desde ahí
                _limpiar_filtros.show();
            } else {
                layoutNoResults.setVisibility(View.GONE);
                eventDisplay.setVisibility(View.VISIBLE);

                // Si hay filtros, mostramos el FAB; si no, lo ocultamos
                if (getIntent().hasExtra("Filters")) {
                    _limpiar_filtros.show();
                } else {
                    _limpiar_filtros.hide();
                }

                // Ordenar y mostrar (tu lógica actual de flattenedList)
                events.sort((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()));
                flattenedList.clear();
                Map<LocalDate, List<Event>> dates = new LinkedHashMap<>();
                for(Event event : events) {
                    LocalDate date = event.getDate();
                    if(!dates.containsKey(date)) dates.put(date, new ArrayList<>());
                    dates.get(date).add(event);
                }

                for(Map.Entry<LocalDate, List<Event>> entry : dates.entrySet()) {
                    flattenedList.add(entry.getKey());
                    flattenedList.addAll(entry.getValue());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void setupUserPermissionListener() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Guardamos el listener en una variable para poder cerrarlo después
        userListener = FirebaseFirestore.getInstance().collection("Usuarios")
                .document(currentUid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;

                    if (snapshot != null && snapshot.exists()) {
                        // Extraemos el valor actual de la DB
                        Boolean nuevoEstadoAdmin = snapshot.getBoolean("admin");


                        if (nuevoEstadoAdmin == null) return;

                        // COMPARACIÓN SEGURA
                        if (nuevoEstadoAdmin != esAdminInicial) {
                            Toast.makeText(this, "Tus permisos han cambiado. Reiniciando sesión...", Toast.LENGTH_LONG).show();

                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    // 3. MUY IMPORTANTE: Limpiar el listener al destruir la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }
}
