package com.example.ttgneventos.util;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ttgneventos.model.AgregarEventos;
import com.example.ttgneventos.model.Configuracion;
import com.example.ttgneventos.R;
import com.example.ttgneventos.model.GestionCategorias;
import com.example.ttgneventos.model.GestionUsuarios;
import com.example.ttgneventos.model.Login;
import com.example.ttgneventos.model.MainMenu;
import com.google.android.material.navigation.NavigationView;

public final class IniciarMenu {
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
                        if (!(activity instanceof MainMenu)) {
                            Intent intent = new Intent(activity, MainMenu.class);
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
                        if (!(activity instanceof MainMenu)) {
                            Intent intent = new Intent(activity, MainMenu.class);
                            intent.putExtra("Es_admin", isAdmin);
                            intent.putExtra("Favoritos", true);
                            activity.startActivity(intent);
                        }
                        break;
                    case R.id.nav_gesUsuario:
                        if (!(activity instanceof GestionUsuarios)) {
                            Intent intent = new Intent(activity, GestionUsuarios.class);
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
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
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
    public static void actualizarEmailEnHeader(NavigationView navView) {
        if (navView == null) return;

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail() != null) {
            View headerView = navView.getHeaderView(0);
            TextView emailHeader = headerView.findViewById(R.id.textView_email);

            if (emailHeader != null) {
                emailHeader.setText(user.getEmail());
            }
        }
    }

}
