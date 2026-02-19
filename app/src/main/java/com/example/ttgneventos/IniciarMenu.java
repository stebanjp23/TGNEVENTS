package com.example.ttgneventos;


import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class IniciarMenu {
        @SuppressLint("NonConstantResourceId")
        public static void setupDrawer (AppCompatActivity activity, DrawerLayout
        drawer, NavigationView navigationView, Toolbar toolbar, boolean isAdmin){
            activity.setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                switch (id){
                    case R.id.nav_inicio:
                        if (!(activity instanceof Inicio)) {
                            Intent intent = new Intent(activity, Inicio.class);
                            intent.putExtra("Es_admin", isAdmin);
                            activity.startActivity(intent);
                        }
                        break;
                    case R.id.nav_config:
                        if (!(activity instanceof Configuracion)) {
                            Intent intent = new Intent(activity, Configuracion.class);
                            intent.putExtra("Es_admin", isAdmin);
                            activity.startActivity(intent);
                        }
                        break;
                    case R.id.nav_favorito:
                        if (!(activity instanceof Inicio)) {
                            Intent intent = new Intent(activity, Inicio.class);
                            intent.putExtra("Es_admin", isAdmin);
                            activity.startActivity(intent);
                        }
                        break;
                    case R.id.nav_gesUsuario:
                        if (!(activity instanceof Lista_usuarios)) {
                            Intent intent = new Intent(activity, Lista_usuarios.class);
                            intent.putExtra("Es_admin", isAdmin);
                            activity.startActivity(intent);
                        }
                        break;
                    case R.id.nav_gesCategoria:
                        if (!(activity instanceof GestionCategorias)) {
                            Intent intent = new Intent(activity, GestionCategorias.class);
                            intent.putExtra("Es_admin", isAdmin);
                            activity.startActivity(intent);
                        }
                        break;
                    case R.id.nav_agregarEventos:
                        if (!(activity instanceof AgregarEventos)) {
                            Intent intent = new Intent(activity, AgregarEventos.class);
                            intent.putExtra("Es_admin", isAdmin);
                            activity.startActivity(intent);
                        }
                        break;
                    case R.id.nav_logout:
                        Intent intent = new Intent(activity, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                        break;
                }

                drawer.closeDrawers();
                return true;
            });
        }

}
