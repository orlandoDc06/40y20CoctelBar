package com.example.a40y20coctelbar.utils;


import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.a40y20coctelbar.Menus.AdministradorMenu;
import com.example.a40y20coctelbar.Menus.CocineroMenu;
import com.example.a40y20coctelbar.Menus.MeseroMenu;
import com.example.a40y20coctelbar.SinRol;
import com.google.firebase.auth.FirebaseUser;

public class NavigationManager {

    public enum RolUsuario {
        ADMINISTRADOR("administrador", AdministradorMenu.class),
        MESERO("mesero", MeseroMenu.class),
        COCINERO("cocinero", CocineroMenu.class),
        SIN_ROL("sin rol definido", SinRol.class);

        private final String nombreRol;
        private final Class<?> claseActivity;

        RolUsuario(String nombreRol, Class<?> claseActivity) {
            this.nombreRol = nombreRol;
            this.claseActivity = claseActivity;
        }

        public String getNombreRol() {
            return nombreRol;
        }

        public Class<?> getClaseActivity() {
            return claseActivity;
        }

        public static RolUsuario desdeString(String rol) {
            if (rol == null) return SIN_ROL;

            for (RolUsuario rolUsuario : values()) {
                if (rolUsuario.nombreRol.equalsIgnoreCase(rol.trim())) {
                    return rolUsuario;
                }
            }
            return SIN_ROL;
        }
    }

    public static void navegarSegunRol(Context context, FirebaseUser user, String rol) {
        RolUsuario rolUsuario = RolUsuario.desdeString(rol);

        Intent intent = new Intent(context, rolUsuario.getClaseActivity());
        context.startActivity(intent);


        String mensajeBienvenida = crearMensajeBienvenida(user, rolUsuario);
        Toast.makeText(context, mensajeBienvenida, Toast.LENGTH_SHORT).show();
    }

    private static String crearMensajeBienvenida(FirebaseUser user, RolUsuario rolUsuario) {
        String nombreUsuario = obtenerNombreMostrar(user);
        String textoRol = rolUsuario == RolUsuario.SIN_ROL ? "" : " - " + capitalizar(rolUsuario.getNombreRol());

        return "Bienvenido " + nombreUsuario + textoRol;
    }

    private static String obtenerNombreMostrar(FirebaseUser user) {
        if (user == null) return "Usuario";

        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }

        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            int atIndex = email.indexOf("@");
            if (atIndex > 0) {
                return email.substring(0, atIndex);
            }
        }

        return "Usuario";
    }

    private static String capitalizar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static void finalizarActivityActual(Context context) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).finish();
        }
    }
}