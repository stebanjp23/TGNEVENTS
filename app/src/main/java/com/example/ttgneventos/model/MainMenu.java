package com.example.ttgneventos.model;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
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

    private FirebaseFirestore _db = null;

    // 1. VARIABLE PARA GUARDAR EL ESTADO ACTUAL
    private boolean esAdminInicial;
    private com.google.firebase.firestore.ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);


        NavigationView navView = findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();
        MenuItem itemAdmin = menu.findItem(R.id.administracion);
        itemAdmin.setVisible(getIntent().getBooleanExtra("Es_admin", false));

        IniciarMenu.setupDrawer(this, drawer, navView, toolbar, getIntent().getBooleanExtra("Es_admin", false));
        ViewCompat.setOnApplyWindowInsetsListener
        (
            findViewById(R.id.main), (v, insets) ->
            {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        );

        // 2. GUARDAMOS EL ESTADO ACTUAL
        esAdminInicial = getIntent().getBooleanExtra("Es_admin", false);

        // Initializes ID references
        _filterButton = findViewById(R.id.filterScreenButton);
        _filterButton.setOnClickListener
        (
            v ->
            {
                Intent filters = new Intent(this, Filters.class);
                filters.putExtra("Es_admin", esAdminInicial);
                startActivity(filters);
            }
        );



        // Llamamos al metodo LISTENER, para saber si el usuario es admin y se apliquen los cambios en el contenido
        setupUserPermissionListener();

        // Initializes the event item display
        RecyclerView eventDisplay = findViewById(R.id.eventDisplay);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        eventDisplay.setLayoutManager(layoutManager);

        List<Object> flattenedList = new ArrayList<>();
        EventItemAdapter adapter = new EventItemAdapter(flattenedList, esAdminInicial);
        eventDisplay.setAdapter(adapter);

        // Retrieves filters if they exist
        Filters.FilterObject filters;
        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey("Filters"))
            filters = (Filters.FilterObject) getIntent().getExtras().getSerializable("Filters");
        else
            filters = null;

        // Retrieves the events from Firestore and updates the event display
        _db = FirebaseFirestore.getInstance();
        _db.collection("Events").get().addOnSuccessListener
        (
            queryDocumentSnapshots ->
            {
                List<Event> events = new ArrayList<>();

                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Event event = document.toObject(Event.class);
                    if(event == null) continue;
                    event.setId(document.getId());

                    // Aplicar Filtros si existen
                    if (filters != null) {

                        // 1. Filtro de Fechas
                        if (filters.getStartDate() != null && event.getDate().isBefore(filters.getStartDate()) ||
                                filters.getEndDate() != null && event.getDate().isAfter(filters.getEndDate())) {
                            continue;
                        }

                        // 2. Filtro de Localización (Zona de Tarragona)
                        // filters.getCiudad() contiene el nombre de la locación seleccionada
                        if (filters.getCiudad() != null) {
                            // Suponiendo que tu POJO Event tiene el método getLocation()
                            if (event.getLocation() == null || !event.getLocation().equalsIgnoreCase(filters.getCiudad())) {
                                continue;
                            }
                        }

                        // 3. Filtro de Categoría
                        if (filters.getCategoria() != null) {
                            // Suponiendo que tu POJO Event tiene el método getCategory()
                            if (event.getCategory() == null || !event.getCategory().equalsIgnoreCase(filters.getCategoria())) {
                                continue;
                            }
                        }

                        // 4. Filtro de Búsqueda por Texto (Nombre o Descripción)
                        if (filters.getKeywords() != null && !filters.getKeywords().isEmpty()) {
                            // Tomamos la primera palabra clave (la que el usuario escribió en el EditText)
                            String searchInput = filters.getKeywords().get(0).toLowerCase();

                            // Obtenemos nombre y descripción (asegúrate de que existan en el POJO)
                            String eventName = (event.getTitle() != null) ? event.getTitle().toLowerCase() : "";
                            String eventDesc = (event.getDescription() != null) ? event.getDescription().toLowerCase() : "";

                            // Si el texto no está ni en el nombre ni en la descripción, descartamos
                            if (!eventName.contains(searchInput) && !eventDesc.contains(searchInput)) {
                                continue;
                            }
                        }
                    }

                    events.add(event);
                }


                // Sorts events by proximity of date
                events.sort((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()));
                Map<LocalDate, List<Event>> dates = new LinkedHashMap<>();
                for(Event event : events)
                {
                    LocalDate date = event.getDate();
                    if(!dates.containsKey(date)) dates.put(date, new ArrayList<>());
                    dates.get(date).add(event);
                }

                for(Map.Entry<LocalDate, List<Event>> entry : dates.entrySet())
                {
                    flattenedList.add(entry.getKey());
                    flattenedList.addAll(entry.getValue());
                }

                adapter.notifyDataSetChanged();
            }
        );
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
