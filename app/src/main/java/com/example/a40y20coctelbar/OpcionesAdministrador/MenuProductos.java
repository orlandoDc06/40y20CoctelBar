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
import com.example.a40y20coctelbar.adaptersAdmin.MenuAdapter;
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

}