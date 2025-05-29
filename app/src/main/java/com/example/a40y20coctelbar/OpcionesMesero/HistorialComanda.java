package com.example.a40y20coctelbar.OpcionesMesero;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.adaptersMesero.HistorialComandaAdapter;
import com.example.a40y20coctelbar.models.Comanda;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistorialComanda extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistorialComandaAdapter adapter;
    private List<Comanda> listaComandas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mesero_historial_comanda);

        recyclerView = findViewById(R.id.rvHistorialComandas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaComandas = new ArrayList<>();
        adapter = new HistorialComandaAdapter(this, listaComandas);
        recyclerView.setAdapter(adapter);

        cargarComandas();
    }

    private void cargarComandas() {
        String uidMesero = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comandas");
        Query query = ref.orderByChild("mesero").equalTo(uidMesero);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaComandas.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comanda comanda = ds.getValue(Comanda.class);
                    listaComandas.add(comanda);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistorialComanda.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}