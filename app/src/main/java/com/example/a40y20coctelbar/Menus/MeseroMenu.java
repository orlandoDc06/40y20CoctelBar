package com.example.a40y20coctelbar.Menus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.a40y20coctelbar.MainActivity;
import com.example.a40y20coctelbar.OpcionesAdministrador.GestionarUsuarios;
import com.example.a40y20coctelbar.OpcionesAdministrador.MenuProductos;
import com.example.a40y20coctelbar.OpcionesAdministrador.ReglasNegocio;
import com.example.a40y20coctelbar.OpcionesAdministrador.Reporte;
import com.example.a40y20coctelbar.OpcionesMesero.CrearComanda;
import com.example.a40y20coctelbar.OpcionesMesero.HistorialComanda;
import com.example.a40y20coctelbar.OpcionesMesero.MenuComanda;
import com.example.a40y20coctelbar.R;
import com.google.firebase.auth.FirebaseAuth;

public class MeseroMenu extends AppCompatActivity {

    CardView crearComandaCard, historialComanda, verMenuCard;
    private Button btnCerrarSesion;
    private FirebaseAuth miAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesero_menu);

        crearComandaCard = findViewById(R.id.cardCrearComanda);
        historialComanda = findViewById(R.id.cardModificarComanda);
        verMenuCard = findViewById(R.id.cardMenuProductos);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        miAuth = FirebaseAuth.getInstance();

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
                Intent intent = new Intent(MeseroMenu.this, MenuComanda.class);
                startActivity(intent);
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });

    }

    private void cerrarSesion() {
        miAuth.signOut();

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MeseroMenu.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }

}