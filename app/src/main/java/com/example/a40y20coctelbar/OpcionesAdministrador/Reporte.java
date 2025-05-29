package com.example.a40y20coctelbar.OpcionesAdministrador;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.models.Comanda;
import com.example.a40y20coctelbar.models.Menu;
import com.example.a40y20coctelbar.models.ProductosComanda;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Reporte extends AppCompatActivity {

    private TextView txtVentasDia, txtTotalComandas, txtMeseroTop, txtCategoriaPopular, txtMayorVenta, txtMayorVentaValor, txtHoraPico;

    public interface MeseroCallback {
        void onResult(String meseroTop);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_reporte);

        txtVentasDia = findViewById(R.id.txtVentasDia);
        txtTotalComandas = findViewById(R.id.txtComandasDia);
        txtMeseroTop = findViewById(R.id.txtMeseroVentas);
        txtCategoriaPopular = findViewById(R.id.txtCategoriaPopular);
        txtMayorVenta = findViewById(R.id.txtMayorVenta);
        txtMayorVentaValor = findViewById(R.id.txtMayorVentaValor);
        txtHoraPico = findViewById(R.id.txtHoraPico);

        obtenerVentasDelDia();
    }

    private void obtenerVentasDelDia() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comandas");

        String fechaHoy = obtenerFechaSinHora();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalVentas = 0;
                List<Comanda> comandasDelDia = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comanda comanda = ds.getValue(Comanda.class);
                    if (comanda != null && comanda.getFecha() != null) {
                        String fechaComanda = comanda.getFecha().split(" ")[0];

                        if (fechaHoy.equals(fechaComanda)) {
                            // Agregar a la lista de comandas del día
                            comandasDelDia.add(comanda);

                            // Sumar al total de ventas
                            try {
                                totalVentas += Double.parseDouble(comanda.getTotalPagar());
                            } catch (NumberFormatException e) {
                                Log.w("Reporte", "Error al convertir monto: " + e.getMessage());
                            }
                        }
                    }
                }

                // Mostrar el total de ventas
                txtVentasDia.setText("$" + String.format("%.2f", totalVentas));

                // Mostrar el total de comandas
                int totalComandas = obtenerTotalComandasDelDia(comandasDelDia, fechaHoy);
                txtTotalComandas.setText(String.valueOf(totalComandas));

                // Mostrar el mesero con más ventas
                obtenerMeseroConMasVentas(comandasDelDia, fechaHoy, new MeseroCallback() {
                    @Override
                    public void onResult(String meseroTop) {
                        txtMeseroTop.setText(meseroTop);
                    }
                });

                // Mostrar el categoria más popular
                String categoriaPopular = obtenerCategoriaMasPopular(comandasDelDia, fechaHoy);
                txtCategoriaPopular.setText(categoriaPopular);

                // Mostrar comanda mas alta
                String ventaAlta = obtenerVentaMasAltaDelDia(comandasDelDia, fechaHoy);
                txtMayorVenta.setText(ventaAlta.substring(0, 20));
                txtMayorVentaValor.setText("$ " + ventaAlta.substring(20));

                // Hora pico
                String horaPico = obtenerHoraPicoDelDia(comandasDelDia, fechaHoy);
                txtHoraPico.setText(horaPico);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Reporte.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String obtenerFechaSinHora() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private int obtenerTotalComandasDelDia(List<Comanda> comandas, String fechaActual) {
        int total = 0;
        for (Comanda c : comandas) {
            if (c.getFecha().startsWith(fechaActual)) {
                total++;
            }
        }
        return total;
    }

    private void obtenerMeseroConMasVentas(List<Comanda> comandas, String fechaActual, MeseroCallback callback) {
        Map<String, Double> ventasPorMesero = new HashMap<>();

        for (Comanda c : comandas) {
            if (c.getFecha().startsWith(fechaActual)) {
                String meseroId = c.getMesero();
                double total = 0.0;
                try {
                    total = Double.parseDouble(c.getTotalPagar());
                } catch (NumberFormatException e) {
                    continue;
                }
                ventasPorMesero.put(meseroId, ventasPorMesero.getOrDefault(meseroId, 0.0) + total);
            }
        }

        String idTop = "N/A";
        double maxVentas = 0.0;

        for (Map.Entry<String, Double> entry : ventasPorMesero.entrySet()) {
            if (entry.getValue() > maxVentas) {
                maxVentas = entry.getValue();
                idTop = entry.getKey();
            }
        }

        if (idTop.equals("N/A")) {
            callback.onResult("Sin datos");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(idTop);
        double finalMaxVentas = maxVentas;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nombre = "Desconocido";
                if (snapshot.exists() && snapshot.child("nombre").getValue() != null) {
                    nombre = snapshot.child("nombre").getValue(String.class);
                }
                callback.onResult(nombre + " ($" + String.format("%.2f", finalMaxVentas) + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult("Error al cargar mesero");
            }
        });
    }

    private String obtenerCategoriaMasPopular(List<Comanda> comandas, String fechaActual) {
        Map<String, Integer> cantidadPorCategoria = new HashMap<>();

        for (Comanda comanda : comandas) {
            if (comanda.getFecha().startsWith(fechaActual)) {
                List<ProductosComanda> productos = comanda.getProductosComanda(); // O el nombre de tu getter real

                if (productos != null) {
                    for (ProductosComanda p : productos) {
                        String categoria = p.getMenu().getCategoria();
                        int cantidad = p.getCantidad();

                        cantidadPorCategoria.put(categoria,
                                cantidadPorCategoria.getOrDefault(categoria, 0) + cantidad);
                    }
                }
            }
        }

        String categoriaTop = "N/A";
        int maxCantidad = 0;

        for (Map.Entry<String, Integer> entry : cantidadPorCategoria.entrySet()) {
            if (entry.getValue() > maxCantidad) {
                maxCantidad = entry.getValue();
                categoriaTop = entry.getKey();
            }
        }

        return categoriaTop + " (" + maxCantidad + " unidades)";
    }

    private String obtenerVentaMasAltaDelDia(List<Comanda> comandas, String fechaActual) {
        double ventaMaxima = 0.0;
        String detalleVenta = "N/A";
        String valorVenta = "N/A";

        for (Comanda comanda : comandas) {
            if (comanda.getFecha().startsWith(fechaActual)) {
                try {
                    double total = Double.parseDouble(comanda.getTotalPagar());
                    if (total > ventaMaxima) {
                        ventaMaxima = total;

                        String keySinPrefijo = comanda.getKey().substring(8);
                        // Puedes incluir más detalles si deseas
                        detalleVenta = "Orden #" + keySinPrefijo;
                        valorVenta = String.format("%.2f", total);
                    }
                } catch (NumberFormatException e) {
                    Log.w("Reporte", "Error al convertir totalPagar: " + e.getMessage());
                }
            }
        }

        return detalleVenta + valorVenta;
    }

    private String obtenerHoraPicoDelDia(List<Comanda> comandas, String fechaActual) {
        Map<String, Integer> ventasPorHora = new HashMap<>();

        for (Comanda comanda : comandas) {
            if (comanda.getFecha().startsWith(fechaActual)) {
                try {
                    String[] partes = comanda.getFecha().split(" ");
                    if (partes.length == 2) {
                        String horaCompleta = partes[1]; // Ej. "14:35"
                        String soloHora = horaCompleta.split(":")[0]; // Ej. "14"
                        ventasPorHora.put(soloHora, ventasPorHora.getOrDefault(soloHora, 0) + 1);
                    }
                } catch (Exception e) {
                    Log.w("Reporte", "Error al procesar hora: " + e.getMessage());
                }
            }
        }

        String horaPico = "N/A";
        int maxVentas = 0;

        for (Map.Entry<String, Integer> entry : ventasPorHora.entrySet()) {
            if (entry.getValue() > maxVentas) {
                maxVentas = entry.getValue();
                horaPico = entry.getKey();
            }
        }

        if (!horaPico.equals("N/A")) {
            horaPico += ":00 (" + maxVentas + " ventas)";
        }

        return horaPico;
    }



}