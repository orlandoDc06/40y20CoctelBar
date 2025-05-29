package com.example.a40y20coctelbar.adaptersCocinero;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsCocinero.EditarComandaDialog;
import com.example.a40y20coctelbar.models.Comanda;
import com.example.a40y20coctelbar.models.ProductosComanda;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ComandaAdapter extends RecyclerView.Adapter<ComandaAdapter.ComandaViewHolder> {

    private List<Comanda> listaComandas;
    private Context context;

    public ComandaAdapter(Context context, List<Comanda> listaComandas) {
        this.context = context;
        this.listaComandas = listaComandas;
    }

    @NonNull
    @Override
    public ComandaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comanda_agrupada, parent, false);
        return new ComandaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComandaViewHolder holder, int position) {
        Comanda comanda = listaComandas.get(position);

        holder.lblNombreCliente.setText("Cliente: " + comanda.getNombreCliente());
        holder.lblFechaComanda.setText("Fecha: " + comanda.getFecha());
        holder.lblEstadoComanda.setText("Estado: " + comanda.getEstadoComanda());

        if (comanda.getEstadoComanda().equalsIgnoreCase("en preparación")) {
            holder.lblEstadoComanda.setBackgroundResource(R.color.oro);
        } else if (comanda.getEstadoComanda().equalsIgnoreCase("pendiente")) {
            holder.lblEstadoComanda.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            holder.lblEstadoComanda.setBackgroundColor(Color.RED);
        }

        holder.lblTotalComanda.setText("Total: $" + comanda.getTotalPagar());

        holder.containerProductos.removeAllViews();

        // Agregar cada producto de la comanda
        if (comanda.getProductosComanda() != null) {
            for (ProductosComanda producto : comanda.getProductosComanda()) {
                View productoView = LayoutInflater.from(context).inflate(R.layout.item_producto_comanda, holder.containerProductos, false);

                ImageView imgProducto = productoView.findViewById(R.id.imgProducto);
                TextView lblNombreProducto = productoView.findViewById(R.id.lblNombreProducto);
                TextView lblCantidadProducto = productoView.findViewById(R.id.lblCantidadProducto);
                TextView lblNotaProducto = productoView.findViewById(R.id.lblNotaProducto);
                TextView lblPrecioProducto = productoView.findViewById(R.id.lblPrecioProducto);

                lblNombreProducto.setText(producto.getMenu().getNombre());
                lblCantidadProducto.setText("Cantidad: " + producto.getCantidad());
                lblNotaProducto.setText("Nota: " + (producto.getNota() != null && !producto.getNota().isEmpty() ? producto.getNota() : "Sin notas"));
                lblPrecioProducto.setText("$" + producto.getMenu().getPrecio());

                if (producto.getMenu().getFoto() != null && !producto.getMenu().getFoto().isEmpty()) {
                    Glide.with(context)
                            .load(producto.getMenu().getFoto())
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(imgProducto);
                } else {
                    imgProducto.setImageResource(R.drawable.ic_launcher_foreground);
                }
                holder.containerProductos.addView(productoView);
            }
        }

        // Configurar el click del botón para abrir el dialog
        holder.btnCompletarComanda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar que el contexto sea una AppCompatActivity
                if (context instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) context;

                    // Crear y mostrar el dialog
                    EditarComandaDialog dialog = EditarComandaDialog.newInstance(
                            comanda.getKey(), //
                            comanda.getEstadoComanda()
                    );

                    dialog.setOnEstadoActualizadoListener(new EditarComandaDialog.OnEstadoActualizadoListener() {
                        @Override
                        public void onEstadoActualizado() {
                            // Este callback se ejecutará cuando se actualice el estado
                            // La actividad padre se encargará de recargar los datos
                        }
                    });

                    dialog.show(activity.getSupportFragmentManager(), "EditarComandaDialog");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaComandas != null ? listaComandas.size() : 0;
    }

    // actualizar la lista
    public void updateList(List<Comanda> nuevaLista) {
        this.listaComandas = nuevaLista;
        notifyDataSetChanged();
    }

    public class ComandaViewHolder extends RecyclerView.ViewHolder {
        TextView lblNombreCliente, lblFechaComanda, lblEstadoComanda, lblTotalComanda;
        LinearLayout containerProductos;
        Button btnCompletarComanda;

        public ComandaViewHolder(@NonNull View itemView) {
            super(itemView);

            lblNombreCliente = itemView.findViewById(R.id.lblNombreCliente);
            lblFechaComanda = itemView.findViewById(R.id.lblFechaComanda);
            lblEstadoComanda = itemView.findViewById(R.id.lblEstadoComanda);
            lblTotalComanda = itemView.findViewById(R.id.lblTotalComanda);
            containerProductos = itemView.findViewById(R.id.containerProductos);
            btnCompletarComanda = itemView.findViewById(R.id.btnCompletarComanda);
        }
    }
}