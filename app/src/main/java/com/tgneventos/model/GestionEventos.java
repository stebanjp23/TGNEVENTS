package com.tgneventos.model;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.tgneventos.R;
import com.tgneventos.pojo.Event;
import com.tgneventos.recyclerviewadapters.GestionEventoItemAdapter;
import com.tgneventos.util.IniciarMenu;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class GestionEventos extends AppCompatActivity {

    private static final String TAG = "GestionEventos";
    private static final DateTimeFormatter DATE_INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_INPUT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private RecyclerView recyclerView;
    private SearchView buscar;
    private FloatingActionButton fabAnadirEvento;
    private GestionEventoItemAdapter adapter;
    private List<Event> listaEventos;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestion_eventos);

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

        recyclerView = findViewById(R.id.vista_lista_eventos);
        buscar = findViewById(R.id.buscar_evento);
        fabAnadirEvento = findViewById(R.id.anadir_evento);

        db = FirebaseFirestore.getInstance();
        listaEventos = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GestionEventoItemAdapter(listaEventos, this::mostrarDialogoEvento, this::confirmarEliminarEvento);
        recyclerView.setAdapter(adapter);

        buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        fabAnadirEvento.setOnClickListener(v -> mostrarDialogoEvento(null));

        inicializarEventosDeEjemploSiHaceFalta();
    }

    private void inicializarEventosDeEjemploSiHaceFalta() {
        db.collection("Events")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        insertarEventosDeEjemplo();
                    } else {
                        cargarEventosDeFirestore();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "No se pudo verificar si hay eventos", e);
                    cargarEventosDeFirestore();
                });
    }

    private void insertarEventosDeEjemplo() {
        List<Event> ejemplos = crearEventosDeEjemplo();
        if (ejemplos.isEmpty()) {
            cargarEventosDeFirestore();
            return;
        }

        CollectionReference eventsCollection = db.collection("Events");
        WriteBatch batch = db.batch();

        for (Event evento : ejemplos) {
            DocumentReference docRef = eventsCollection.document();
            batch.set(docRef, evento);
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Eventos de ejemplo creados", Toast.LENGTH_SHORT).show();
                    cargarEventosDeFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "No se pudieron crear eventos de ejemplo", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error insertando eventos de ejemplo", e);
                    cargarEventosDeFirestore();
                });
    }

    private List<Event> crearEventosDeEjemplo() {
        List<Event> eventos = new ArrayList<>();

        // 1. Magia - Mago Hugo
        eventos.add(crearEventoCompleto("Mago Hugo - Imagina", "Familiar",
                LocalDateTime.of(2026, 3, 13, 19, 0), "Palau de Congressos", 15.0,
                "Espectáculo de ilusionismo y magia de cerca.", "https://picsum.photos/seed/magic/800/600",
                "https://maps.app.goo.gl/9rR5S7Gv7K1uP2pB7", "https://agenda.tarragona.cat/esdeveniment/mago-hugo-imagina/9355"));

        // 2. Ópera - Norma
        eventos.add(crearEventoCompleto("Norma (Ópera)", "Musica",
                LocalDateTime.of(2026, 3, 13, 18, 0), "Teatre Tarragona", 45.0,
                "Tragedia lírica en dos actos de Vincenzo Bellini.", "https://picsum.photos/seed/opera/800/600",
                "https://maps.app.goo.gl/uXv7uF8yS6T2N1rA9", "https://agenda.tarragona.cat/esdeveniment/norma-vincenzo-bellini/9142"));

        // 3. Tributo - Brothers in Band
        eventos.add(crearEventoCompleto("Brothers in Band (Dire Straits)", "Musica",
                LocalDateTime.of(2026, 3, 14, 21, 0), "Palau de Congressos", 38.0,
                "The Very Best of Dire Straits Show.", "https://picsum.photos/seed/rock/800/600",
                "https://maps.app.goo.gl/9rR5S7Gv7K1uP2pB7", "https://agenda.tarragona.cat/esdeveniment/brothers-in-band/9344"));

        // 4. Teatro - La Tempestat
        eventos.add(crearEventoCompleto("La Tempestat", "Teatro",
                LocalDateTime.of(2026, 3, 14, 20, 0), "Teatre Tarragona", 24.0,
                "Obra de Shakespeare dirigida por Oriol Broggi.", "https://picsum.photos/seed/theater/800/600",
                "https://maps.app.goo.gl/uXv7uF8yS6T2N1rA9", "https://agenda.tarragona.cat/esdeveniment/la-tempestat-de-william-shakespeare/9138"));

        // 5. Pop - Pablo López
        eventos.add(crearEventoCompleto("Pablo López en Concierto", "Musica",
                LocalDateTime.of(2026, 3, 15, 21, 0), "Palau de Congressos", 50.0,
                "Gira 'El Niño del Espacio' en directo.", "https://picsum.photos/seed/pablo/800/600",
                "https://maps.app.goo.gl/9rR5S7Gv7K1uP2pB7", "https://agenda.tarragona.cat/esdeveniment/pablo-lopez/9350"));

        // 6. Geek - Starraco Infinity
        eventos.add(crearEventoCompleto("Starraco Infinity 2026", "Feria",
                LocalDateTime.of(2026, 3, 21, 10, 0), "Recinte Firal", 12.0,
                "Convención de ciencia ficción y fantasía.", "https://picsum.photos/seed/star/800/600",
                "https://maps.app.goo.gl/q3E6J9V8H2M1L5R4", "https://agenda.tarragona.cat/esdeveniment/starraco-infinity-2026/9200"));

        // 7. Clásica - Leonard Slatkin
        eventos.add(crearEventoCompleto("Leonard Slatkin & Simfònica", "Musica",
                LocalDateTime.of(2026, 3, 21, 20, 0), "Teatre Tarragona", 35.0,
                "Franz Schubert Filharmonia presenta la 9ª de Schubert.", "https://picsum.photos/seed/classic/800/600",
                "https://maps.app.goo.gl/uXv7uF8yS6T2N1rA9", "https://agenda.tarragona.cat/esdeveniment/leonard-slatkin-franz-schubert-filharmonia/9144"));

        // 8. Danza - Festival Dansa TGN
        eventos.add(crearEventoCompleto("Festival Dansa Tarragona", "Danza",
                LocalDateTime.of(2026, 3, 25, 18, 30), "Espais de la Ciutat", 0.0,
                "Espectáculos de danza contemporánea en la calle.", "https://picsum.photos/seed/dance/800/600",
                "https://maps.app.goo.gl/uXv7uF8yS6T2N1rA9", "https://agenda.tarragona.cat/esdeveniment/festival-dansa-tarragona/9400"));

        // 9. Guitarra - Mediterranean Fest
        eventos.add(crearEventoCompleto("Mediterranean Guitar Festival", "Musica",
                LocalDateTime.of(2026, 3, 28, 20, 30), "Església Sant Llorenç", 15.0,
                "Concierto de guitarra clásica y española.", "https://picsum.photos/seed/guitar/800/600",
                "https://maps.app.goo.gl/K9N8H7L6J5I4U3Y2", "https://agenda.tarragona.cat/esdeveniment/mediterranean-guitar-festival/9500"));

        // 10. Cultura - Portes Obertes MNAT
        eventos.add(crearEventoCompleto("Portes Obertes: MNAT", "Cultura",
                LocalDateTime.of(2026, 3, 29, 11, 0), "Museu Arqueològic", 0.0,
                "Visita gratuita al Museo Nacional Arqueológico de Tarragona.", "https://picsum.photos/seed/roma/800/600",
                "https://maps.app.goo.gl/P2O1M0L9K8J7I6H5", "https://www.mnat.cat"));

        return eventos;
    }
    // Método auxiliar actualizado para incluir los campos _ubication y _web
    private Event crearEventoCompleto(String t, String cat, LocalDateTime dt, String loc, double p, String desc, String img, String maps, String web) {
        Event e = new Event();
        e.setTitle(t);
        e.setCategory(cat);
        e.setDateTime(dt);
        e.setLocation(loc);
        e.setPrice(p);
        e.setDescription(desc);
        e.setImageUrl(img);
        e.set_ubication(maps); // Campo para Google Maps
        e.set_web(web);         // Campo para la web oficial
        return e;
    }

    private void cargarEventosDeFirestore() {
        db.collection("Events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaEventos.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setId(doc.getId());
                                listaEventos.add(event);
                            }
                        } catch (Exception parseError) {
                            Log.e(TAG, "Error leyendo evento " + doc.getId(), parseError);
                        }
                    }

                    listaEventos.sort((e1, e2) -> {
                        LocalDateTime dt1 = e1.getDateTime();
                        LocalDateTime dt2 = e2.getDateTime();
                        if (dt1 == null && dt2 == null) return 0;
                        if (dt1 == null) return 1;
                        if (dt2 == null) return -1;
                        return dt1.compareTo(dt2);
                    });

                    adapter.actualizarDatos(listaEventos);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar eventos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al cargar eventos", e);
                });
    }

    private void mostrarDialogoEvento(Event eventoExistente) {
        boolean esEdicion = eventoExistente != null;

        View formView = getLayoutInflater().inflate(R.layout.dialog_evento_form, null);

        EditText inputTitulo = formView.findViewById(R.id.input_titulo_evento);
        AutoCompleteTextView inputCategoria = formView.findViewById(R.id.input_categoria_evento);
        EditText inputFecha = formView.findViewById(R.id.input_fecha_evento);
        EditText inputHora = formView.findViewById(R.id.input_hora_evento);
        AutoCompleteTextView inputUbicacion = formView.findViewById(R.id.input_zona_evento);
        EditText inputPrecio = formView.findViewById(R.id.input_precio_evento);
        EditText inputDescripcion = formView.findViewById(R.id.input_descripcion_evento);
        EditText inputImagen = formView.findViewById(R.id.input_imagen_evento);

        // --- NUEVOS CAMPOS ---
        EditText inputUrlUbicacion = formView.findViewById(R.id.input_ubicacion_evento);
        EditText inputUrlWeb = formView.findViewById(R.id.input_web_evento);

        LocalDate fechaInicial = LocalDate.now();
        LocalTime horaInicial = LocalTime.of(12, 0);

        db.collection("Locacions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> ubicaciones = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String nombre = doc.getString("Nom");
                if (nombre != null) ubicaciones.add(nombre);
            }
            ArrayAdapter<String> adapterUbi = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ubicaciones);
            inputUbicacion.setAdapter(adapterUbi);

            // SI ES EDICIÓN: Seteamos el valor actual después de cargar la lista
            if (esEdicion && eventoExistente.getLocation() != null) {
                inputUbicacion.setText(eventoExistente.getLocation(), false);
            }
        });

        // --- CARGAR CATEGORÍAS ---
        db.collection("Categorias").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> categorias = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String nombre = doc.getString("nombre");
                if (nombre != null) categorias.add(nombre);
            }
            ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categorias);
            inputCategoria.setAdapter(adapterCat);

            // SI ES EDICIÓN: Seteamos el valor actual
            if (esEdicion && eventoExistente.getCategory() != null) {
                inputCategoria.setText(eventoExistente.getCategory(), false);
            }
        });

        if (esEdicion) {
            inputTitulo.setText(eventoExistente.getTitle());
            inputDescripcion.setText(eventoExistente.getDescription());
            inputPrecio.setText(String.valueOf(eventoExistente.getPrice()));
            inputImagen.setText(eventoExistente.getImageUrl());

            // --- SETEAR VALORES NUEVOS EN EDICIÓN ---
            inputUrlUbicacion.setText(eventoExistente.get_ubication());
            inputUrlWeb.setText(eventoExistente.get_web());

            if (eventoExistente.getDateTime() != null) {
                fechaInicial = eventoExistente.getDateTime().toLocalDate();
                horaInicial = eventoExistente.getDateTime().toLocalTime();
            }
        }

        inputFecha.setText(fechaInicial.format(DATE_INPUT_FORMAT));
        inputHora.setText(horaInicial.format(TIME_INPUT_FORMAT));

        configurarSelectorDeFecha(inputFecha);
        configurarSelectorDeHora(inputHora);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(esEdicion ? "Editar evento" : "Nuevo evento")
                .setView(formView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(esEdicion ? "Guardar" : "Crear", null)
                .create();

        dialog.setOnShowListener(dialogInterface ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    Event eventoConstruido = construirEventoDesdeFormulario(
                            inputTitulo,
                            inputCategoria,
                            inputFecha,
                            inputHora,
                            inputUbicacion,
                            inputPrecio,
                            inputDescripcion,
                            inputImagen,
                            inputUrlUbicacion, // Pasamos los nuevos campos
                            inputUrlWeb
                    );

                    if (eventoConstruido == null) return;

                    if (esEdicion) {
                        actualizarEvento(eventoExistente, eventoConstruido, dialog);
                    } else {
                        crearEvento(eventoConstruido, dialog);
                    }
                }));

        dialog.show();
    }

    private void configurarSelectorDeFecha(EditText inputFecha) {
        View.OnClickListener openDatePicker = v -> {
            LocalDate currentDate = parseDateOrNow(inputFecha.getText().toString().trim());
            DatePickerDialog pickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        LocalDate pickedDate = LocalDate.of(year, month + 1, dayOfMonth);
                        inputFecha.setText(pickedDate.format(DATE_INPUT_FORMAT));
                    },
                    currentDate.getYear(),
                    currentDate.getMonthValue() - 1,
                    currentDate.getDayOfMonth()
            );
            pickerDialog.show();
        };

        inputFecha.setOnClickListener(openDatePicker);
        inputFecha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                openDatePicker.onClick(v);
            }
        });
    }

    private void configurarSelectorDeHora(EditText inputHora) {
        View.OnClickListener openTimePicker = v -> {
            LocalTime currentTime = parseTimeOrDefault(inputHora.getText().toString().trim());
            TimePickerDialog pickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) ->
                            inputHora.setText(LocalTime.of(hourOfDay, minute).format(TIME_INPUT_FORMAT)),
                    currentTime.getHour(),
                    currentTime.getMinute(),
                    true
            );
            pickerDialog.show();
        };

        inputHora.setOnClickListener(openTimePicker);
        inputHora.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                openTimePicker.onClick(v);
            }
        });
    }

    private Event construirEventoDesdeFormulario(
            EditText inputTitulo,
            AutoCompleteTextView inputCategoria,
            EditText inputFecha,
            EditText inputHora,
            AutoCompleteTextView inputUbicacion,
            EditText inputPrecio,
            EditText inputDescripcion,
            EditText inputImagen,
            EditText inputUrlUbicacion, // Recibimos nuevos campos
            EditText inputUrlWeb
    ) {
        // ... (obtención de textos existentes)
        String titulo = inputTitulo.getText().toString().trim();
        String categoria = inputCategoria.getText().toString().trim();
        String fechaString = inputFecha.getText().toString().trim();
        String horaString = inputHora.getText().toString().trim();
        String ubicacion = inputUbicacion.getText().toString().trim();
        String precioString = inputPrecio.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();
        String imageUrl = inputImagen.getText().toString().trim();

        // Obtención de nuevos campos
        String urlUbi = inputUrlUbicacion.getText().toString().trim();
        String urlWeb = inputUrlWeb.getText().toString().trim();

        if (titulo.isEmpty()) {
            inputTitulo.setError("Titulo obligatorio");
            return null;
        }

        if (categoria.isEmpty()) {
            inputCategoria.setError("Categoria obligatorio");
            return null;
        }

        if (ubicacion.isEmpty()) {
            inputUbicacion.setError("Ubicacion obligatoria");
            return null;
        }

        if (descripcion.isEmpty()) {
            inputDescripcion.setError("Descripcion obligatoria");
            return null;
        }
        if (urlUbi.isEmpty()) {
            inputUrlUbicacion.setError("URL de ubicacion obligatoria");
            return null;
        }
        if (urlWeb.isEmpty()) {
            inputUrlWeb.setError("URL de web obligatoria");
            return null;
        }

        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(fechaString, DATE_INPUT_FORMAT);
            hora = LocalTime.parse(horaString, TIME_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }

        double precio = 0.0;
        try {
            if (!precioString.isEmpty()) precio = Double.parseDouble(precioString);
        } catch (NumberFormatException e) {
            inputPrecio.setError("Numero invalido");
            return null;
        }

        // --- CONSTRUCCIÓN DEL OBJETO ---
        Event nuevoEvento = new Event();
        nuevoEvento.setTitle(titulo);
        nuevoEvento.setCategory(categoria);
        nuevoEvento.setDateTime(LocalDateTime.of(fecha, hora));
        nuevoEvento.setLocation(ubicacion);
        nuevoEvento.setPrice(precio);
        nuevoEvento.setDescription(descripcion);
        nuevoEvento.setImageUrl(imageUrl);

        // ASIGNAMOS LOS NUEVOS CAMPOS
        nuevoEvento.set_ubication(urlUbi);
        nuevoEvento.set_web(urlWeb);

        return nuevoEvento;
    }

    private void crearEvento(Event evento, AlertDialog dialog) {
        db.collection("Events")
                .add(evento)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Evento creado correctamente", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarEventosDeFirestore();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al crear evento", Toast.LENGTH_SHORT).show());
    }

    private void actualizarEvento(Event eventoOriginal, Event eventoActualizado, AlertDialog dialog) {
        if (eventoOriginal.getId() == null || eventoOriginal.getId().isEmpty()) {
            Toast.makeText(this, "No se puede actualizar el evento seleccionado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Events")
                .document(eventoOriginal.getId())
                .set(eventoActualizado)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarEventosDeFirestore();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar evento", Toast.LENGTH_SHORT).show());
    }

    private void confirmarEliminarEvento(Event event) {
        if (event == null || event.getId() == null || event.getId().isEmpty()) {
            Toast.makeText(this, "No se puede eliminar el evento seleccionado", Toast.LENGTH_SHORT).show();
            return;
        }

        String titulo = event.getTitle() == null || event.getTitle().trim().isEmpty()
                ? "este evento"
                : event.getTitle();

        new AlertDialog.Builder(this)
                .setTitle("Eliminar evento")
                .setMessage("Seguro que deseas borrar " + titulo + "?")
                .setPositiveButton("Si, borrar", (dialog, which) -> eliminarEvento(event.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEvento(String eventId) {
        db.collection("Events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                    cargarEventosDeFirestore();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar evento", Toast.LENGTH_SHORT).show());
    }

    private LocalDate parseDateOrNow(String text) {
        try {
            return LocalDate.parse(text, DATE_INPUT_FORMAT);
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }

    private LocalTime parseTimeOrDefault(String text) {
        try {
            return LocalTime.parse(text, TIME_INPUT_FORMAT);
        } catch (Exception ignored) {
            return LocalTime.of(12, 0);
        }
    }

}