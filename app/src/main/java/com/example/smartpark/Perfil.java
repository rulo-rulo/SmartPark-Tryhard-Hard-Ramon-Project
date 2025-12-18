package com.example.smartpark;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Perfil extends AppCompatActivity {

    private TextView nombre, correo;
    private ImageView fotoPerfil;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imagenSeleccionadaUri;

    private ActivityResultLauncher<String> permisoGaleriaLauncher;
    private ActivityResultLauncher<String> selectorImagenLauncher;

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);

        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.correo);
        fotoPerfil = findViewById(R.id.fotoPerfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            cargarDatosUsuario(user.getUid());
        }

        // Registrar el launcher para pedir permiso de almacenamiento
        permisoGaleriaLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        abrirGaleria();
                    } else {
                        Toast.makeText(this, "Permiso denegado para acceder a la galería", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Registrar launcher para seleccionar una imagen
        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imagenSeleccionadaUri = uri;
                        subirImagenAFirebase(uri);
                    }
                }
        );

        fotoPerfil.setOnClickListener(v -> mostrarDialogoFoto());

        configurarBotones();
    }

    private void cargarDatosUsuario(String uid) {
        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreUsuario = documentSnapshot.getString("nombre");
                        String correoUsuario = documentSnapshot.getString("correo");
                        String fotoUrl = documentSnapshot.getString("fotoPerfil");

                        nombre.setText(nombreUsuario != null ? nombreUsuario : "Sin nombre");
                        correo.setText(correoUsuario != null ? correoUsuario : "Sin correo");

                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.foto_perfil) // opcional, imagen por defecto
                                    .into(fotoPerfil);
                        }
                    } else {
                        nombre.setText("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarDialogoFoto() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View vista = getLayoutInflater().inflate(R.layout.dialog_foto_perfil, null);
        builder.setView(vista);
        android.app.AlertDialog dialog = builder.create();

        ImageView fotoGrande = vista.findViewById(R.id.fotoGrande);
        Button btnCambiar = vista.findViewById(R.id.btnCambiarFoto);

        // Cargamos la foto actual del usuario en el diálogo
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("usuarios").document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String url = doc.getString("fotoPerfil");
                            if (url != null && !url.isEmpty()) {
                                Glide.with(this)
                                        .load(url)
                                        .circleCrop()
                                        .placeholder(R.drawable.foto_perfil)
                                        .into(fotoGrande);
                            } else {
                                fotoGrande.setImageResource(R.drawable.foto_perfil);
                            }
                        }
                    });
        }

        btnCambiar.setOnClickListener(v -> {
            dialog.dismiss();
            comprobarPermisoYSeleccionar();
        });

        dialog.show();
    }

    private void comprobarPermisoYSeleccionar() {
        String permiso = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            permisoGaleriaLauncher.launch(permiso);
        }
    }

    private void abrirGaleria() {
        selectorImagenLauncher.launch("image/*");
    }

    private void subirImagenAFirebase(Uri uri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        StorageReference ref = storage.getReference().child("foto_perfil/" + uid + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();

                            // 🔹 Guardar URL en Firestore
                            db.collection("usuarios").document(uid)
                                    .update("fotoPerfil", url)
                                    .addOnSuccessListener(aVoid -> {
                                        // 🔸 Aquí reemplazamos la línea por Glide circular
                                        Glide.with(this)
                                                .load(url)
                                                .circleCrop()
                                                .placeholder(R.drawable.foto_perfil)
                                                .into(fotoPerfil);

                                        Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error guardando URL", Toast.LENGTH_SHORT).show()
                                    );
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                );
    }

    private void configurarBotones() {
        Button volverBtn = findViewById(R.id.volver2);
        volverBtn.setOnClickListener(view -> finish());

        Button cambiarNombreBtn = findViewById(R.id.cambiarNombre);
        cambiarNombreBtn.setOnClickListener(view ->
                startActivity(new Intent(this, CambiarNombre.class))
        );

        Button cambiarCorreoBtn = findViewById(R.id.cambiarCorreo);
        cambiarCorreoBtn.setOnClickListener(view ->
                startActivity(new Intent(this, CambiarCorreo.class))
        );

        Button cambiarContrasenaBtn = findViewById(R.id.cambiarContrasena);
        cambiarContrasenaBtn.setOnClickListener(view ->
                startActivity(new Intent(this, CambiarContrasenyaPrimero.class))
        );

        LinearLayout homeTab = findViewById(R.id.home);
        homeTab.setOnClickListener(view ->
                startActivity(new Intent(this, HomePage.class))
        );

        LinearLayout mapasTab = findViewById(R.id.mapas);
        mapasTab.setOnClickListener(view ->
                startActivity(new Intent(this, Mapa.class))
        );

        Button misReservasBtn = findViewById(R.id.misReservas);
        misReservasBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ListaReservas.class))
        );

        Button cerrarSesionBtn = findViewById(R.id.cerrarSesion);
        cerrarSesionBtn.setOnClickListener(view -> {

            FirebaseAuth.getInstance().signOut();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.signOut().addOnCompleteListener(task -> {

                Intent intent = new Intent(Perfil.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

    }

    public void lanzarAcercaDe(android.view.View view){
        startActivity(new Intent(this, AcercaDeActivity.class));
    }

    public void lanzarPreguntas(android.view.View view){
        startActivity(new Intent(this, PreguntasFrecuentesActivity.class));
    }
}
