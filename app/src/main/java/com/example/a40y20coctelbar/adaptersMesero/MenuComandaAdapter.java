package com.example.a40y20coctelbar.adaptersMesero;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.models.Menu;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MenuComandaAdapter extends RecyclerView.Adapter<MenuComandaAdapter.MenuComandaViewHolder> {

    Context context;
    List<Menu> menuList;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public MenuComandaAdapter(Context context, List<Menu> menuList) {
        this.context = context;
        this.menuList = menuList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.storageReference = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public MenuComandaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_menu, parent, false);
        return new MenuComandaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuComandaViewHolder holder, int position) {
        Menu menu = menuList.get(position);

        holder.nombre.setText(menu.getNombre());
        holder.descripcion.setText(menu.getDescripcion());
        holder.precio.setText("$" + menu.getPrecio());

        // Cargar imagen
        if (menu.getFoto() != null && !menu.getFoto().isEmpty()) {
            Glide.with(context)
                    .load(menu.getFoto())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.imagen);
        } else {
            holder.imagen.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public void filtrar(List<Menu> filtroMenu){
        this.menuList = filtroMenu;
        notifyDataSetChanged();
    }

    public class MenuComandaViewHolder extends RecyclerView.ViewHolder {

        TextView nombre, descripcion, precio;
        ImageView imagen;
        ImageButton btnAgregar;

        public  MenuComandaViewHolder(@NonNull View itemView){
            super(itemView);

            nombre = itemView.findViewById(R.id.lblMenuNombre);
            descripcion = itemView.findViewById(R.id.lblMenuDescripcion);
            precio = itemView.findViewById(R.id.lblMenuPrecio);
            imagen = itemView.findViewById(R.id.imgMenuImagen);
            btnAgregar = itemView.findViewById(R.id.imgBtnMenuAdd);

        }
    }




}
