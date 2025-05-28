package com.example.a40y20coctelbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.a40y20coctelbar.utils.AuthManager;
import com.example.a40y20coctelbar.utils.NavigationManager;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements AuthManager.CallbackAutenticacion {

    private EditText txtEmail, txtPassword;
    private TextView btnCrear;
    private Button btnIniciar, btnCrearGoogle;
    private AuthManager authManager;

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

        configurarWindowInsets();
        inicializarVistas();
        inicializarAuthManager();
        configurarEventListeners();
        verificarSesionActiva();
    }

    private void configurarWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarVistas() {
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnCrear = findViewById(R.id.btnCrear);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnCrearGoogle = findViewById(R.id.btnCrearGoogle);
    }

    private void inicializarAuthManager() {
        authManager = new AuthManager(this, this);
    }

    private void configurarEventListeners() {
        btnCrear.setOnClickListener(v -> registrarUsuario());
        btnIniciar.setOnClickListener(v -> iniciarSesionUsuario());
        btnCrearGoogle.setOnClickListener(v -> iniciarSesionConGoogle());
    }

    private void verificarSesionActiva() {
        authManager.verificarSesionActiva();
    }

    private void registrarUsuario() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        authManager.registrarConEmailYPassword(email, password);
    }

    private void iniciarSesionUsuario() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        authManager.iniciarSesionConEmailYPassword(email, password);
    }

    private void iniciarSesionConGoogle() {
        authManager.iniciarSesionConGoogle();
    }

    // Implementación de AuthManager.CallbackAutenticacion
    @Override
    public void alExitoAutenticacion(FirebaseUser user, String rol) {
        NavigationManager.navegarSegunRol(this, user, rol);
        NavigationManager.finalizarActivityActual(this);
    }

    @Override
    public void alErrorAutenticacion(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void alEnviarVerificacionEmail() {
        Toast.makeText(this, "Se envió un correo de verificación", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void alCancelarAutenticacion() {
        // Este método se llama cuando el usuario cancela la autenticación con Google
        // Puedes mostrar un mensaje o simplemente no hacer nada
        Toast.makeText(this, "Autenticación cancelada", Toast.LENGTH_SHORT).show();
    }
}