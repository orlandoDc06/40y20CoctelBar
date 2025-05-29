package com.example.a40y20coctelbar.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

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
    private Handler mainHandler; // Handler para ejecutar en el hilo principal

    public interface CallbackAutenticacion {
        void alExitoAutenticacion(FirebaseUser user, String rol);
        void alErrorAutenticacion(String error);
        void alEnviarVerificacionEmail();
        void alCancelarAutenticacion();
    }

    public AuthManager(Context context, CallbackAutenticacion callback) {
        this.context = context;
        this.authCallback = callback;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.credentialManager = CredentialManager.create(context);
        this.mainHandler = new Handler(Looper.getMainLooper()); // Inicializar handler
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
                            // Guardar usuario en la base de datos (nuevo usuario)
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


                        mainHandler.post(() -> {
                            Toast.makeText(context, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        authCallback.alErrorAutenticacion("Fallo en la creación: ");
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
                        authCallback.alErrorAutenticacion("Error inicio sesión ");
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
                        // Ejecutar en el hilo principal
                        mainHandler.post(() -> {
                            manejarInicioSesionGoogle(getCredentialResponse.getCredential());
                        });
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        // Ejecutar en el hilo principal
                        mainHandler.post(() -> {
                            manejarErrorGoogle(e);
                        });
                    }
                }
        );
    }

    private void manejarErrorGoogle(GetCredentialException e) {

        if (e instanceof GetCredentialCancellationException) {

            if (authCallback != null) {
                authCallback.alCancelarAutenticacion();
            }
        } else if (e instanceof NoCredentialException) {
            authCallback.alErrorAutenticacion("No hay cuentas de Google disponibles");
        } else {
            authCallback.alErrorAutenticacion("Error al inicar sesion");
        }
    }

    private void manejarInicioSesionGoogle(Credential credential) {
        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;

            // Verificar que sea una credencial de Google
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                try {
                    Bundle credentialData = customCredential.getData();
                    GoogleIdTokenCredential googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credentialData);
                    autenticarFirebaseConGoogle(googleIdTokenCredential.getIdToken());
                } catch (Exception e) {
                    authCallback.alErrorAutenticacion("Error al procesar ");
                }
            } else {
                authCallback.alErrorAutenticacion("Tipo de credencial no soportado");
            }
        } else {
            authCallback.alErrorAutenticacion("Credencial inválida");
        }
    }

    private void autenticarFirebaseConGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            authCallback.alErrorAutenticacion("Token de autenticación inválido");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "USUARIO REGISTRADO CON GOOGLE");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Verificar si el usuario ya existe antes de guardarlo
                            verificarYGuardarUsuarioGoogle(user);
                        }
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.w(TAG, "Fallo la autenticación con Firebase: " + errorMsg);
                        authCallback.alErrorAutenticacion("Error al iniciar sesión con Google: " + errorMsg);
                    }
                });
    }

    private void verificarYGuardarUsuarioGoogle(FirebaseUser user) {
        String userId = user.getUid();
        databaseReference.child("usuarios").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // si usuario ya existe, solo obtener el rol y navegar
                            obtenerRolUsuarioYNavegar(user);
                        } else {
                            // Usuario nuevo, guardarlo con rol por defecto
                            Log.d(TAG, "Usuario nuevo con Google, guardando en base de datos");
                            UserManager.guardarUsuarioEnBaseDatos(user, "Sin rol definido", databaseReference);
                            obtenerRolUsuarioYNavegar(user);
                        }
                    } else {
                        obtenerRolUsuarioYNavegar(user);
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