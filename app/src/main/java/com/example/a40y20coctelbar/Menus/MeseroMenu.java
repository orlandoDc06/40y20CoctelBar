package com.example.a40y20coctelbar.Menus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.a40y20coctelbar.OpcionesAdministrador.GestionarUsuarios;
import com.example.a40y20coctelbar.OpcionesAdministrador.MenuProductos;
import com.example.a40y20coctelbar.OpcionesAdministrador.ReglasNegocio;
import com.example.a40y20coctelbar.OpcionesAdministrador.Reporte;
import com.example.a40y20coctelbar.OpcionesMesero.CrearComanda;
import com.example.a40y20coctelbar.OpcionesMesero.HistorialComanda;
import com.example.a40y20coctelbar.R;

public class MeseroMenu extends AppCompatActivity {

    CardView crearComandaCard, historialComanda, verMenuCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesero_menu);

        crearComandaCard = findViewById(R.id.cardCrearComanda);
        historialComanda = findViewById(R.id.cardModificarComanda);
        verMenuCard = findViewById(R.id.cardMenuProductos);


        crearComandaCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeseroMenu.this, CrearComanda.class);
                startActivity(intent);
            }
        });

        historialComanda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeseroMenu.this, HistorialComanda.class);
                startActivity(intent);
            }
        });

        verMenuCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeseroMenu.this, MenuProductos.class);
                startActivity(intent);
            }
        });


    }
}