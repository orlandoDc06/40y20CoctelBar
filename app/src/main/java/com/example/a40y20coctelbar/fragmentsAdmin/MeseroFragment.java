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
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.adapters.UsuarioAdapter;
import com.example.a40y20coctelbar.dialogsAdmin.MeseroDialog;
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
    private Button btnAbrirDialog;

    private RecyclerView recyclerMeseros;
    private UsuarioAdapter usuarioAdapter;
    private List<Usuario> usuarioList;
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
        btnAbrirDialog = view.findViewById(R.id.btnAbrirNewMesero);
        recyclerMeseros = view.findViewById(R.id.recyclerMeseros);

        // Verificar que el RecyclerView existe
        if (recyclerMeseros == null) {
            Log.e(TAG, "RecyclerView no encontrado en el layout");
            Toast.makeText(getContext(), "Error: RecyclerView no encontrado", Toast.LENGTH_LONG).show();
            return;
        }

        // Configurar botón
        btnAbrirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MeseroDialog dialog = new MeseroDialog();
                dialog.show(requireActivity().getSupportFragmentManager(), "mesero");
            }
        });


    }

    private void setupRecyclerView() {
        // Verificar que el RecyclerView existe antes de configurarlo
        if (recyclerMeseros == null) {
            Log.e(TAG, "No se puede configurar RecyclerView porque es null");
            return;
        }

        usuarioList = new ArrayList<>();
        usuarioAdapter = new UsuarioAdapter(usuarioList, getContext());
        recyclerMeseros.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMeseros.setAdapter(usuarioAdapter);

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
                if (usuarioList == null) {
                    Log.e(TAG, "usuarioList es null");
                    return;
                }

                usuarioList.clear();

                for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Usuario usuario = usuarioSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            // Guardar la clave del usuario (UID de Firebase Auth)
                            usuario.setKey(usuarioSnapshot.getKey());
                            usuarioList.add(usuario);
                            Log.d(TAG, "Usuario cargado: " + usuario.getCorreo() + " - " + usuario.getNombre());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar usuario: " + e.getMessage());
                    }
                }

                // Notificar al adapter que los datos han cambiado
                if (usuarioAdapter != null) {
                    usuarioAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "usuarioAdapter es null");
                }

                Log.d(TAG, "Total usuarios cargados: " + usuarioList.size());
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