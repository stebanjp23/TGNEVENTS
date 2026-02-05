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
                                    ObtenerDatosUsuario(mAuth.getCurrentUser().getUid());

                                } else {
                                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

        private boolean validarCampos(String c, String p) {
            if (c.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }


    }




