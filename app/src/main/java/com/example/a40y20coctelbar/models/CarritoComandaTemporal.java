package com.example.a40y20coctelbar.models;

import java.util.ArrayList;

public class CarritoComandaTemporal {

    private static ArrayList<ProductosComanda> listaProductos = new ArrayList<>();

    public static ArrayList<ProductosComanda> getListaProductos() {
        return listaProductos;
    }

    public static void agregarProducto(ProductosComanda producto) {
        listaProductos.add(producto);
    }

    public static void limpiarCarrito() {
        listaProductos.clear();
    }

    public static boolean estaVacio() {
        return listaProductos.isEmpty();
        }


}
