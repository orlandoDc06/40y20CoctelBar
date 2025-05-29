package com.example.a40y20coctelbar.OpcionesMesero;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.adaptersMesero.CarritoComandaAdapter;
import com.example.a40y20coctelbar.models.CarritoComandaTemporal;
import com.example.a40y20coctelbar.models.Comanda;
import com.example.a40y20coctelbar.models.ProductosComanda;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrearComanda extends AppCompatActivity {

    private ArrayList<ProductosComanda> carrito;
    private EditText etNombreCliente;
    private Button btnCrearComanda;
    private TextView tvTotal;
    private CarritoComandaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mesero_crear_comanda);

        etNombreCliente = findViewById(R.id.etNombreCliente);
        btnCrearComanda = findViewById(R.id.btnFinalizarComanda);
        tvTotal = findViewById(R.id.tvTotalPagar);

        carrito = CarritoComandaTemporal.getListaProductos();


        // RecyclerView
        RecyclerView rvCarrito = findViewById(R.id.rv_carritoCompras);
        adapter = new CarritoComandaAdapter(this, carrito, new CarritoComandaAdapter.OnCarritoActionListener() {
            @Override
            public void onEditar(int position) {
                if (position >= 0 && position < carrito.size()) {
                    ProductosComanda producto = carrito.get(position);
                    mostrarDialogoEditarProducto(producto, position);
                }
            }

            @Override
            public void onEliminar(int position) {
                new AlertDialog.Builder(CrearComanda.this)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que deseas eliminar este producto de la comanda?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            if (position >= 0 && position < carrito.size()) {
                                carrito.remove(position);
                                adapter.notifyItemRemoved(position);
                                recalcularTotal();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        rvCarrito.setLayoutManager(new LinearLayoutManager(this));;
        rvCarrito.setAdapter(adapter);


        // Verificar si está vacío
        if (carrito == null || carrito.isEmpty()) {
            Toast.makeText(this, "Por ahora no se han agregado productos a la comanda", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Calcular total
        double total = 0.0;
        for (ProductosComanda p : carrito) {
            try {
                double precio = Double.parseDouble(p.getMenu().getPrecio().replace("$", "").replace(",", "."));
                total += precio * p.getCantidad();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Error en el formato del precio: " + p.getMenu().getPrecio(), Toast.LENGTH_SHORT).show();
            }
        }

        tvTotal.setText("Total: $" + String.format("%.2f", total));
        double totalFinal = total;

        btnCrearComanda.setOnClickListener(v -> {
            String nombreCliente = etNombreCliente.getText().toString().trim();

            if (nombreCliente.isEmpty()) {
                Toast.makeText(this, "Debe ingresar un nombre de cliente", Toast.LENGTH_SHORT).show();
                return;
            }

            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            String key = "comanda_" + System.currentTimeMillis();
            String estadoComanda = "Pendiente";
            String mesero = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String totalPagar = String.valueOf(totalFinal);

            Comanda comanda = new Comanda(fecha, nombreCliente, key, carrito, estadoComanda, mesero, totalPagar);

            FirebaseDatabase.getInstance().getReference().child("comandas")
                    .child(key)
                    .setValue(comanda)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Comanda creada", Toast.LENGTH_SHORT).show();
                        System.out.println(comanda.getFecha() + comanda.getNombreCliente() + comanda.getProductosComanda());
                        CarritoComandaTemporal.limpiarCarrito();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void recalcularTotal() {
        double total = 0.0;
        for (ProductosComanda p : carrito) {
            try {
                total += Double.parseDouble(p.getMenu().getPrecio()) * p.getCantidad();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Error en formato del precio", Toast.LENGTH_SHORT).show();
            }
        }
        tvTotal.setText("Total: $" + String.format("%.2f", total));
    }

    private void mostrarDialogoEditarProducto(ProductosComanda producto, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar producto");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.mesero_dialog_agregar_producto, null);
        builder.setView(dialogView);

        EditText etCantidad = dialogView.findViewById(R.id.editCantidad);
        EditText etNota = dialogView.findViewById(R.id.editNota);

        etCantidad.setText(String.valueOf(producto.getCantidad()));
        etNota.setText(producto.getNota());

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String cantidadStr = etCantidad.getText().toString().trim();
            String notaStr = etNota.getText().toString().trim();

            if (cantidadStr.isEmpty()) {
                Toast.makeText(this, "Debe ingresar una cantidad", Toast.LENGTH_SHORT).show();
                return;
            }

            int nuevaCantidad = Integer.parseInt(cantidadStr);

            // Actualizar
            producto.setCantidad(nuevaCantidad);
            producto.setNota(notaStr);

            // Actualizar Adapter
            adapter.notifyItemChanged(position);

            recalcularTotal();
        });

        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

