package com.example.a40y20coctelbar.OpcionesMesero;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a40y20coctelbar.OpcionesAdministrador.MenuProductos;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.adaptersAdmin.MenuAdapter;
import com.example.a40y20coctelbar.adaptersMesero.MenuComandaAdapter;
import com.example.a40y20coctelbar.models.CarritoComandaTemporal;
import com.example.a40y20coctelbar.models.Menu;
import com.example.a40y20coctelbar.models.ProductosComanda;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.List;

public class MenuComanda extends AppCompatActivity {

    EditText etBuscador;
    RecyclerView rvLista;
    MenuComandaAdapter menuComandaAdapter;
    private List<Menu> menuList;
    private DatabaseReference databaseReference;
    private ValueEventListener menuValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mesero_menu_productos);

        initViews();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setupRecyclerView();
        cargarMenuDesdeFirebase();
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filtrar(s.toString());
            }
        });

    }

    private void initViews() {
        etBuscador = findViewById(R.id.etBuscador);
        rvLista = findViewById(R.id.rvListaMenuComandas);
    }

    private void setupRecyclerView() {
        menuList = new ArrayList<>();
        menuComandaAdapter = new MenuComandaAdapter(MenuComanda.this, menuList, new MenuComandaAdapter.OnProductoAgregarListener() {
            @Override
            public void onAgregarClick(Menu menu) {
                mostrarDialogoAgregar(menu);
            }
        });
        rvLista.setLayoutManager(new GridLayoutManager(this, 1));
        rvLista.setAdapter(menuComandaAdapter);
    }

    private void cargarMenuDesdeFirebase() {
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

                menuComandaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MenuComanda.this, "Error al cargar el menú: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.child("platillos").addValueEventListener(menuValueEventListener);
    }

    private void mostrarDialogoAgregar(Menu menu) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar producto");

        View dialogView = getLayoutInflater().inflate(R.layout.mesero_dialog_agregar_producto, null);
        builder.setView(dialogView);

        EditText etCantidad = dialogView.findViewById(R.id.etCantidad);
        EditText etNota = dialogView.findViewById(R.id.etNota);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String cantidadStr = etCantidad.getText().toString();
            String nota = etNota.getText().toString();

            if (!cantidadStr.isEmpty()) {
                int cantidad = Integer.parseInt(cantidadStr);
                ProductosComanda producto = new ProductosComanda(menu, cantidad, nota);
                CarritoComandaTemporal.agregarProducto(producto);
                System.out.println(CarritoComandaTemporal.getListaProductos());
                Toast.makeText(this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        builder.create().show();
    }


    public void onCategoriaClick(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout categoriaLayout = (LinearLayout) view;
            TextView tvCategoria = (TextView) categoriaLayout.getChildAt(1);
            String categoria = tvCategoria.getTag().toString();

            if (categoria.equalsIgnoreCase("todo")) {
                menuComandaAdapter.filtrar(menuList);
            } else {
                filtrarPorCategoria(categoria);
            }
        }
    }

    public void filtrar(String texto){
            List<Menu> filtrarLista = new ArrayList<>();
            for(Menu menu: menuList){
                if(menu.getNombre().toLowerCase().contains(texto.toLowerCase())){
                    filtrarLista.add(menu);

                }
            }
            menuComandaAdapter.filtrar(filtrarLista);
    }

    public void filtrarPorCategoria(String categoria) {
        List<Menu> filtrarLista = new ArrayList<>();
        for(Menu menu : menuList){
            if(menu.getCategoria() != null && menu.getCategoria().equalsIgnoreCase(categoria)){
                filtrarLista.add(menu);
            }
        }

        menuComandaAdapter.filtrar(filtrarLista);
    }

}