package com.example.a40y20coctelbar.adaptersMesero;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.models.Comanda;
import com.example.a40y20coctelbar.models.ProductosComanda;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HistorialComandaAdapter extends RecyclerView.Adapter<HistorialComandaAdapter.ViewHolder> {

    private List<Comanda> listaComandas;
    private Context context;

    public HistorialComandaAdapter(Context context, List<Comanda> listaComandas) {
        this.context = context;
        this.listaComandas = listaComandas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comanda comanda = listaComandas.get(position);
        String keySinPrefijo = comanda.getKey().substring(8);
        holder.lblKey.setText("Orden: #" + keySinPrefijo);
        holder.lblFecha.setText("Fecha: " + comanda.getFecha());
        holder.lblNombreCliente.setText("Cliente: " + comanda.getNombreCliente());
        holder.lblEstado.setText("Estado: " + comanda.getEstadoComanda());
        holder.lblSubtotal.setText("Total: $" + comanda.getTotalPagar());

        holder.btnEditar.setOnClickListener(v -> {
            mostrarDialogoEditar(comanda, holder.getAdapterPosition());
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if ("Pendiente".equalsIgnoreCase(comanda.getEstadoComanda())) {
                new AlertDialog.Builder(context)
                        .setTitle("Eliminar Comanda")
                        .setMessage("¿Estás seguro de que deseas eliminar esta comanda?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comandas").child(comanda.getKey());
                            ref.removeValue()
                                    .addOnSuccessListener(unused -> {
                                        // Eliminar de la lista local
                                        listaComandas.remove(holder.getAdapterPosition());
                                        notifyItemRemoved(holder.getAdapterPosition());
                                        notifyItemRangeChanged(holder.getAdapterPosition(), listaComandas.size());

                                        Toast.makeText(context, "Comanda eliminada", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                Toast.makeText(context, "Esta comanda ya se está preparando, habla con cocina para eliminarla.", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return listaComandas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblKey, lblFecha, lblNombreCliente, lblEstado, lblSubtotal;
        ImageButton btnEliminar, btnEditar;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblKey = itemView.findViewById(R.id.lblKey);
            lblFecha = itemView.findViewById(R.id.lblFecha);
            lblNombreCliente = itemView.findViewById(R.id.lvlNombreCliente);
            lblEstado = itemView.findViewById(R.id.lblEstado);
            lblSubtotal = itemView.findViewById(R.id.lblSubtotal);
            btnEliminar = itemView.findViewById(R.id.imgBtnHistorialComandaDelete);
            btnEditar = itemView.findViewById(R.id.imgBtnHistorialComandaEdit);
        }
    }

    private void mostrarDialogoEditar(Comanda comanda, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.mesero_dialog_editar_comanda, null);
        builder.setView(view);

        EditText editNombreCliente = view.findViewById(R.id.editNombreCliente);
        RecyclerView rvProductosEditar = view.findViewById(R.id.rvProductosEditar);
        TextView txtSubtotal = view.findViewById(R.id.txtSubtotal);
        Button btnGuardarCambios = view.findViewById(R.id.btnGuardarCambios);

        editNombreCliente.setText(comanda.getNombreCliente());

        List<ProductosComanda> listaEditable = new ArrayList<>(comanda.getProductosComanda());
        EditarProductoComandaAdapter editarAdapter = new EditarProductoComandaAdapter(listaEditable, updatedSubtotal -> {
            txtSubtotal.setText("Subtotal: $" + String.format("%.2f", updatedSubtotal));
        });

        rvProductosEditar.setLayoutManager(new LinearLayoutManager(context));
        rvProductosEditar.setAdapter(editarAdapter);

        double subtotalInicial = calcularSubtotal(listaEditable);
        txtSubtotal.setText("Subtotal: $" + String.format("%.2f", subtotalInicial));

        AlertDialog dialog = builder.create();

        btnGuardarCambios.setOnClickListener(v -> {
            String nuevoNombre = editNombreCliente.getText().toString().trim();
            comanda.setNombreCliente(nuevoNombre);
            comanda.setProductosComanda(listaEditable);
            comanda.setTotalPagar(String.format("%.2f", calcularSubtotal(listaEditable)));

            // Aquí actualizas en Firebase
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comandas").child(comanda.getKey());
            ref.setValue(comanda).addOnSuccessListener(aVoid -> {
                notifyItemChanged(position);
                Toast.makeText(context, "Comanda actualizada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }

    private double calcularSubtotal(List<ProductosComanda> productos) {
        double total = 0.0;
        for (ProductosComanda p : productos) {
            total += Double.parseDouble(p.getMenu().getPrecio()) * p.getCantidad();
        }
        return total;
    }
}
