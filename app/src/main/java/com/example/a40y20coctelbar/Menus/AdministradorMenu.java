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
import com.example.a40y20coctelbar.R;

public class AdministradorMenu extends AppCompatActivity {

    CardView gestionUsersCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador_menu);

        gestionUsersCards = findViewById(R.id.cardGestionarUsuarios);
        gestionUsersCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministradorMenu.this, GestionarUsuarios.class);
                startActivity(intent);
            }
        });


    }
}