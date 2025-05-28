package com.example.a40y20coctelbar.adaptersMesero;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.models.ProductosComanda;

import java.util.ArrayList;

public class CarritoComandaAdapter extends RecyclerView.Adapter<CarritoComandaAdapter.CarritoViewHolder> {

private ArrayList<ProductosComanda> carrito;
private Context context;
private OnCarritoActionListener listener;

public interface OnCarritoActionListener {
    void onEditar(int position);
    void onEliminar(int position);
}

public CarritoComandaAdapter(Context context, ArrayList<ProductosComanda> carrito, OnCarritoActionListener listener) {
    this.context = context;
    this.carrito = carrito;
    this.listener = listener;
}

@NonNull
@Override
public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_rv_carrito, parent, false);
    return new CarritoViewHolder(view);
}

@Override
public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
    ProductosComanda producto = carrito.get(position);
    holder.tvNombre.setText(producto.getMenu().getNombre());
    holder.tvPrecio.setText(producto.getMenu().getPrecio());
    holder.tvCantidad.setText("Cantidad: " + producto.getCantidad());

    String nota = producto.getNota();
    holder.tvNota.setText(nota == null || nota.isEmpty() ? "Nota: Ninguna" : "Nota: " + nota);

    Glide.with(context)
            .load(producto.getMenu().getFoto())
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgProducto);

    holder.btnEditar.setOnClickListener(v -> {
        int pos = holder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) {
            listener.onEditar(pos);
        }
    });

    holder.btnEliminar.setOnClickListener(v -> {
        int pos = holder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) {
            listener.onEliminar(pos);
        }
    });
}

@Override
public int getItemCount() {
    return carrito.size();
}

public static class CarritoViewHolder extends RecyclerView.ViewHolder {
    ImageView imgProducto;
    TextView tvNombre, tvPrecio, tvCantidad, tvNota;
    ImageButton btnEditar, btnEliminar;

    public CarritoViewHolder(@NonNull View itemView) {
        super(itemView);
        imgProducto = itemView.findViewById(R.id.imgCarritoImagen);
        tvNombre = itemView.findViewById(R.id.lblCarritoNombre);
        tvPrecio = itemView.findViewById(R.id.lblCarritoPrecio);
        tvCantidad = itemView.findViewById(R.id.lblCarritoCantidad);
        tvNota = itemView.findViewById(R.id.lblCarritoNota);
        btnEditar = itemView.findViewById(R.id.imgBtnCarritoEdit);
        btnEliminar = itemView.findViewById(R.id.imgBtnCarritoDelete);
    }
}
}
