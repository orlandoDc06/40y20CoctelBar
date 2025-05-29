package com.example.a40y20coctelbar.dialogsAdmin;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.a40y20coctelbar.models.Menu;
import com.example.a40y20coctelbar.models.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MenuProductosDialog extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String ARG_MENU_ITEM = "menu_item";

    private ImageView imgFotoPlatillo;
    private Button btnSeleccionarImagen;
    private EditText etNombrePlatillo, etDescripcionPlatillo, etPrecioPlatillo;
    private Button btnGuardar;
    private TextView btnCancelar;
    private Spinner spCategoriaPlatillo;

    private Uri imageUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private Menu menuEdit;
    private boolean modeEdit = false;
    private String originalImageUrl;

    // Array de categorías
    private String[] categorias = {"Sodas", "Cocteles", "Shots", "Cervezas", "Vinos", "Boquitas"};

    //crear una nueva instancia para agregar
    public static MenuProductosDialog newInstance() {
        return new MenuProductosDialog();
    }

    //crear una nueva instancia para editar
    public static MenuProductosDialog newInstance(Menu menu) {
        MenuProductosDialog dialog = new MenuProductosDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MENU_ITEM, menu);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Verificar si se está editando
        if (getArguments() != null) {
            menuEdit = (Menu) getArguments().getSerializable(ARG_MENU_ITEM);
            if (menuEdit != null) {
                modeEdit = true;
                originalImageUrl = menuEdit.getFoto();
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_dialog_menu_productos, container, false);

        //Inicializar vistas
        AsociarElementosXml(view);

        //Configurar el spinner
        setupSpinner();

        //Si estamos en modo edición, cargar
        if (modeEdit && menuEdit != null) {
            CargarDatosExistentes();
        }

        //Configurar listeners
        setupListeners();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(modeEdit ? "Actualizando platillo..." : "Guardando platillo...");
        progressDialog.setCancelable(false);

        return view;
    }

    private void AsociarElementosXml(View view) {
        imgFotoPlatillo = view.findViewById(R.id.imgFotoPlatillo);
        btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen);
        etNombrePlatillo = view.findViewById(R.id.etNombrePlatillo);
        etDescripcionPlatillo = view.findViewById(R.id.etDescripcionPlatillo);
        etPrecioPlatillo = view.findViewById(R.id.etPrecioPlatillo);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        spCategoriaPlatillo = view.findViewById(R.id.spCategoriaProducto);

        if (modeEdit) {
            btnGuardar.setText("Actualizar");
        }
    }

    private void setupSpinner() {
        // Crear adapter para el spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoriaPlatillo.setAdapter(adapter);
    }

    private void CargarDatosExistentes() {
        etNombrePlatillo.setText(menuEdit.getNombre());
        etDescripcionPlatillo.setText(menuEdit.getDescripcion());
        etPrecioPlatillo.setText(menuEdit.getPrecio());

        // Cargar categoría en el spinner
        if (menuEdit.getCategoria() != null && !menuEdit.getCategoria().isEmpty()) {
            for (int i = 0; i < categorias.length; i++) {
                if (categorias[i].equals(menuEdit.getCategoria())) {
                    spCategoriaPlatillo.setSelection(i);
                    break;
                }
            }
        }

        // Cargar imagen existente
        if (menuEdit.getFoto() != null && !menuEdit.getFoto().isEmpty()) {
            Glide.with(this)
                    .load(menuEdit.getFoto())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(imgFotoPlatillo);
        }
    }

    private void setupListeners() {
        btnSeleccionarImagen.setOnClickListener(v -> openImageChooser());
        btnGuardar.setOnClickListener(v -> {
            if (modeEdit) {
                actualizarPlatillo();
            } else {
                guardarPlatillo();
            }
        });
        btnCancelar.setOnClickListener(v -> dismiss());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imgFotoPlatillo.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarPlatillo() {
        // Validar campos
        String nombre = etNombrePlatillo.getText().toString().trim();
        String descripcion = etDescripcionPlatillo.getText().toString().trim();
        String precioStr = etPrecioPlatillo.getText().toString().trim();
        String categoria = spCategoriaPlatillo.getSelectedItem().toString();

        if (!validarCampos(nombre, descripcion, precioStr)) {
            return;
        }

        if (imageUri == null) {
            Toast.makeText(getContext(), "Por favor seleccione una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            etPrecioPlatillo.setError("Precio inválido");
            return;
        }

        // Mostrar progress
        progressDialog.show();

        // Subir imagen y guardar
        subirImagenYGuardarPlatillo(nombre, descripcion, precio, categoria);
    }

    private void actualizarPlatillo() {
        // Validar campos
        String nombre = etNombrePlatillo.getText().toString().trim();
        String descripcion = etDescripcionPlatillo.getText().toString().trim();
        String precioStr = etPrecioPlatillo.getText().toString().trim();
        String categoria = spCategoriaPlatillo.getSelectedItem().toString();

        if (!validarCampos(nombre, descripcion, precioStr)) {
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            etPrecioPlatillo.setError("Precio inválido");
            return;
        }

        // Mostrar progress
        progressDialog.show();

        // Si se seleccionó una nueva imagen, subirla primero
        if (imageUri != null) {
            subirNuevaImagenYActualizar(nombre, descripcion, precio, categoria);
        } else {
            // Solo actualizar los datos sin cambiar la imagen
            actualizarPlatilloEnDatabase(nombre, descripcion, precio, categoria, originalImageUrl);
        }
    }

    private boolean validarCampos(String nombre, String descripcion, String precioStr) {
        if (TextUtils.isEmpty(nombre)) {
            etNombrePlatillo.setError("Ingrese el nombre del platillo");
            return false;
        }

        if (TextUtils.isEmpty(descripcion)) {
            etDescripcionPlatillo.setError("Ingrese la descripción");
            return false;
        }

        if (TextUtils.isEmpty(precioStr)) {
            etPrecioPlatillo.setError("Ingrese el precio");
            return false;
        }

        return true;
    }

    private void subirImagenYGuardarPlatillo(String nombre, String descripcion, double precio, String categoria) {
        // Generar nombre único para la imagen
        String fileName = "platillos/" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        // Subir imagen
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Guardar platillo en la database
                        guardarPlatilloEnDatabase(nombre, descripcion, precio, categoria, imageUrl);
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error al obtener URL de la imagen", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    private void subirNuevaImagenYActualizar(String nombre, String descripcion, double precio, String categoria) {
        // Generar nombre único para la nueva imagen
        String fileName = "platillos/" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        // Subir nueva imagen
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga de la nueva imagen
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String newImageUrl = uri.toString();

                        //Eliminar imagen anterior
                        if (originalImageUrl != null && !originalImageUrl.isEmpty()) {
                            try {
                                StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(originalImageUrl);
                                oldImageRef.delete().addOnSuccessListener(unused -> {
                                    Log.d("MenuDialog", "Imagen anterior eliminada");
                                }).addOnFailureListener(e -> {
                                    Log.e("MenuDialog", "Error al eliminar imagen anterior: " + e.getMessage());
                                });
                            } catch (Exception e) {
                                Log.e("MenuDialog", "Error al obtener referencia de imagen anterior: " + e.getMessage());
                            }
                        }

                        // Actualizar platillo
                        actualizarPlatilloEnDatabase(nombre, descripcion, precio, categoria, newImageUrl);
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error al obtener URL de la nueva imagen", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error al subir la nueva imagen", Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarPlatilloEnDatabase(String nombre, String descripcion, double precio, String categoria, String imageUrl) {
        //Generar key
        String platilloKey = "platillo_" + System.currentTimeMillis();

        // objeto Menu
        Menu platillo = new Menu(platilloKey, nombre, descripcion, imageUrl, String.valueOf(precio), categoria);

        // Guardar nodo platillos
        databaseReference.child("platillos").child(platilloKey).setValue(platillo)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Platillo guardado exitosamente", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error al guardar el platillo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarPlatilloEnDatabase(String nombre, String descripcion, double precio, String categoria, String imageUrl) {
        // Crenado objeto Menu actualizado
        Menu platilloActualizado = new Menu(menuEdit.getKey(), nombre, descripcion, imageUrl, String.valueOf(precio), categoria);

        // Actualizar en Firebase
        databaseReference.child("platillos").child(menuEdit.getKey()).setValue(platilloActualizado)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Platillo actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error al actualizar el platillo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}