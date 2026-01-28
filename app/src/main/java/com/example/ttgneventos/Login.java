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

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private Button Registrar;
    private Button Iniciar;
    private EditText Correo;
    private EditText Password;

    public static List<Usuario> DBusers = new ArrayList<>();


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

        if (DBusers.isEmpty()) {
            DBusers.add(new Usuario("admin", "admin@gmail.com", "admin"));
            DBusers.add(new Usuario("user", "steban@gmail.com", "Jh1191jh"));
            DBusers.add(new Usuario("pepe", "pepe@gmail.com", "1234"));
            DBusers.get(0).setAdmin(true);
        }


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
                if (c.isEmpty() && p.isEmpty()) {
                    Correo.setError("El correo no puede estar vacío");
                    Password.setError("La contraseña no puede estar vacía");
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(c).matches()) {
                    Correo.setError("Formato de correo no válido");
                    return;
                }

                // 2. Buscamos en la "base de datos"
                boolean encontrado = false;
                for (int i = 0; i < DBusers.size(); i++) {
                    if (DBusers.get(i).getCorreo().equals(c) && DBusers.get(i).getContraseña().equals(p)) {
                        encontrado = true;


                        Intent intent = new Intent(this, Inicio.class);
                        intent.putExtra("admin", DBusers.get(i).isAdmin());

                        startActivity(intent);
                        finish(); // Opcional: cierra el Login para que no puedan volver atrás
                        break;
                    }
                }

                if (!encontrado) {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
                break;

        }


    }


}