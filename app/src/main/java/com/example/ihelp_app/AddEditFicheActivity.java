package com.example.ihelp_app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditFicheActivity extends AppCompatActivity {

    private EditText editTextTitre;
    private EditText editTextCategorie;
    private EditText editTextContenu;
    private Button buttonSave;

    private FirebaseFirestore db;
    private CollectionReference fichesRef;
    private FirebaseAuth mAuth;

    private String ficheId = null; // Null si ajout, ID si modification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_fiche);

        editTextTitre = findViewById(R.id.editTextTitre);
        editTextCategorie = findViewById(R.id.editTextCategorie);
        editTextContenu = findViewById(R.id.editTextContenu);
        buttonSave = findViewById(R.id.buttonSave);

        db = FirebaseFirestore.getInstance();
        fichesRef = db.collection("fiches");
        mAuth = FirebaseAuth.getInstance();

        // Vérifier si on est en mode modification
        if (getIntent().hasExtra("id")) {
            ficheId = getIntent().getStringExtra("id");
            editTextTitre.setText(getIntent().getStringExtra("titre"));
            editTextCategorie.setText(getIntent().getStringExtra("categorie"));
            editTextContenu.setText(getIntent().getStringExtra("contenu"));
            buttonSave.setText("Mettre à jour");
        } else {
            buttonSave.setText("Sauvegarder");
        }

        buttonSave.setOnClickListener(v -> saveFiche());
    }

    private void saveFiche() {
        String titre = editTextTitre.getText().toString().trim();
        String categorie = editTextCategorie.getText().toString().trim();
        String contenu = editTextContenu.getText().toString().trim();

        // NOUVEAU: Préparer la version en minuscules pour la recherche
        String titreLower = titre.toLowerCase();

        if (TextUtils.isEmpty(titre) || TextUtils.isEmpty(categorie) || TextUtils.isEmpty(contenu)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assurez-vous que l'utilisateur est connecté avant d'appeler getUid()
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Erreur: Utilisateur non authentifié.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        if (ficheId != null) {
            // Mise à jour
            Map<String, Object> fiche = new HashMap<>();
            fiche.put("titre", titre);
            fiche.put("categorie", categorie);
            fiche.put("contenu", contenu);
            // AJOUT DU CHAMP DE RECHERCHE EN MINUSCULES
            fiche.put("titreLower", titreLower);

            fichesRef.document(ficheId).update(fiche)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddEditFicheActivity.this, "Fiche mise à jour", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast
                            .makeText(AddEditFicheActivity.this, "Erreur lors de la mise à jour: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show());

        } else {
            // Ajout
            String id = fichesRef.document().getId();
            // Utiliser la classe Fiche qui gère titreLower automatiquement
            Fiche fiche = new Fiche(id, titre, categorie, contenu, userId);

            fichesRef.document(id).set(fiche)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddEditFicheActivity.this, "Fiche ajoutée", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast
                            .makeText(AddEditFicheActivity.this, "Erreur lors de l'ajout: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}