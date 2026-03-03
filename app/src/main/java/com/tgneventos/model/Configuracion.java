package com.tgneventos.model;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.tgneventos.R;
import com.tgneventos.util.IniciarMenu;
import com.tgneventos.util.NotificationPreferences;
import com.tgneventos.util.ThemePreferences;

public final class Configuracion extends AppCompatActivity {

    private final FirebaseAuth dbAuth = FirebaseAuth.getInstance();
    private LinearLayout confContrasena;
    private SwitchCompat confModoOscuro;
    private SwitchCompat confNotificaciones;
    private LinearLayout confCerrarSesion;
    private boolean updatingNotificationSwitch = false;
    private boolean permissionRequestedFromToggle = false;
    private boolean isUpdatingDarkModeSwitch = false;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!permissionRequestedFromToggle) {
                    return;
                }

                permissionRequestedFromToggle = false;

                if (isGranted) {
                    applyNotificationSetting(true, true);
                    return;
                }

                setNotificationSwitchChecked(false);
                applyNotificationSetting(false, false);
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
            });

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

        confContrasena = findViewById(R.id.config_contrasena);
        confModoOscuro = findViewById(R.id.config_modo_oscuro);
        confNotificaciones = findViewById(R.id.config_notificaciones);
        confCerrarSesion = findViewById(R.id.config_cerrar_sesion);

        setupPasswordReset();
        setupDarkModeToggle();
        setupNotificationToggle();
        confCerrarSesion.setOnClickListener
        (
            v ->
            {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        );
    }

    private void setupPasswordReset() {
        if (confContrasena == null) {
            return;
        }

        confContrasena.setOnClickListener(v -> {
            if (dbAuth.getCurrentUser() == null) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            String emailAddress = dbAuth.getCurrentUser().getEmail();
            if (emailAddress == null || emailAddress.isEmpty()) {
                Toast.makeText(this, "No se encontro un correo valido", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Restablecer contrasena")
                    .setMessage("Deseas recibir un correo en " + emailAddress + " para cambiar tu contrasena?")
                    .setPositiveButton("Enviar", (dialog, which) ->
                            dbAuth.sendPasswordResetEmail(emailAddress)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            mostrarMensajeExito(emailAddress);
                                        } else {
                                            Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                                        }
                                    }))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void setupDarkModeToggle() {
        boolean isCurrentlyDark = ThemePreferences.isDarkModeEnabled(this);

        isUpdatingDarkModeSwitch = true;
        confModoOscuro.setChecked(isCurrentlyDark);
        isUpdatingDarkModeSwitch = false;

        confModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingDarkModeSwitch) return;

            if (isChecked == ThemePreferences.isDarkModeEnabled(this)) return;

            ThemePreferences.setDarkModeEnabled(this, isChecked);

            buttonView.post(() -> {
                ThemePreferences.applySavedNightMode(Configuracion.this);
            });
        });
    }

    private void setupNotificationToggle() {
        boolean notificationsEnabled = NotificationPreferences.isNotificationsEnabled(this);
        if (notificationsEnabled && !NotificationPreferences.hasRuntimePermission(this)) {
            notificationsEnabled = false;
            NotificationPreferences.setNotificationsEnabled(this, false);
        }

        setNotificationSwitchChecked(notificationsEnabled);
        NotificationPreferences.syncTopicWithStoredSetting(this);
        NotificationPreferences.refreshAndStoreCurrentToken();

        confNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updatingNotificationSwitch) {
                return;
            }

            if (isChecked && !NotificationPreferences.hasRuntimePermission(this)) {
                permissionRequestedFromToggle = true;
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }

            applyNotificationSetting(isChecked, true);
        });
    }

    private void setNotificationSwitchChecked(boolean checked) {
        updatingNotificationSwitch = true;
        confNotificaciones.setChecked(checked);
        updatingNotificationSwitch = false;
    }

    private void applyNotificationSetting(boolean enabled, boolean showToast) {
        NotificationPreferences.setNotificationsEnabled(this, enabled);
        NotificationPreferences.updateCurrentUserNotificationPreference(this);
        NotificationPreferences.updateTopicSubscription(enabled)
                .addOnSuccessListener(unused -> {
                    if (!showToast) {
                        return;
                    }
                    String message = enabled ? "Notificaciones activadas" : "Notificaciones desactivadas";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!showToast) {
                        return;
                    }
                    String message = enabled
                            ? "No se pudo activar la suscripcion de notificaciones"
                            : "No se pudo desactivar la suscripcion de notificaciones";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarMensajeExito(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Correo enviado")
                .setMessage("Hemos enviado un correo para restablecer tu contrasena a:\n\n" + email
                        + "\n\nRevisa tu bandeja de entrada y la carpeta de spam.")
                .setPositiveButton("Entendido", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}