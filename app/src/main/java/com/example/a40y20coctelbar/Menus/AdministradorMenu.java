package com.example.a40y20coctelbar.Menus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.example.a40y20coctelbar.R;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class AdministradorMenu extends AppCompatActivity {

    CardView gestionUsersCards, menuProductsCard, reporteCard, visualizaReglasCard;
    private Button btnCerrarSesion;
    private FirebaseAuth miAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador_menu);

        gestionUsersCards = findViewById(R.id.cardGestionarUsuarios);
        menuProductsCard = findViewById(R.id.cardMenuProductos);
        reporteCard = findViewById(R.id.cardReporte);
        visualizaReglasCard = findViewById(R.id.cardReglasNegocio);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        miAuth = FirebaseAuth.getInstance();



        gestionUsersCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministradorMenu.this, GestionarUsuarios.class);
                startActivity(intent);
            }
        });

        menuProductsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministradorMenu.this, MenuProductos.class);
                startActivity(intent);
            }
        });

        reporteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministradorMenu.this, Reporte.class);
                startActivity(intent);
            }
        });

        visualizaReglasCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministradorMenu.this, ReglasNegocio.class);
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

        Intent intent = new Intent(AdministradorMenu.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
}