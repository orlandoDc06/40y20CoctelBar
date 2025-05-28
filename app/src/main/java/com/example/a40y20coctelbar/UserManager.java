package com.example.a40y20coctelbar;


import android.util.Log;

import com.example.a40y20coctelbar.models.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class UserManager {
    private static final String TAG = "UserManager";

    public static void guardarUsuarioEnBaseDatos(FirebaseUser firebaseUser, String rol, DatabaseReference databaseReference) {
        if (firebaseUser == null) {
            Log.e(TAG, "FirebaseUser es null, no se puede guardar");
            return;
        }

        if (databaseReference == null) {
            Log.e(TAG, "DatabaseReference es null, no se puede guardar");
            return;
        }

        // Crear objeto Usuario
        Usuario usuario = crearUsuarioDesdeFirebaseUser(firebaseUser, rol);

        // Guardar en Firebase
        String userId = firebaseUser.getUid();
        databaseReference.child("usuarios").child(userId).setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario guardado exitosamente: " + usuario.getCorreo());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar usuario: " + e.getMessage());
                });
    }

    private static Usuario crearUsuarioDesdeFirebaseUser(FirebaseUser firebaseUser, String rol) {
        Usuario usuario = new Usuario();

        // Establecer correo
        usuario.setCorreo(firebaseUser.getEmail());

        // Establecer nombre
        String nombre = obtenerNombreUsuario(firebaseUser);
        usuario.setNombre(nombre);

        // Establecer foto de perfil
        String fotoPerfil = firebaseUser.getPhotoUrl() != null ?
                firebaseUser.getPhotoUrl().toString() : "";
        usuario.setPp(fotoPerfil);

        // Establecer rol
        usuario.setRol(rol);

        return usuario;
    }

    private static String obtenerNombreUsuario(FirebaseUser firebaseUser) {
        String nombre = firebaseUser.getDisplayName();

        if (nombre == null || nombre.isEmpty()) {
            if (firebaseUser.getEmail() != null) {
                // Extraer nombre del correo
                int atIndex = firebaseUser.getEmail().indexOf("@");
                if (atIndex > 0) {
                    nombre = firebaseUser.getEmail().substring(0, atIndex);
                } else {
                    nombre = "Usuario";
                }
            } else {
                nombre = "Usuario";
            }
        }

        return nombre;
    }

    public static void actualizarRolUsuario(String userId, String nuevoRol, DatabaseReference databaseReference,
                                            ListenerActualizacionRol listener) {
        if (userId == null || userId.isEmpty()) {
            listener.alError("ID de usuario inválido");
            return;
        }

        databaseReference.child("usuarios").child(userId).child("rol").setValue(nuevoRol)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Rol actualizado correctamente para usuario: " + userId);
                    listener.alExito();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar rol: " + e.getMessage());
                    listener.alError("Error al actualizar rol: " + e.getMessage());
                });
    }

    public interface ListenerActualizacionRol {
        void alExito();

        void alError(String error);
    }
}