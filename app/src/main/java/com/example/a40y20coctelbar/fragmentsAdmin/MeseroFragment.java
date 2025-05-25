package com.example.a40y20coctelbar.fragmentsAdmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsAdmin.MeseroDialog;

public class MeseroFragment extends Fragment {

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
        view = inflater.inflate(R.layout.admin_fragment_mesero, container, false);

        //Para abrir el fragment dialog
        btnAbrirDialog = view.findViewById(R.id.btnAbrirNewMesero);

        btnAbrirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MeseroDialog dialog = new MeseroDialog();
                dialog.show(requireActivity().getSupportFragmentManager(), "mesero");
            }
        });
        return view;
    }

}