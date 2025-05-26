package com.example.a40y20coctelbar.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.a40y20coctelbar.R;
import com.example.a40y20coctelbar.dialogsAdmin.MeseroDialog;
import com.example.a40y20coctelbar.models.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UsuarioMeseroAdapter extends RecyclerView.Adapter<UsuarioMeseroAdapter.UsuarioViewHolder> {

    private List<Usuario> usuarioList;
    private Context context;

    public UsuarioMeseroAdapter(List<Usuario> usuarioList, Context context) {
        this.usuarioList = usuarioList;
        this.context = context;
    }

    @NonNull
    @Override
    public UsuarioMeseroAdapter.UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuarios, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioMeseroAdapter.UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);

        // Configurar nombre si no tiene nombre se extrae del correo
        String nombreMostrar = usuario.getNombre();
        if (nombreMostrar == null || nombreMostrar.isEmpty()) {
            if (usuario.getCorreo() != null && !usuario.getCorreo().isEmpty()) {
                // Extraer nombre del correo (parte antes del @)
                nombreMostrar = usuario.getCorreo().substring(0, usuario.getCorreo().indexOf("@"));
            } else {
                nombreMostrar = "Usuario";
            }
        }
        holder.lblItemNombre.setText(nombreMostrar);

        // Configurar rol
        String rol = usuario.getRol();
        if (rol == null || rol.isEmpty()) {
            rol = "Sin rol definido";
        }
        holder.lblItemRol.setText(rol);

        // Configurar imagen de perfil
        if (usuario.getPp() != null && !usuario.getPp().isEmpty()) {
            // Usar Glide para cargar la imagen desde URL
            Glide.with(context)
                    .load(usuario.getPp())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imgItemUsuario);
        } else {
            // Imagen por defecto si no hay URL
            holder.imgItemUsuario.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // EDITAR USUARIO
        holder.imgItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear el dialog pasando los datos del usuario
                MeseroDialog dialog = MeseroDialog.newInstance(usuario);

                // Mostrar el dialog
                if (context instanceof AppCompatActivity) {
                    dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "edit_usuario");
                }
            }
        });

        // ELIMINAR USUARIO (opcional - puedes implementar esta funcionalidad también)
        holder.imgItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar confirmación antes de eliminar
                new AlertDialog.Builder(context)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que quieres eliminar ?")
                        .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                eliminarUsuario(usuario, position);
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

    }

    private void eliminarUsuario(Usuario usuario, int position) {
        if (usuario.getKey() != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("usuarios").child(usuario.getKey())
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                            // El listener en el Fragment actualizará automáticamente la lista
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error al eliminar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView lblItemNombre, lblItemRol;
        ImageView imgItemUsuario;
        ImageButton imgItemDelete, imgItemEdit;
        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);

            lblItemNombre = itemView.findViewById(R.id.lblItemNombre);
            lblItemRol = itemView.findViewById(R.id.lblItemRol);
            imgItemUsuario = itemView.findViewById(R.id.imgItemUsuario);
            imgItemDelete = itemView.findViewById(R.id.imgItemDelete);
            imgItemEdit = itemView.findViewById(R.id.imgItemEdit);
        }
    }
}
