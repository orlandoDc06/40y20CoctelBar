package com.example.a40y20coctelbar.OpcionesAdministrador;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsAdmin.MenuProductosDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuProductos extends AppCompatActivity {

    private Button btnAbrirDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_menu_productos); // Reemplaza "tu_layout" con el nombre real del layout XML

        btnAbrirDialog = findViewById(R.id.btnAbrirNewProducto);

        btnAbrirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuProductosDialog dialog = new MenuProductosDialog();
                dialog.show(getSupportFragmentManager(), "menuproductos");
            }
        });
    }
}
