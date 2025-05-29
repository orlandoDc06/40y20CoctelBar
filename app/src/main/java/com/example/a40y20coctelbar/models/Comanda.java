package com.example.a40y20coctelbar.models;

import java.io.Serializable;
import java.util.List;

public class Comanda implements Serializable {
    private String fecha;
    private String nombreCliente;
    private String key;
    private List<ProductosComanda> productosComanda;
    private String estadoComanda;
    private String mesero;
    private String totalPagar;
    private String foto;

    public Comanda() {
    }

    public Comanda(String fecha, String nombreCliente, String key, List<ProductosComanda> productosComanda, String estadoComanda, String mesero, String totalPagar) {
        this.fecha = fecha;
        this.nombreCliente = nombreCliente;
        this.key = key;
        this.productosComanda = productosComanda;
        this.estadoComanda = estadoComanda;
        this.mesero = mesero;
        this.totalPagar = totalPagar;
    }


    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<ProductosComanda> getProductosComanda() {
        return productosComanda;
    }

    public void setProductosComanda(List<ProductosComanda> productosComanda) {
        this.productosComanda = productosComanda;
    }

    public String getEstadoComanda() {
        return estadoComanda;
    }

    public void setEstadoComanda(String estadoComanda) {
        this.estadoComanda = estadoComanda;
    }

    public String getMesero() {
        return mesero;
    }

    public void setMesero(String mesero) {
        this.mesero = mesero;
    }

    public String getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(String totalPagar) {
        this.totalPagar = totalPagar;
    }


}
