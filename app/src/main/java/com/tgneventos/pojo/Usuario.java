package com.tgneventos.pojo;

import java.util.List;

public final class Usuario {
    private String uid;
    private String nombre;
    private String correo;
    private String contrasena;
    private boolean admin;
    private List<String> _favourites;


    public Usuario(String nombre, String correo, String contrasena) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.admin = false;

    }

    public Usuario(String uid, String nombre, String correo, String contrasena, boolean admin) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.admin = admin;
    }

    public Usuario(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    public Usuario() {
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public List<String> getFavourites() {
        return _favourites;
    }
}
