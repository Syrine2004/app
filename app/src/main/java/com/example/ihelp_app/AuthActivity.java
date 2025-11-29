package com.example.ihelp_app; // <<< VÉRIFIEZ LE NOM DE VOTRE PACKAGE

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth); // Utilise activity_auth.xml

        // 1. Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Lier les vues (Widgets)
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.registerLink);

        // 3. Vérifier l'état de connexion au démarrage
        if (mAuth.getCurrentUser() != null) {
            goToMainActivity();
        }

        // 4. Définir les actions de clic
        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> registerUser());
    }

    // --- LOGIQUE sInscrire (Création de compte) ---
    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || password.length() < 6) {
            Toast.makeText(this, "Email invalide ou mot de passe trop court (min 6).", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Compte créé !", Toast.LENGTH_SHORT).show();
                        goToMainActivity(); // Rediriger
                    } else {
                        Toast.makeText(AuthActivity.this, "Échec: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- LOGIQUE seConnecter ---
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                        goToMainActivity(); // Rediriger
                    } else {
                        Toast.makeText(AuthActivity.this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- Navigation ---
    private void goToMainActivity() {
        // Lance l'activité principale
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}