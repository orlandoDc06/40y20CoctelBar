package com.example.a40y20coctelbar.OpcionesAdministrador;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.models.Comanda;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Reporte extends AppCompatActivity {

    private TextView txtVentasDia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_reporte);

        txtVentasDia = findViewById(R.id.txtVentasDia);

        obtenerVentasDelDia();
    }

    private void obtenerVentasDelDia() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comandas");

        String fechaHoy = obtenerFechaSinHora(); // "28/05/2025"

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double total = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comanda comanda = ds.getValue(Comanda.class);
                    if (comanda != null && comanda.getFecha() != null) {
                        // Extraer solo la fecha sin hora: "28/05/2025"
                        String fechaComanda = comanda.getFecha().split(" ")[0];

                        if (fechaHoy.equals(fechaComanda)) {
                            try {
                                total += Double.parseDouble(comanda.getTotalPagar());
                            } catch (NumberFormatException e) {
                                Log.w("Dashboard", "Error al convertir monto: " + e.getMessage());
                            }
                        }
                    }
                }
                txtVentasDia.setText("$" + String.format("%.2f", total));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Reporte.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String obtenerFechaSinHora() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
}