package com.example.a40y20coctelbar.fragmentsAdmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.adapters.CocineroAdapter;
import com.example.a40y20coctelbar.models.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class CocineroFragment extends Fragment {
    View view;
    private Button btnAbrirDialog;
    private static final String TAG = "CocineroFragment";
    private RecyclerView recyclerCocineros;
    private CocineroAdapter cocineroAdapter;
    private List<Usuario> usuarioList;
    private List<Usuario> cocineroList;
    private DatabaseReference databaseReference;



    public static CocineroFragment newInstance(String param1, String param2) {
        CocineroFragment fragment = new CocineroFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.admin_fragment_cocinero, container, false);

        // Inicializar Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        initViews();

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar usuarios desde la base de datos
        cargarUsuarios();

        return view;
    }

    private void initViews() {
        btnAbrirDialog = view.findViewById(R.id.btnAbrirNewCocinero);
        recyclerCocineros = view.findViewById(R.id.recyclerCocineros);

        // Verificar que el RecyclerView existe
        if (recyclerCocineros == null) {
            Log.e(TAG, "RecyclerView no encontrado en el layout");
            Toast.makeText(getContext(), "Error: RecyclerView no encontrado", Toast.LENGTH_LONG).show();
            return;
        }

        // Configurar botón
        btnAbrirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private void setupRecyclerView() {
        // Verificar que el RecyclerView existe antes de configurarlo
        if (recyclerCocineros == null) {
            Log.e(TAG, "No se puede configurar RecyclerView porque es null");
            return;
        }

        usuarioList = new ArrayList<>();
        cocineroList = new ArrayList<>(); // Lista filtrada para cocinero
        cocineroAdapter = new CocineroAdapter(cocineroList, getContext());
        recyclerCocineros.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCocineros.setAdapter(cocineroAdapter);

        Log.d(TAG, "RecyclerView configurado correctamente");
    }

    private void cargarUsuarios() {
        if (databaseReference == null) {
            Log.e(TAG, "DatabaseReference es null");
            return;
        }

        databaseReference.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (usuarioList == null || cocineroList == null) {
                    Log.e(TAG, "usuarioList o CocineroList es null");
                    return;
                }

                usuarioList.clear();
                cocineroList.clear();

                for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Usuario usuario = usuarioSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            // Guardar la clave del usuario
                            usuario.setKey(usuarioSnapshot.getKey());
                            usuarioList.add(usuario);

                            // Filtrar solo los Cocinero
                            if ("Cocinero".equals(usuario.getRol())) {
                                cocineroList.add(usuario);
                                Log.d(TAG, "Cocinero cargado: " + usuario.getCorreo() + " - " + usuario.getNombre());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar usuario: " + e.getMessage());
                    }
                }

                // Notificar al adapter que los datos han cambiado
                if (cocineroAdapter != null) {
                    cocineroAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "cocineroAdapter es null");
                }

                Log.d(TAG, "Total usuarios cargados: " + usuarioList.size());
                Log.d(TAG, "Total Cocinero cargados: " + cocineroList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al cargar usuarios: " + databaseError.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar usuarios: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}