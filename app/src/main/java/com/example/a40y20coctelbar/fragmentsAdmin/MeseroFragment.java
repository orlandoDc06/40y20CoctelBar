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
import com.example.a40y20coctelbar.adaptersAdmin.UsuarioMeseroAdapter;
import com.example.a40y20coctelbar.models.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MeseroFragment extends Fragment {

    private static final String TAG = "MeseroFragment";

    View view;

    private RecyclerView recyclerMeseros;
    private UsuarioMeseroAdapter usuarioMeseroAdapter;
    private List<Usuario> usuarioList;
    private List<Usuario> meserosList; // Lista filtrada solo para meseros
    private DatabaseReference databaseReference;

    public static MeseroFragment newInstance() {
        MeseroFragment fragment = new MeseroFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.admin_fragment_mesero, container, false);

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
        recyclerMeseros = view.findViewById(R.id.recyclerMeseros);

        // Verificar que el RecyclerView existe
        if (recyclerMeseros == null) {
            Log.e(TAG, "RecyclerView no encontrado en el layout");
            Toast.makeText(getContext(), "Error: RecyclerView no encontrado", Toast.LENGTH_LONG).show();
            return;
        }

    }

    private void setupRecyclerView() {

        usuarioList = new ArrayList<>();
        meserosList = new ArrayList<>(); // Lista filtrada para meseros
        usuarioMeseroAdapter = new UsuarioMeseroAdapter(meserosList, getContext());
        recyclerMeseros.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMeseros.setAdapter(usuarioMeseroAdapter);

    }

    private void cargarUsuarios() {
        if (databaseReference == null) {
            Log.e(TAG, "DatabaseReference es null");
            return;
        }

        databaseReference.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (usuarioList == null || meserosList == null) {
                    Log.e(TAG, "usuarioList o meserosList es null");
                    return;
                }

                usuarioList.clear();
                meserosList.clear();

                for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Usuario usuario = usuarioSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            // Guardar la clave del usuario
                            usuario.setKey(usuarioSnapshot.getKey());
                            usuarioList.add(usuario);

                            // Filtrar solo los meseros
                            if ("Mesero".equals(usuario.getRol())) {
                                meserosList.add(usuario);
                                Log.d(TAG, "Mesero cargado: " + usuario.getCorreo() + " - " + usuario.getNombre());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar usuario: " + e.getMessage());
                    }
                }

                // Notificar al adapter que los datos han cambiado
                if (usuarioMeseroAdapter != null) {
                    usuarioMeseroAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "usuarioMeseroAdapter es null");
                }

                Log.d(TAG, "Total usuarios cargados: " + usuarioList.size());
                Log.d(TAG, "Total meseros cargados: " + meserosList.size());
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