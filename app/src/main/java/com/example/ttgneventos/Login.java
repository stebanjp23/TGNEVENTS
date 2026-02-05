package com.example.ttgneventos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private Button Registrar;
    private Button Iniciar;
    private EditText Correo;
    private EditText Password;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Registrar = findViewById(R.id.Registrar);
        Registrar.setOnClickListener(this);
        Iniciar = findViewById(R.id.Iniciar);
        Iniciar.setOnClickListener(this);
        Correo = findViewById(R.id.Correo);
        Password = findViewById(R.id.Password);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


    }

    @Override
    public void onClick(View v) {
        String c = Correo.getText().toString();
        String p = Password.getText().toString();

        switch (v.getId()) {
            case R.id.Registrar:
                Intent registrar = new Intent(this, Registro.class);
                startActivity(registrar);
                break;

            case R.id.Iniciar:
                // 1. Verificamos el formato antes de entrar al bucle
                if (!validarCampos(c, p)) {
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(c).matches()) {
                    Correo.setError("Formato de correo no válido");
                    return;
                }

                mAuth.signInWithEmailAndPassword(c, p)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();

                                // 2. FILTRO G FOR LIVE: ¿Ha verificado el correo?
                                if (user != null && user.isEmailVerified()) {
                                    // Si está verificado, buscamos sus datos en Firestore
                                    ObtenerDatosUsuario(user.getUid());
                                } else {
                                    // Si NO está verificado, le avisamos y ofrecemos reenviar
                                    mostrarPopupVerificacion(user);
                                    mAuth.signOut(); // Cerramos sesión para que no se quede logueado a medias
                                }
                            } else {
                                mostrarPopupRegistro(c);
                            }
                        });
                break;
        }


    }

        private void ObtenerDatosUsuario(String uid){
            db.collection("Usuarios").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Usuario usuario = documentSnapshot.toObject(Usuario.class);

                            if (usuario != null) {
                                Intent intent = new Intent(Login.this, MainMenu.class);
                                intent.putExtra("Es_admin", usuario.isAdmin());
                                startActivity(intent);
                                finish();
                            }

                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(Login.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                    });
        }

        private void mostrarPopupRegistro(String emailOlvidado) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);

            builder.setTitle("Usuario no encontrado");
            builder.setMessage("El correo " + emailOlvidado + " no está registrado. ¿Quieres crear una cuenta ahora?");

            // Botón para ir al Registro
            builder.setPositiveButton("Registrarme", (dialog, which) -> {
                Intent intent = new Intent(Login.this, Registro.class);
                startActivity(intent);
            });

            // Botón para cancelar
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

            builder.create().show();
        }

        private boolean validarCampos(String c, String p) {
            if (c.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

    private void mostrarPopupVerificacion(com.google.firebase.auth.FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);

        builder.setTitle("Cuenta no verificada");
        builder.setMessage("Tu correo no ha sido verificado. Revisa tu bandeja de entrada o solicita un nuevo enlace.");

        builder.setPositiveButton("Reenviar Correo", (dialog, which) -> {
            if (user != null) {
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "Nuevo enlace enviado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "Error al enviar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Entendido", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    }




