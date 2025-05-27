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
import com.example.a40y20coctelbar.adaptersAdmin.PendienteAdapter;
import com.example.a40y20coctelbar.models.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PendientesFragment extends Fragment {


    View view;
    private static final String TAG = "PendientesFragment";
    private RecyclerView recyclerPendientes;
    private PendienteAdapter pendienteAdapter;
    private List<Usuario> usuarioList;
    private List<Usuario> pendientesList;
    private DatabaseReference databaseReference;

    public static PendientesFragment newInstance(String param1, String param2) {
        PendientesFragment fragment = new PendientesFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.admin_fragment_pendientes, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        initViews();
        setupRecyclerView();
        cargarUsuarios();

        return view;
    }

    private void initViews() {
        recyclerPendientes = view.findViewById(R.id.recyclerPendientes);

        if (recyclerPendientes == null) {
            Log.e(TAG, "RecyclerView no encontrado en el layout");
            Toast.makeText(getContext(), "Error: RecyclerView no encontrado", Toast.LENGTH_LONG).show();
            return;
        }

    }

    private void setupRecyclerView() {
        if (recyclerPendientes == null) {
            Log.e(TAG, "No se puede configurar RecyclerView porque es null");
            return;
        }

        usuarioList = new ArrayList<>();
        pendientesList = new ArrayList<>();
        pendienteAdapter = new PendienteAdapter(pendientesList, getContext());
        recyclerPendientes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPendientes.setAdapter(pendienteAdapter);

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
                if (usuarioList == null || pendientesList == null) {
                    Log.e(TAG, "usuarioList o CocineroList es null");
                    return;
                }

                usuarioList.clear();
                pendientesList.clear();

                for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Usuario usuario = usuarioSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            usuario.setKey(usuarioSnapshot.getKey());
                            usuarioList.add(usuario);
                            // Filtrar solo los Cocinero
                            if ("Sin rol definido".equals(usuario.getRol())) {
                                pendientesList.add(usuario);
                                Log.d(TAG, "Pendiente cargado: " + usuario.getCorreo() + " - " + usuario.getNombre());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar usuario: " + e.getMessage());
                    }
                }

                // Notificar al adapter que los datos han cambiado
                if (pendienteAdapter != null) {
                    pendienteAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "pendienteAdapter es null");
                }

                Log.d(TAG, "Total usuarios cargados: " + usuarioList.size());
                Log.d(TAG, "Total pendientes cargados: " + pendientesList.size());
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