package com.example.ttgneventos.model;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ttgneventos.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Filters extends AppCompatActivity {

    // Referencias de UI
    private ImageButton _btnBack; //bt para volver
    private LinearLayout _startDateField, _endDateField; //Fechas
    private EditText keysearch; //Campo de palabras clave
    private Button _btnAplicarFiltros; //bt para filtrar
    private AutoCompleteTextView _spinnerCiudad, _spinnerCategoria; //desplegables de ciudad y categoría



    // Lista de palabras clave
    private List<String> _keywords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filters);


        // Configuración de Insets (márgenes de sistema)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        //Btn de Retroceso al menú principal
        _btnBack = findViewById(R.id.btnBack);
        _btnBack.setOnClickListener(v -> finish());


        // 1. Inicializar selectores de Fecha
        _startDateField = findViewById(R.id.startDateField);
        _endDateField = findViewById(R.id.endDateField);
        _startDateField.setOnClickListener(v -> datePickDialog(_startDateField));
        _endDateField.setOnClickListener(v -> datePickDialog(_endDateField));
        keysearch = findViewById(R.id.keysearch);


        // 2. Inicializar Selectores de Ciudad y Categoría (Exposed Dropdown)ç
        try {
            setupSpinners();
        } catch (Exception e) {
            Log.e("SPINERS CRASH", "Error al inicializar spinners", e);

        }


        // 4. Botón de Aplicar Filtros
        _btnAplicarFiltros = findViewById(R.id.btnAplicarFiltros);
        _btnAplicarFiltros.setOnClickListener(v -> aplicarYEnviarFiltros());
    }


    private void setupSpinners() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Configurar Ciudades
        _spinnerCiudad = findViewById(R.id.spinnerCiudad);

        // 1. Cargar Ubicaciones desde la BD
        db.collection("Locacions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> ubicaciones = new ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                // Asegúrate de que el campo en Firebase se llame "nombre"
                String nombre_ubi = doc.getString("Nom");
                if (nombre_ubi != null) ubicaciones.add(nombre_ubi);
            }

            ArrayAdapter<String> adapterUbi = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, ubicaciones);
            _spinnerCiudad.setAdapter(adapterUbi); // _spinnerCiudad es el ID de tu XML
        });

        // Configurar Categorías
        _spinnerCategoria = findViewById(R.id.spinnerCategoria);

        // 1. Cargar Categorias desde la BD

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
            _spinnerCategoria.setAdapter(spiner_categorias); // _spinnercat es el ID de tu XML
        });
    }


    private void aplicarYEnviarFiltros() {
        Intent mainMenu = new Intent(this, MainMenu.class);

        // Obtener Fechas de los TextViews internos
        TextView startDateTextView = (TextView) _startDateField.getChildAt(0);
        TextView endDateTextView = (TextView) _endDateField.getChildAt(0);
        LocalDate startDate = parseLocalDate(startDateTextView.getText().toString());
        LocalDate endDate = parseLocalDate(endDateTextView.getText().toString());

        // Obtener Ciudad y Categoría
        String ciudad = _spinnerCiudad.getText().toString();
        String categoria = _spinnerCategoria.getText().toString();

        // Limpiar valores si el usuario no seleccionó nada
        if (ciudad.isEmpty()) ciudad = null;
        if (categoria.isEmpty()) categoria = null;

        // CAPTURAR LA BÚSQUEDA POR TEXTO LIBRE
        String textoBusqueda = keysearch.getText().toString().trim();
        if (!textoBusqueda.isEmpty()) {
            // Añadimos el texto a la lista de palabras clave
            _keywords.clear(); // Limpiamos para que solo busque lo que hay escrito ahora
            _keywords.add(textoBusqueda);
        }

        List<String> keywords = _keywords.isEmpty() ? null : new ArrayList<>(_keywords);

        // Crear objeto de transferencia
        FilterObject filterData = new FilterObject(startDate, endDate, keywords, ciudad, categoria);

        mainMenu.putExtra("Filters", filterData);
        mainMenu.putExtra("Es_admin", getIntent().getBooleanExtra("Es_admin", false));

        // Usar FLAG_ACTIVITY_CLEAR_TOP para volver a la instancia existente del menú si es necesario
        mainMenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainMenu);
        finish();
    }

    private LocalDate parseLocalDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null; // Si es el texto por defecto (DD/MM/YYYY), devuelve null
        }
    }

    private void datePickDialog(LinearLayout dateField) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            TextView dateText = (TextView) dateField.getChildAt(0);
            String formattedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateText.setText(formattedDate);
        });
        datePickerDialog.show();
    }

    // --- CLASE DE DATOS (POJO) ---
    public static class FilterObject implements Serializable {
        private final LocalDate _startDate;
        private final LocalDate _endDate;
        private final List<String> _keywords;
        private final String _ciudad;
        private final String _categoria;

        public FilterObject(LocalDate startDate, LocalDate endDate, List<String> keywords, String ciudad, String categoria) {
            _startDate = startDate;
            _endDate = endDate;
            _keywords = keywords;
            _ciudad = ciudad;
            _categoria = categoria;
        }

        public LocalDate getStartDate() { return _startDate; }
        public LocalDate getEndDate() { return _endDate; }
        public List<String> getKeywords() { return _keywords; }
        public String getCiudad() { return _ciudad; }
        public String getCategoria() { return _categoria; }
    }
}