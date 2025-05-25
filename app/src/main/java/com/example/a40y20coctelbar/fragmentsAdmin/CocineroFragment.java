package com.example.a40y20coctelbar.fragmentsAdmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsAdmin.CocineroDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CocineroFragment extends Fragment {
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
        view = inflater.inflate(R.layout.admin_fragment_cocinero, container, false);

        //Para abrir el fragment dialog
        btnAbrirDialog = view.findViewById(R.id.btnAbrirNewCocinero);

        btnAbrirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CocineroDialog dialog = new CocineroDialog();
                dialog.show(requireActivity().getSupportFragmentManager(), "cocinero");
            }
        });
        return view;
    }


}