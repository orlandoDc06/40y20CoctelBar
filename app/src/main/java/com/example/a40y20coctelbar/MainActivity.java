package com.example.a40y20coctelbar;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText txtEmail, txtPassword;
    private Button btnCrear, btnIniciar, btnCrearGoogle;
    private FirebaseAuth miAuth;

    //CLASES PARA GOOGLE
    private CredentialManager credentialManager;
    private static final int RC_SING_IN = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AsociarElementosXml();
    }

    private void AsociarElementosXml(){
        credentialManager = CredentialManager.create(getBaseContext());

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnCrear = findViewById(R.id.btnCrear);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnCrearGoogle = findViewById(R.id.btnCrearGoogle);
        miAuth = FirebaseAuth.getInstance();


        //Evento registrar users
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // RegisterUser();
                RegisterGoogle();
            }
        });

        // EVENTO INICIAR SESION
        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IniciarSesion();
            }
        });

        //REGISTRARSE CON GOOGLE
        btnCrearGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterGoogle();
            }
        });
        
        if (miAuth.getCurrentUser() != null){
            Toast.makeText(this, "El usuario ya inicio sesion", Toast.LENGTH_SHORT).show();
        }
    }

    private void RegisterUser(){
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        if(email.isEmpty() || email.isBlank() || password.isBlank() || password.isEmpty()){
            Toast.makeText(this, "Debe rellenar todos los datos", Toast.LENGTH_SHORT).show();
        }
        else {
            miAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
               if(task.isSuccessful()){
                   FirebaseUser userCreate = miAuth.getCurrentUser();

                   if (userCreate != null){
                       userCreate.sendEmailVerification().addOnCompleteListener(task1 -> {
                           if (task1.isSuccessful()){
                               Toast.makeText(this, "Se envio un correo de verificacion", Toast.LENGTH_SHORT).show();
                           }
                           else {
                               Toast.makeText(this, "Fallo el envio", Toast.LENGTH_SHORT).show();
                           }
                       });
                   }
                   Toast.makeText(this, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
               }
               else {
                   System.out.println("Fallo en la creacion" + task.getException().getMessage());
               }
            });
        }
    }

    private void IniciarSesion(){
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        if(email.isEmpty() || email.isBlank() || password.isBlank() || password.isEmpty()){
            Toast.makeText(this, "Debe rellenar todos los datos", Toast.LENGTH_SHORT).show();
        }
        else {
            miAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    FirebaseUser userLogeado = miAuth.getCurrentUser();

                    if (userLogeado != null  && userLogeado.isEmailVerified()){

                        Toast.makeText(this, "Benvendio", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, "Debe verifiacar el correo", Toast.LENGTH_SHORT).show();

                    }
               }
               else {
                   System.out.println("Error inicio sesion" + task.getException().getMessage());
               }
            });
        }
    }

    //REGISTRO CON GOOLE
    private void RegisterGoogle(){
        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption
                .Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("540675848058-4r8brq7le1fo80ok2m94c6fpp8fm5dg9.apps.googleusercontent.com")
                .build();

        GetCredentialRequest request = new GetCredentialRequest
                .Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                getBaseContext(),
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        handleSignIn(getCredentialResponse.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e("ERROR", e.getMessage());
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        if(credential instanceof CustomCredential){
            CustomCredential customCredential = (CustomCredential) credential;
            Bundle credientialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credientialData);
            firebaseAuthWhitGoogle(googleIdTokenCredential.getIdToken());
        }
    }

    private void firebaseAuthWhitGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        miAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Log.d("EXITO","USUARIO REGISTRADO");
                    }
                    else{
                        Log.w("ERROR", "Fallo la creadion del usuario");
                    }
                });

    }
}