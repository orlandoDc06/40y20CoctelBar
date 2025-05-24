package com.example.a40y20coctelbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText txtEmail, txtPassword;
    private Button btnCrear, btnIniciar;
    private FirebaseAuth miAuth;

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
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnCrear = findViewById(R.id.btnCrear);
        btnIniciar = findViewById(R.id.btnIniciar);
        miAuth = FirebaseAuth.getInstance();

        //Evento registrar users
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUser();
            }
        });

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IniciarSesion();
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
}