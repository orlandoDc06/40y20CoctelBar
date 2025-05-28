package com.example.a40y20coctelbar.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executors;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private static final String SERVER_CLIENT_ID = "540675848058-4r8brq7le1fo80ok2m94c6fpp8fm5dg9.apps.googleusercontent.com";

    private Context context;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private CredentialManager credentialManager;
    private CallbackAutenticacion authCallback;

    public interface CallbackAutenticacion {
        void alExitoAutenticacion(FirebaseUser user, String rol);
        void alErrorAutenticacion(String error);
        void alEnviarVerificacionEmail();
    }

    public AuthManager(Context context, CallbackAutenticacion callback) {
        this.context = context;
        this.authCallback = callback;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.credentialManager = CredentialManager.create(context);
    }

    public void registrarConEmailYPassword(String email, String password) {
        if (email.isEmpty() || email.isBlank() || password.isBlank() || password.isEmpty()) {
            authCallback.alErrorAutenticacion("Debe rellenar todos los datos");
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser userCreate = firebaseAuth.getCurrentUser();
                        if (userCreate != null) {
                            // Guardar usuario en la base de datos
                            UserManager.guardarUsuarioEnBaseDatos(userCreate, "Sin rol definido", databaseReference);

                            // Enviar verificación por email
                            userCreate.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            authCallback.alEnviarVerificacionEmail();
                                        } else {
                                            authCallback.alErrorAutenticacion("Fallo el envío de verificación");
                                        }
                                    });
                        }
                        Toast.makeText(context, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        authCallback.alErrorAutenticacion("Fallo en la creación: " + error);
                    }
                });
    }

    public void iniciarSesionConEmailYPassword(String email, String password) {
        if (email.isEmpty() || email.isBlank() || password.isBlank() || password.isEmpty()) {
            authCallback.alErrorAutenticacion("Debe rellenar todos los datos");
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser userLogeado = firebaseAuth.getCurrentUser();

                        if (userLogeado != null && userLogeado.isEmailVerified()) {
                            obtenerRolUsuarioYNavegar(userLogeado);
                        } else {
                            authCallback.alErrorAutenticacion("Debe verificar el correo");
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        authCallback.alErrorAutenticacion("Error inicio sesión: " + error);
                    }
                });
    }

    public void iniciarSesionConGoogle() {
        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(SERVER_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                context,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        manejarInicioSesionGoogle(getCredentialResponse.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Credential fetch failed: " + e.getMessage(), e);
                        authCallback.alErrorAutenticacion("Error con Google Sign-In: " + e.getMessage());
                    }
                }
        );
    }

    private void manejarInicioSesionGoogle(Credential credential) {
        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credentialData);
            autenticarFirebaseConGoogle(googleIdTokenCredential.getIdToken());
        }
    }

    private void autenticarFirebaseConGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "USUARIO REGISTRADO CON GOOGLE");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Guardar usuario
                            UserManager.guardarUsuarioEnBaseDatos(user, "Sin rol definido", databaseReference);
                            obtenerRolUsuarioYNavegar(user);
                        }
                    } else {
                        Log.w(TAG, "Fallo la creación del usuario con Google");
                        authCallback.alErrorAutenticacion("Error al autenticar con Google");
                    }
                });
    }

    private void obtenerRolUsuarioYNavegar(FirebaseUser user) {
        String userId = user.getUid();
        databaseReference.child("usuarios").child(userId).child("rol").get()
                .addOnCompleteListener(taskRol -> {
                    if (taskRol.isSuccessful()) {
                        String rol = String.valueOf(taskRol.getResult().getValue());
                        authCallback.alExitoAutenticacion(user, rol);
                    } else {
                        authCallback.alErrorAutenticacion("Error al obtener el rol del usuario");
                    }
                });
    }

    public void verificarSesionActiva() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            boolean esGoogle = currentUser.getProviderData().size() > 1 &&
                    currentUser.getProviderData().get(1).getProviderId().equals("google.com");

            if (currentUser.isEmailVerified() || esGoogle) {
                obtenerRolUsuarioYNavegar(currentUser);
            }
        }
    }
}