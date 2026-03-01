package com.example.ttgneventos.model;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ttgneventos.R;
import com.example.ttgneventos.util.IniciarMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public final class Configuracion extends AppCompatActivity {

    private FirebaseAuth db_auth = FirebaseAuth.getInstance();
    private LinearLayout _conf_contraseña;
    private SwitchCompat _conf_modo_oscuro;
    private SwitchCompat _conf_notificaciones;
    private SharedPreferences preferencias;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion);

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

        _conf_contraseña = findViewById(R.id.config_contraseña);
        _conf_modo_oscuro = findViewById(R.id.config_modo_oscuro);
        _conf_notificaciones = findViewById(R.id.config_notificaciones);
        preferencias = getSharedPreferences("preferencias_usuario", MODE_PRIVATE);


        _conf_contraseña.setOnClickListener(v -> {
            String emailAddress = db_auth.getCurrentUser().getEmail();

            if (emailAddress != null) {
                // Mostramos un diálogo de confirmación antes de enviar
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Restablecer Contraseña")
                        .setMessage("¿Deseas recibir un correo electrónico en " + emailAddress + " para cambiar tu contraseña?")
                        .setPositiveButton("Enviar", (dialog, which) -> {

                            // Si el usuario acepta, enviamos el correo
                            db_auth.sendPasswordResetEmail(emailAddress)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Ventana de éxito
                                            mostrarMensajeExito(emailAddress);
                                        } else {
                                            Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
        _conf_modo_oscuro.setChecked(preferencias.getBoolean("modo_oscuro", false));
        _conf_notificaciones.setChecked(preferencias.getBoolean("notificaciones", true));

        _conf_modo_oscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            preferencias.edit().putBoolean("modo_oscuro", isChecked).apply();
        });

        _conf_notificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencias.edit().putBoolean("notificaciones", isChecked).apply();
            String msg = isChecked ? "Notificaciones activadas" : "Notificaciones silenciadas";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

    }

    private void mostrarMensajeExito(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¡Correo Enviado!")
                .setMessage("Hemos enviado un correo para restablecer su contraseña a:\n\n" + email + "\n\nPor favor, revisa tu bandeja de entrada (y la carpeta de spam).")
                .setPositiveButton("Entendido", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }


}