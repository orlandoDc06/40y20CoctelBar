package com.example.a40y20coctelbar.utils;

import android.util.Log;

import com.example.a40y20coctelbar.models.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

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

    // Verifica si un usuario existe en la base de datos
    public static void verificarUsuarioExiste(String userId, DatabaseReference databaseReference, ListenerVerificacionUsuario listener) {
        if (userId == null || userId.isEmpty()) {
            listener.alError("ID de usuario inválido");
            return;
        }

        if (databaseReference == null) {
            listener.alError("DatabaseReference es null");
            return;
        }

        databaseReference.child("usuarios").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean existe = task.getResult().exists();
                        listener.alResultado(existe);
                    } else {
                        Log.e(TAG, "Error al verificar usuario: " + task.getException());
                        listener.alError("Error al verificar usuario: " + task.getException().getMessage());
                    }
                });
    }


    public static void actualizarDatosBasicosUsuario(FirebaseUser firebaseUser, DatabaseReference databaseReference) {
        if (firebaseUser == null || databaseReference == null) {
            Log.e(TAG, "FirebaseUser o DatabaseReference es null");
            return;
        }

        String userId = firebaseUser.getUid();

        Map<String, Object> actualizaciones = new HashMap<>();

        String nombre = obtenerNombreUsuario(firebaseUser);
        actualizaciones.put("nombre", nombre);

        String fotoPerfil = firebaseUser.getPhotoUrl() != null ?
                firebaseUser.getPhotoUrl().toString() : "";
        actualizaciones.put("pp", fotoPerfil);

        if (firebaseUser.getEmail() != null) {
            actualizaciones.put("correo", firebaseUser.getEmail());
        }

        databaseReference.child("usuarios").child(userId).updateChildren(actualizaciones)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Datos básicos del usuario actualizados exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar datos básicos: " + e.getMessage());
                });
    }

    //guarda usuario solo si no existe
    public static void guardarUsuarioSiNoExiste(FirebaseUser firebaseUser, String rol, DatabaseReference databaseReference, ListenerGuardadoUsuario listener) {
        if (firebaseUser == null) {
            if (listener != null) {
                listener.alError("FirebaseUser es null");
            }
            return;
        }

        String userId = firebaseUser.getUid();

        verificarUsuarioExiste(userId, databaseReference, new ListenerVerificacionUsuario() {
            @Override
            public void alResultado(boolean existe) {
                if (existe) {
                    Log.d(TAG, "Usuario ya existe, actualizando solo datos básicos");
                    actualizarDatosBasicosUsuario(firebaseUser, databaseReference);
                    if (listener != null) {
                        listener.alExitoUsuarioExistente();
                    }
                } else {
                    Log.d(TAG, "Usuario nuevo, guardando completo");
                    guardarUsuarioEnBaseDatos(firebaseUser, rol, databaseReference);
                    if (listener != null) {
                        listener.alExitoUsuarioNuevo();
                    }
                }
            }

            @Override
            public void alError(String error) {
                if (listener != null) {
                    listener.alError(error);
                }
            }
        });
    }

    private static Usuario crearUsuarioDesdeFirebaseUser(FirebaseUser firebaseUser, String rol) {
        Usuario usuario = new Usuario();

        usuario.setCorreo(firebaseUser.getEmail());

        String nombre = obtenerNombreUsuario(firebaseUser);
        usuario.setNombre(nombre);

        String fotoPerfil = firebaseUser.getPhotoUrl() != null ?
                firebaseUser.getPhotoUrl().toString() : "";
        usuario.setPp(fotoPerfil);

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

    // INTERFACES
    public interface ListenerActualizacionRol {
        void alExito();
        void alError(String error);
    }

    public interface ListenerVerificacionUsuario {
        void alResultado(boolean existe);
        void alError(String error);
    }

    public interface ListenerGuardadoUsuario {
        void alExitoUsuarioNuevo();
        void alExitoUsuarioExistente();
        void alError(String error);
    }
}