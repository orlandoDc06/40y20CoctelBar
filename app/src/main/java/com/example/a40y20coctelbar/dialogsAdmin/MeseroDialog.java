package com.example.a40y20coctelbar.dialogsAdmin;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.models.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MeseroDialog extends DialogFragment {

    private static final String ARG_USUARIO = "usuario";
    private static final String TAG = "MeseroDialog";

    private Usuario usuario;
    private EditText txtEditNombreUsuario;
    private Spinner spEditRol;
    private ImageView imgEditUsuario;
    private Button btnGuardarEdit, btnEditCancelar;
    private DatabaseReference databaseReference;

    // Constructor estático para crear el dialog con datos del usuario
    public static MeseroDialog newInstance(Usuario usuario) {
        MeseroDialog fragment = new MeseroDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USUARIO, usuario);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuario = (Usuario) getArguments().getSerializable(ARG_USUARIO);
        }

        // Inicializar Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_dialog_mesero, container, false);

        initViews(view);
        setupSpinner();
        loadUserData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        txtEditNombreUsuario = view.findViewById(R.id.txtEditNombreUsuario);
        spEditRol = view.findViewById(R.id.spEditRol);
        imgEditUsuario = view.findViewById(R.id.imgEditUsuario);
        btnGuardarEdit = view.findViewById(R.id.btnGuardarEdit);
        btnEditCancelar = view.findViewById(R.id.btnEditCancelar);
    }

    private void setupSpinner() {
        // Crear array con los roles disponibles
        String[] roles = {"Administrador", "Cocinero", "Mesero"};

        // Crear adapter para el spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditRol.setAdapter(adapter);
    }

    private void loadUserData() {
        if (usuario != null) {
            // Cargar nombre
            String nombre = usuario.getNombre();
            if (nombre == null || nombre.isEmpty()) {
                if (usuario.getCorreo() != null && !usuario.getCorreo().isEmpty()) {
                    nombre = usuario.getCorreo().substring(0, usuario.getCorreo().indexOf("@"));
                } else {
                    nombre = "";
                }
            }
            txtEditNombreUsuario.setText(nombre);
            txtEditNombreUsuario.setHint("Nombre del usuario");

            // Cargar rol en el spinner
            String rolUsuario = usuario.getRol();
            if (rolUsuario != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spEditRol.getAdapter();
                int position = adapter.getPosition(rolUsuario);
                if (position >= 0) {
                    spEditRol.setSelection(position);
                }
            }

            // Cargar imagen de perfil
            if (usuario.getPp() != null && !usuario.getPp().isEmpty()) {
                Glide.with(this)
                        .load(usuario.getPp())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imgEditUsuario);
            } else {
                imgEditUsuario.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    private void setupClickListeners() {
        btnGuardarEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarUsuario();
            }
        });

        btnEditCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void actualizarUsuario() {
        if (usuario == null || usuario.getKey() == null) {
            Toast.makeText(getContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoNombre = txtEditNombreUsuario.getText().toString().trim();
        String nuevoRol = spEditRol.getSelectedItem().toString();

        // Validaciones
        if (nuevoNombre.isEmpty()) {
            txtEditNombreUsuario.setError("El nombre no puede estar vacío");
            return;
        }

        // Crear un mapa con los campos a actualizar
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nuevoNombre);
        updates.put("rol", nuevoRol);

        // Actualizar en Firebase
        databaseReference.child("usuarios").child(usuario.getKey())
                .updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Usuario actualizado correctamente");
                        Toast.makeText(getContext(), "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al actualizar usuario: " + e.getMessage());
                        Toast.makeText(getContext(), "Error al actualizar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}