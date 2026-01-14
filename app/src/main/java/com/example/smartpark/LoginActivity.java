package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    EditText editTextCorreo, editTextContrasena;
    Button btnLogin;
    FirebaseAuth mAuth;
    TextView textView;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), HomePage.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        editTextCorreo = findViewById(R.id.loginCorreo);
        editTextContrasena = findViewById(R.id.loginContrasena);
        btnLogin = findViewById(R.id.btnLogin);
        textView = findViewById(R.id.gotoRegister);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        textView = findViewById(R.id.contrasenaOlvidada);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CambiarContrasenyaPrimero.class);
                startActivity(intent);
                finish();
            }
        });

        Button volverBtn = findViewById(R.id.volver3);
        volverBtn.setOnClickListener(view -> {
            finish();
        });

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ImageButton googleLoginBtn = findViewById(R.id.googleLogin);
        googleLoginBtn.setOnClickListener(v -> signInWithGoogle());


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo, contrasena;
                correo = editTextCorreo.getText().toString();
                contrasena = editTextContrasena.getText().toString();

                if(TextUtils.isEmpty(correo)) {
                    Toast.makeText(LoginActivity.this, "Introduce tu correo electrónico", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(contrasena)) {
                    Toast.makeText(LoginActivity.this, "Introduce la contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Sesión iniciada.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Autenticación Fallida.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection("usuarios").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        if (!doc.exists()) {
                                            Map<String, Object> nuevoUsuario = new HashMap<>();
                                            nuevoUsuario.put("nombre", user.getDisplayName());
                                            nuevoUsuario.put("correo", user.getEmail());
                                            nuevoUsuario.put("fotoPerfil",
                                                    user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                                            nuevoUsuario.put("uid", user.getUid());
                                            nuevoUsuario.put("rfid", ""); // 👈 campo vacío al registrarse

                                            db.collection("usuarios")
                                                    .document(user.getUid())
                                                    .set(nuevoUsuario)
                                                    .addOnSuccessListener(aVoid ->
                                                            Toast.makeText(this, "Cuenta creada con Google", Toast.LENGTH_SHORT).show()
                                                    )
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(this, "Error guardando datos en Firestore", Toast.LENGTH_SHORT).show()
                                                    );
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error comprobando Firestore", Toast.LENGTH_SHORT).show()
                                    );
                        }

                        Toast.makeText(this, "Bienvenido, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomePage.class));
                        finish();

                    } else {
                        Toast.makeText(this, "Error en la autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}