package com.example.ttgneventos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class Inicio extends AppCompatActivity{
    private TextView estado_admin;
    public boolean admin;
    public Button gestion_users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        IniciarMenu.setupDrawer(this, drawer, navView, toolbar, getIntent().getBooleanExtra("Es_admin", false));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        estado_admin = findViewById(R.id.es_admin);
        // Cambio sugerido
        admin = getIntent().getBooleanExtra("Es_admin", false);

        if (admin) {
            estado_admin.setText("SI ADMIN");
        } else {
            estado_admin.setText("NO ADMIN");
        }


    }
}