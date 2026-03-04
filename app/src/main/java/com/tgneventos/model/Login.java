package com.tgneventos.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tgneventos.R;
import com.tgneventos.pojo.Usuario;
import com.tgneventos.util.NotificationPreferences;

import java.util.HashMap;
import java.util.Map;

public final class Login extends AppCompatActivity implements View.OnClickListener {
    private Button Registrar;
    private Button Iniciar;
    private EditText Correo;
    private EditText Password;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView recuperar_contraseña;

    // Variables de Google
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // --- INICIALIZACIÓN ---
        Registrar = findViewById(R.id.Registrar);
        Registrar.setOnClickListener(this);
        Iniciar = findViewById(R.id.Iniciar);
        Iniciar.setOnClickListener(this);
        Correo = findViewById(R.id.Correo);
        Password = findViewById(R.id.Password);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recuperar_contraseña = findViewById(R.id.btnRecuperarPass);

        // --- LÓGICA DE GOOGLE (Configuración) ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Asignar el click al botón de Google
        findViewById(R.id.btnGoogle).setOnClickListener(v -> iniciarSesionGoogle());

        // --- LÓGICA RECUPERAR PASS ---
        recuperar_contraseña.setOnClickListener(v -> {
            String correo = Correo.getText().toString().trim();
            if (correo.isEmpty()) {
                Correo.setError("Introduce tu correo");
                return;
            }
            mAuth.sendPasswordResetEmail(correo).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Correo enviado")
                            .setMessage("Revisa tu bandeja para restablecer la contraseña.")
                            .setPositiveButton("Ok", null).show();
                }
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // --- MÉTODOS DE GOOGLE ---

    private void iniciarSesionGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.w("GoogleAuth", "Fallo inicio Google", e);
                Toast.makeText(this, "Error al conectar con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Google ya verifica el email, así que vamos directo a Firestore
                        comprobarYCrearUsuarioEnFirestore(user);
                    } else {
                        Toast.makeText(this, "Error de autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void comprobarYCrearUsuarioEnFirestore(FirebaseUser user) {
        if (user == null) return;

        db.collection("Usuarios").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // El usuario ya existía, cargamos sus datos
                        ObtenerDatosUsuario(user.getUid());
                    } else {
                        // Es la primera vez que entra con Google: creamos su perfil
                        Map<String, Object> nuevoUsuario = new HashMap<>();
                        nuevoUsuario.put("email", user.getEmail());
                        nuevoUsuario.put("isAdmin", false); // Por defecto no es admin

                        db.collection("Usuarios").document(user.getUid()).set(nuevoUsuario)
                                .addOnSuccessListener(aVoid -> ObtenerDatosUsuario(user.getUid()));
                    }
                });
    }

    // --- RESTO DE TU LÓGICA (Mantenida) ---

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            ObtenerDatosUsuario(currentUser.getUid());
        }
    }

    @Override
    public void onClick(View v) {
        String c = Correo.getText().toString().trim();
        String p = Password.getText().toString().trim();

        if (v.getId() == R.id.Registrar) {
            startActivity(new Intent(this, Registro.class));
        } else if (v.getId() == R.id.Iniciar) {
            if (!validarCampos(c, p)) return;
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(c).matches()) {
                Correo.setError("Formato no válido");
                return;
            }

            mAuth.signInWithEmailAndPassword(c, p).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        ObtenerDatosUsuario(user.getUid());
                    } else {
                        mostrarPopupVerificacion(user);
                        mAuth.signOut();
                    }
                } else {
                    mostrarPopupRegistro(c);
                }
            });
        }
    }

    private void ObtenerDatosUsuario(String uid){
        db.collection("Usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Usuario usuario = documentSnapshot.toObject(Usuario.class);
                        if (usuario != null) {
                            NotificationPreferences.syncTopicWithStoredSetting(this);
                            NotificationPreferences.refreshAndStoreCurrentToken();
                            Intent intent = new Intent(this, MainMenu.class);
                            intent.putExtra("Es_admin", usuario.isAdmin());
                            startActivity(intent);
                            finish();
                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(Login.this, "Error de datos", Toast.LENGTH_SHORT).show());
    }

    private void mostrarPopupRegistro(String emailOlvidado) {
        new AlertDialog.Builder(this)
                .setTitle("Usuario no encontrado")
                .setMessage("¿Quieres registrar una cuenta?")
                .setPositiveButton("Registrarme", (dialog, which) -> startActivity(new Intent(this, Registro.class)))
                .setNegativeButton("Cancelar", null).show();
    }

    private void mostrarPopupVerificacion(FirebaseUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Cuenta no verificada")
                .setMessage("Revisa tu correo o solicita un nuevo enlace.")
                .setPositiveButton("Reenviar", (dialog, which) -> {
                    if (user != null) user.sendEmailVerification();
                })
                .setNegativeButton("Cerrar", null).show();
    }

    private boolean validarCampos(String c, String p) {
        if (c.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}