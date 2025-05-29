package com.example.a40y20coctelbar.dialogsCocinero;

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
import com.example.a40y20coctelbar.dialogsAdmin.AdministradorDialog;
import com.example.a40y20coctelbar.models.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditarComandaDialog extends DialogFragment {

    private Spinner spinnerEstado;
    private Button btnActualizar, btnCerrar;
    private String comandaId;
    private String estadoActual;
    private OnEstadoActualizadoListener listener;

    // Interface para comunicar con la actividad padre
    public interface OnEstadoActualizadoListener {
        void onEstadoActualizado();
    }

    public static EditarComandaDialog newInstance(String comandaId, String estadoActual) {
        EditarComandaDialog dialog = new EditarComandaDialog();
        Bundle args = new Bundle();
        args.putString("comandaId", comandaId);
        args.putString("estadoActual", estadoActual);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            comandaId = getArguments().getString("comandaId");
            estadoActual = getArguments().getString("estadoActual");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cocinero_dialog_editar_comanda, container, false);

        initializeViews(view);
        setupSpinner();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        spinnerEstado = view.findViewById(R.id.spnEditarEstadoCocinero);
        btnActualizar = view.findViewById(R.id.button);
        btnCerrar = view.findViewById(R.id.button2);
    }

    private void setupSpinner() {
        // Crear array de estados
        String[] estados = {"Pendiente", "En preparación", "Listo", "Incidencia"};

        // Crear adapter para el spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        // Seleccionar el estado actual
        if (estadoActual != null) {
            for (int i = 0; i < estados.length; i++) {
                if (estados[i].equalsIgnoreCase(estadoActual)) {
                    spinnerEstado.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupClickListeners() {
        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarEstadoComanda();
            }
        });

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void actualizarEstadoComanda() {
        if (comandaId == null || comandaId.isEmpty()) {
            Toast.makeText(getContext(), "Error: ID de comanda no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoEstado = spinnerEstado.getSelectedItem().toString();

        DatabaseReference comandaRef = FirebaseDatabase.getInstance()
                .getReference("comandas")
                .child(comandaId);

        comandaRef.child("estadoComanda").setValue(nuevoEstado)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),
                                "Estado actualizado a: " + nuevoEstado,
                                Toast.LENGTH_SHORT).show();

                        if (listener != null) {
                            listener.onEstadoActualizado();
                        }

                        dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),
                                "Error al actualizar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void setOnEstadoActualizadoListener(OnEstadoActualizadoListener listener) {
        this.listener = listener;
    }
}