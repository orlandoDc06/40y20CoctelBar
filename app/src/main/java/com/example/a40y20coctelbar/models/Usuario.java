package com.example.a40y20coctelbar.models;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String correo;
    private String nombre;
    private String pp;
    private String rol;
    private String key;
    public Usuario() {

    }

    public Usuario(String correo, String nombre, String pp, String rol, String key) {
        this.correo = correo;
        this.nombre = nombre;
        this.pp = pp;
        this.rol = rol;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPp() {
        return pp;
    }

    public void setPp(String pp) {
        this.pp = pp;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
