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
import androidx.recyclerview.widget.RecyclerView;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.models.ProductosComanda;

import java.util.List;

public class EditarProductoComandaAdapter extends RecyclerView.Adapter<EditarProductoComandaAdapter.ProductoViewHolder> {

    public interface SubtotalChangeListener {
        void onSubtotalChanged(double nuevoSubtotal);
    }

    private List<ProductosComanda> listaProductos;
    private Context context;
    private SubtotalChangeListener subtotalChangeListener;

    public EditarProductoComandaAdapter(List<ProductosComanda> listaProductos, SubtotalChangeListener listener) {
        this.listaProductos = listaProductos;
        this.subtotalChangeListener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_editar_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        ProductosComanda producto = listaProductos.get(position);

        holder.lblNombre.setText(producto.getMenu().getNombre());
        holder.lblCantidad.setText("Cantidad: " + producto.getCantidad());
        holder.lblNota.setText("Nota: " + (producto.getNota().isEmpty() ? "Ninguna" : producto.getNota()));
        holder.lblPrecio.setText(String.format("Total: $%.2f", producto.getCantidad() * Double.parseDouble(producto.getMenu().getPrecio())));

        holder.btnEditar.setOnClickListener(v -> mostrarDialogoEditar(producto, position));
        holder.btnEliminar.setOnClickListener(v -> {
            listaProductos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listaProductos.size());
            recalcularSubtotal();
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    private void mostrarDialogoEditar(ProductosComanda producto, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.mesero_dialog_editar_producto, null);
        builder.setView(view);

        EditText editCantidad = view.findViewById(R.id.editCantidad);
        EditText editNota = view.findViewById(R.id.editNota);
        Button btnGuardar = view.findViewById(R.id.btnGuardarCambios);

        editCantidad.setText(String.valueOf(producto.getCantidad()));
        editNota.setText(producto.getNota());

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            int nuevaCantidad;
            try {
                nuevaCantidad = Integer.parseInt(editCantidad.getText().toString().trim());
                if (nuevaCantidad <= 0) {
                    Toast.makeText(context, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                return;
            }

            String nuevaNota = editNota.getText().toString().trim();
            producto.setCantidad(nuevaCantidad);
            producto.setNota(nuevaNota);

            notifyItemChanged(position);
            recalcularSubtotal();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void recalcularSubtotal() {
        double subtotal = 0.0;
        for (ProductosComanda p : listaProductos) {
            subtotal += Double.parseDouble(p.getMenu().getPrecio()) * p.getCantidad();
        }
        subtotalChangeListener.onSubtotalChanged(subtotal);
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView lblNombre, lblCantidad, lblNota, lblPrecio;
        ImageButton btnEditar, btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            lblNombre = itemView.findViewById(R.id.lblNombre);
            lblCantidad = itemView.findViewById(R.id.lblCantidad);
            lblNota = itemView.findViewById(R.id.lblNota);
            lblPrecio = itemView.findViewById(R.id.lblPrecio);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}