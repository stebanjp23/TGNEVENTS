package com.example.ttgneventos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Registro extends AppCompatActivity implements View.OnClickListener {
    private EditText Nombre_usuario;
    private EditText Correo_nuevo;
    private EditText Password_nueva;
    private Button Registrarse;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Nombre_usuario = findViewById(R.id.Nombre);
        Correo_nuevo = findViewById(R.id.Correo_nuevo);
        Password_nueva = findViewById(R.id.Password_nueva);
        Registrarse = findViewById(R.id.Registrarse);
        Registrarse.setOnClickListener(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public void onClick(View v) {
        String nombre = Nombre_usuario.getText().toString().trim();
        String correo = Correo_nuevo.getText().toString().trim();
        String password = Password_nueva.getText().toString().trim();

        if (v.getId() == R.id.Registrarse) {

            // 1️⃣ Validar campos vacíos
            if(!validarCampos(nombre, correo, password)) return;

            // 2️⃣ Deshabilitar botón para evitar doble click
            Registrarse.setEnabled(false);

            // 3️⃣ Crear usuario en Firebase
            mAuth.createUserWithEmailAndPassword(correo, password)
                    .addOnCompleteListener(this, task -> {

                        // Volvemos a habilitar el botón
                        Registrarse.setEnabled(true);

                        if(task.isSuccessful()) {
                            com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null) {
                                // Enviar correo de verificación
                                user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    if(verifyTask.isSuccessful()) {
                                        // Mostrar AlertDialog en lugar de pasar mensaje por intent
                                        new AlertDialog.Builder(Registro.this)
                                                .setTitle("Registro exitoso")
                                                .setMessage("Se ha enviado un correo de verificación a " + correo + ". Revisa tu bandeja de entrada.")
                                                .setPositiveButton("Aceptar", (dialog, which) -> {
                                                    // Guardar datos en Firestore
                                                    guardarUsuarioFirestore(user.getUid(), nombre, correo);
                                                })
                                                .setCancelable(false)
                                                .show();
                                    } else {
                                        Toast.makeText(this, "Error al enviar correo de verificación: " +
                                                verifyTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } else {
                            // 4️⃣ Manejo completo de errores de Firebase
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
                            } else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(this, "Este correo ya está registrado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
        }
    }

    private void guardarUsuarioFirestore(String uid, String nombre, String correo){
        Usuario nuevoUsuario = new Usuario(nombre, correo);
        nuevoUsuario.setAdmin(false);

        db.collection("Usuarios").document(uid)
                .set(nuevoUsuario)
                .addOnSuccessListener(aVoid -> {
                    mAuth.signOut(); // No dejamos que el usuario entre sin verificar
                    Intent intent = new Intent(Registro.this, Login.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validarCampos(String nombre, String correo, String password){
        if(nombre.isEmpty()) {
            Nombre_usuario.setError("El nombre es obligatorio");
            return false;
        }
        if(correo.isEmpty()) {
            Correo_nuevo.setError("El correo es obligatorio");
            return false;
        }
        if(password.isEmpty()) {
            Password_nueva.setError("La contraseña es obligatoria");
            return false;
        }

        // Validar formato de correo
        if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Correo_nuevo.setError("Formato de correo no válido");
            return false;
        }

        return true;
    }

}