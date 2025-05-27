package com.example.a40y20coctelbar.OpcionesAdministrador;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsAdmin.MenuProductosDialog;
import com.example.a40y20coctelbar.models.Menu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MenuProductos extends AppCompatActivity {

    private Button btnAbrirDialog;
    private RecyclerView recyclerMenuProductos;
    private MenuAdapter menuAdapter;
    private List<Menu> menuList;
    private DatabaseReference databaseReference;
    private ValueEventListener menuValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_menu_productos);

        initViews();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setupRecyclerView();
        cargarMenuDesdeFirebase();

        btnAbrirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuProductosDialog dialog = new MenuProductosDialog();
                dialog.show(getSupportFragmentManager(), "menuproductos");
            }
        });
    }

    private void initViews() {
        btnAbrirDialog = findViewById(R.id.btnAbrirNewProducto);
        recyclerMenuProductos = findViewById(R.id.recyclerMenuProductos);
    }

    private void setupRecyclerView() {
        menuList = new ArrayList<>();
        menuAdapter = new MenuAdapter(this, menuList);
        recyclerMenuProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerMenuProductos.setAdapter(menuAdapter);
    }

    private void cargarMenuDesdeFirebase() {
        // Crear el listener una sola vez
        menuValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        Menu menu = dataSnapshot.getValue(Menu.class);
                        if (menu != null) {
                            menu.setKey(dataSnapshot.getKey());
                            menuList.add(menu);
                        }
                    } catch (Exception e) {
                        System.out.println("Error al convertir: " + dataSnapshot.getKey() + " - " + e.getMessage());
                        continue;
                    }
                }

                menuAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MenuProductos.this, "Error al cargar el menú: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.child("platillos").addValueEventListener(menuValueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (menuValueEventListener != null) {
            databaseReference.child("platillos").removeEventListener(menuValueEventListener);
        }
    }

    // Clase Adapter

    public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

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
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
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
}