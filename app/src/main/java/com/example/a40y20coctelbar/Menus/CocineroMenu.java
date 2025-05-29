package com.example.a40y20coctelbar.Menus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a40y20coctelbar.MainActivity;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.adaptersCocinero.ComandaAdapter;
import com.example.a40y20coctelbar.models.Comanda;
import com.example.a40y20coctelbar.models.ProductosComanda;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CocineroMenu extends AppCompatActivity {

    private FirebaseAuth miAuth;
    private Button btnCerrarSesion;
    private ComandaAdapter adapter;
    private RecyclerView recyclerView;
    private List<Comanda> listaComandas;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cocinero_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();
        cargarComandas();

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
    }

    private void initializeViews() {
        miAuth = FirebaseAuth.getInstance();
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        recyclerView = findViewById(R.id.rcvComandaCocinero);

        // Inicializar Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar lista
        listaComandas = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComandaAdapter(this, listaComandas);
        recyclerView.setAdapter(adapter);
    }

    private void cargarComandas() {
        // Cargar comandas desde Firebase
        databaseReference.child("comandas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaComandas.clear();

                for (DataSnapshot comandaSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Comanda comanda = comandaSnapshot.getValue(Comanda.class);
                        if (comanda != null) {
                            // Establecer el ID de la comanda desde la key
                            comanda.setKey(comandaSnapshot.getKey());

                            // Solo mostrar comandas que no estén en estado "Listo"
                            String estado = comanda.getEstadoComanda();
                            if (estado != null && !estado.equalsIgnoreCase("listo")) {
                                listaComandas.add(comanda);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("CocineroMenu", "Error: " + e.getMessage());
                    }
                }

                // Actualizar el adaptador
                adapter.updateList(listaComandas);

                // Mostrar mensaje si no hay datos
                if (listaComandas.isEmpty()) {
                    Toast.makeText(CocineroMenu.this, "No hay comandas pendientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CocineroMenu.this, "Error al cargar comandas: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("CocineroMenu", "Error de Firebase: " + databaseError.getMessage());
            }
        });
    }

    private void cerrarSesion() {
        miAuth.signOut();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CocineroMenu.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}