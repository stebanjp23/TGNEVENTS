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
import android.widget.Spinner;
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

        eventos.add(crearEventoEjemplo(
                "Tarraco Viva: Recreacion Romana",
                "Cultura",
                LocalDateTime.of(2026, 5, 17, 18, 0),
                "Anfiteatro de Tarragona",
                12.0,
                "Visitas teatralizadas y recreaciones historicas del mundo romano.",
                "https://picsum.photos/id/1040/1200/800"
        ));

        eventos.add(crearEventoEjemplo(
                "Festival Dixieland Tarragona",
                "Musica",
                LocalDateTime.of(2026, 4, 25, 20, 0),
                "Teatre Metropol",
                15.0,
                "Noche de jazz en el centro historico con bandas invitadas.",
                "https://picsum.photos/id/1011/1200/800"
        ));

        eventos.add(crearEventoEjemplo(
                "Concierto de Santa Tecla",
                "Fiesta",
                LocalDateTime.of(2026, 9, 20, 21, 30),
                "Placa de la Font",
                0.0,
                "Concierto al aire libre dentro del programa de fiestas de Santa Tecla.",
                "https://picsum.photos/id/1062/1200/800"
        ));

        eventos.add(crearEventoEjemplo(
                "Concurs de Castells",
                "Tradicion",
                LocalDateTime.of(2026, 10, 3, 12, 0),
                "Tarraco Arena Placa",
                18.0,
                "Jornada principal del concurso con colles de toda Catalunya.",
                "https://picsum.photos/id/1025/1200/800"
        ));

        return eventos;
    }

    private Event crearEventoEjemplo(
            String titulo,
            String categoria,
            LocalDateTime fechaHora,
            String ubicacion,
            double precio,
            String descripcion,
            String imageUrl
    ) {
        Event evento = new Event();
        evento.setTitle(titulo);
        evento.setCategory(categoria);
        evento.setDateTime(fechaHora);
        evento.setLocation(ubicacion);
        evento.setPrice(precio);
        evento.setDescription(descripcion);
        evento.setImageUrl(imageUrl);
        return evento;
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
        AutoCompleteTextView inputUbicacion = formView.findViewById(R.id.input_ubicacion_evento);
        EditText inputPrecio = formView.findViewById(R.id.input_precio_evento);
        EditText inputDescripcion = formView.findViewById(R.id.input_descripcion_evento);
        EditText inputImagen = formView.findViewById(R.id.input_imagen_evento);

        LocalDate fechaInicial = LocalDate.now();
        LocalTime horaInicial = LocalTime.of(12, 0);

        if (esEdicion) {
            inputTitulo.setText(eventoExistente.getTitle());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // 1. Cargar Ubicaciones desde la BD
            db.collection("Locacions").get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> ubicaciones = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    // Asegurate de que el campo en Firebase se llame "nombre"
                    String nombre_ubi = doc.getString("Nom");
                    if (nombre_ubi != null) ubicaciones.add(nombre_ubi);
                }

                ArrayAdapter<String> adapterUbi = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, ubicaciones);
                inputUbicacion.setAdapter(adapterUbi); // _spinnerCiudad es el ID de tu XML
            });

            db.collection("Categorias").get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> categorias = new ArrayList<>();
                try {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre_categoria = doc.getString("nombre");
                        if (nombre_categoria != null) categorias.add(nombre_categoria);
                    }

                }catch (Exception e){
                    Log.e("CATEGORIAS CRASH", "Error al inicializar categorias", e);
                }

                ArrayAdapter<String> spiner_categorias = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, categorias);
                inputCategoria.setAdapter(spiner_categorias); // _spinnercat es el ID de tu XML
            });
            inputDescripcion.setText(eventoExistente.getDescription());
            inputPrecio.setText(String.valueOf(eventoExistente.getPrice()));
            inputImagen.setText(eventoExistente.getImageUrl());

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
                            inputImagen
                    );

                    if (eventoConstruido == null) {
                        return;
                    }

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
            EditText inputImagen
    ) {
        String titulo = inputTitulo.getText().toString().trim();
        String categoria = inputCategoria.getText().toString().trim();
        String fechaString = inputFecha.getText().toString().trim();
        String horaString = inputHora.getText().toString().trim();
        String ubicacion = inputUbicacion.getText().toString().trim();
        String precioString = inputPrecio.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();
        String imageUrl = inputImagen.getText().toString().trim();

        if (titulo.isEmpty()) {
            inputTitulo.setError("Titulo obligatorio");
            return null;
        }
        if (categoria.isEmpty()) {
            inputTitulo.setError("Categoria obligatorio");
            return null;
        }
        if (ubicacion.isEmpty()) {
            inputDescripcion.setError("Ubicacion obligatoria");
            return null;
        }
        if (descripcion.isEmpty()) {
            inputDescripcion.setError("Descripcion obligatoria");
            return null;
        }
        if (!imageUrl.isEmpty() && !URLUtil.isValidUrl(imageUrl)) {
            inputImagen.setError("URL de imagen invalida");
            return null;
        }

        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(fechaString, DATE_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            inputFecha.setError("Formato: yyyy-MM-dd");
            return null;
        }

        try {
            hora = LocalTime.parse(horaString, TIME_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            inputHora.setError("Formato: HH:mm");
            return null;
        }

        double precio = 0.0;
        if (!precioString.isEmpty()) {
            try {
                precio = Double.parseDouble(precioString);
            } catch (NumberFormatException e) {
                inputPrecio.setError("Numero invalido");
                return null;
            }
        }

        Event nuevoEvento = new Event();
        nuevoEvento.setTitle(titulo);
        nuevoEvento.setCategory(categoria);
        nuevoEvento.setDateTime(LocalDateTime.of(fecha, hora));
        nuevoEvento.setLocation(ubicacion);
        nuevoEvento.setPrice(precio);
        nuevoEvento.setDescription(descripcion);
        nuevoEvento.setImageUrl(imageUrl);

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