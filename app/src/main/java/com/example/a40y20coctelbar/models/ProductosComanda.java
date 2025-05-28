package com.example.a40y20coctelbar.models;

import java.io.Serializable;

public class ProductosComanda implements Serializable {

    private Menu menu;
    private int cantidad;
    private String nota;

    public ProductosComanda() {
    }

    public ProductosComanda(Menu menu, int cantidad, String nota) {
        this.menu = menu;
        this.cantidad = cantidad;
        this.nota = nota;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
