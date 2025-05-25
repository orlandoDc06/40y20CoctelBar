package com.example.a40y20coctelbar.fragmentsAdmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsAdmin.AdministradorDialog;


public class AdministradorFragment extends Fragment {

    View view;
    private Button btnAbrirDialog;


    public static CocineroFragment newInstance(String param1, String param2) {
        CocineroFragment fragment = new CocineroFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.admin_fragment_administrador, container, false);

        //Para abrir el fragment dialog
        btnAbrirDialog = view.findViewById(R.id.btnAbrirNewAdministrador);

        btnAbrirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdministradorDialog dialog = new AdministradorDialog();
                dialog.show(requireActivity().getSupportFragmentManager(), "administrador");
            }
        });
        return view;
    }
}