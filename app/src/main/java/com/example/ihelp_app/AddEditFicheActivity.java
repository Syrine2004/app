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

        if (TextUtils.isEmpty(titre) || TextUtils.isEmpty(categorie) || TextUtils.isEmpty(contenu)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        if (ficheId != null) {
            // Mise à jour
            Map<String, Object> fiche = new HashMap<>();
            fiche.put("titre", titre);
            fiche.put("categorie", categorie);
            fiche.put("contenu", contenu);
            // userId ne change pas

            fichesRef.document(ficheId).update(fiche)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddEditFicheActivity.this, "Fiche mise à jour", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast
                            .makeText(AddEditFicheActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT)
                            .show());

        } else {
            // Ajout
            // On laisse Firestore générer l'ID du document, mais on peut aussi le stocker
            // dans l'objet si besoin.
            // Ici, on va créer un objet Map ou utiliser la classe Fiche, mais attention
            // l'ID est généré après.
            // Pour simplifier avec la classe Fiche, on peut générer l'ID avant ou laisser
            // Firestore le faire.
            // Utilisons une Map pour l'ajout simple ou la classe Fiche sans ID, puis on set
            // l'ID ?
            // Le plus simple avec Firestore est d'ajouter et de laisser l'ID auto.

            // Mais notre classe Fiche a un champ ID. C'est mieux de le remplir.
            String id = fichesRef.document().getId();
            Fiche fiche = new Fiche(id, titre, categorie, contenu, userId);

            fichesRef.document(id).set(fiche)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddEditFicheActivity.this, "Fiche ajoutée", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast
                            .makeText(AddEditFicheActivity.this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show());
        }
    }
}
