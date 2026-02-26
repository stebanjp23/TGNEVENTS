package com.example.ttgneventos.pojo;

import com.google.firebase.firestore.Exclude;

public class Categoria {
    private String uid;
    private String nombre;

    public Categoria(String uid, String nombre) {
        this.uid = uid;
        this.nombre = nombre;
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public Categoria() {
    }
    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
