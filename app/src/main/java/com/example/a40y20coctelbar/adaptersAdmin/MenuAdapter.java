package com.example.a40y20coctelbar.adaptersAdmin;

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
import com.example.a40y20coctelbar.OpcionesAdministrador.MenuProductos;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsAdmin.MenuProductosDialog;
import com.example.a40y20coctelbar.models.Menu;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class    MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private Context context;
    private List<Menu> menuList;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public MenuAdapter(Context context, List<Menu> menuList) {
        this.context = context;
        this.menuList = menuList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.storageReference = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public MenuAdapter.MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new MenuAdapter.MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.MenuViewHolder holder, int position) {
        Menu menu = menuList.get(position);

        holder.lblNombrePlatillo.setText(menu.getNombre());
        holder.lblDescripcionPlatillo.setText(menu.getDescripcion());
        holder.lblPrecioPlatillo.setText("$" + menu.getPrecio());

        // Mostrar categoría
        if (menu.getCategoria() != null && !menu.getCategoria().isEmpty()) {
            holder.lblCategoriaPlatillo.setText(menu.getCategoria());
            holder.lblCategoriaPlatillo.setVisibility(View.VISIBLE);
        } else {
            holder.lblCategoriaPlatillo.setVisibility(View.GONE);
        }

        // Cargar imagen
        if (menu.getFoto() != null && !menu.getFoto().isEmpty()) {
            Glide.with(context)
                    .load(menu.getFoto())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        //EDITAR
        holder.imgBtnEditarPlatillo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Mostrar dialog de edición pasando los datos del platillo
                    if (context instanceof androidx.fragment.app.FragmentActivity) {
                        MenuProductosDialog dialog = MenuProductosDialog.newInstance(menu);
                        dialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "EditMenuDialog");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error al abrir editor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        //ELIMINAR
        holder.imgBtnEliminarPlatillo.setOnClickListener(v -> {
            eliminarPlatillo(menu);
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    private void eliminarPlatillo(Menu menu) {

        databaseReference.child("platillos").child(menu.getKey()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Eliminar imagen del Storage
                    if (menu.getFoto() != null && !menu.getFoto().isEmpty()) {
                        try {
                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(menu.getFoto());
                            imageRef.delete().addOnSuccessListener(unused -> {
                                System.out.println("Imagen eliminada del Storage");
                            }).addOnFailureListener(e -> {
                                System.out.println("Error al eliminar imagen: " + e.getMessage());
                            });
                        } catch (Exception e) {
                            System.out.println("Error al obtener referencia de imagen: " + e.getMessage());
                        }
                    }

                    Toast.makeText(context, "Platillo eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al eliminar el platillo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void updateList(List<Menu> newList) {
        this.menuList = newList;
        notifyDataSetChanged();
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView lblNombrePlatillo, lblDescripcionPlatillo, lblPrecioPlatillo, lblCategoriaPlatillo;
        ImageButton imgBtnEditarPlatillo, imgBtnEliminarPlatillo;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            lblNombrePlatillo = itemView.findViewById(R.id.lblNombrePlatillo);
            lblDescripcionPlatillo = itemView.findViewById(R.id.lblDescripcionPlatillo);
            lblPrecioPlatillo = itemView.findViewById(R.id.lblPrecioPlatillo);
            lblCategoriaPlatillo = itemView.findViewById(R.id.lblCategoriaPlatillo);
            imgBtnEditarPlatillo = itemView.findViewById(R.id.imgBtnEditarPlatillo);
            imgBtnEliminarPlatillo = itemView.findViewById(R.id.imgBtnEliminarPlatillo);
        }
    }
}
