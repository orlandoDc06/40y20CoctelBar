package com.example.a40y20coctelbar.OpcionesAdministrador;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.a40y20coctelbar.Menus.AdministradorMenu;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.fragmentsAdmin.AdministradorFragment;
import com.example.a40y20coctelbar.fragmentsAdmin.CocineroFragment;
import com.example.a40y20coctelbar.fragmentsAdmin.MeseroFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class GestionarUsuarios extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.admin_gestionar_usarios);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_meseros);
        bottomNav.setOnItemSelectedListener(navListener);

        Fragment selectedFragment = new MeseroFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();
                Fragment selectedFragment = null;

                if (itemId == R.id.nav_meseros) {
                    selectedFragment = new MeseroFragment();
                } else if (itemId == R.id.nav_cocineros) {
                    selectedFragment = new CocineroFragment();
                } else if (itemId == R.id.nav_administradores) {
                    selectedFragment = new AdministradorFragment();
                } else {
                    selectedFragment = new MeseroFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            };


}