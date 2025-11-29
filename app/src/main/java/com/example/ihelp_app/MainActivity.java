package com.example.ihelp_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
// Importation nécessaire pour l'AlertDialog
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Importation correcte pour le composant AppCompat
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    // La collection de base pour les fiches dans Firebase
    private CollectionReference fichesRef;

    private FicheAdapter adapter;
    private RecyclerView recyclerViewFiches;
    private FloatingActionButton fabAddFiche;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Le chemin de collection "fiches"
        fichesRef = db.collection("fiches");

        // 1. VÉRIFICATION D'AUTHENTIFICATION & REDIRECTION
        if (mAuth.getCurrentUser() == null) {
            goToAuthActivity();
            // Ne pas continuer l'exécution si non authentifié
            return;
        }

        recyclerViewFiches = findViewById(R.id.recyclerViewFiches);
        fabAddFiche = findViewById(R.id.fabAddFiche);
        // Initialisation correcte du SearchView depuis le layout
        searchView = findViewById(R.id.searchView);

        // Initialisation et affichage de la liste de l'utilisateur
        setUpRecyclerView(null); // Affiche toutes les fiches au démarrage

        fabAddFiche.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditFicheActivity.class);
            startActivity(intent);
        });

        // GESTION DU BOUTON DE DÉCONNEXION (Custom Header)
        android.widget.ImageView imageViewLogout = findViewById(R.id.imageViewLogout);
        imageViewLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Déconnexion de Firebase
            Toast.makeText(MainActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
            goToAuthActivity(); // Redirige vers l'écran d'authentification
        });

        // 2. FONCTIONNALITÉ DE RECHERCHE/FILTRAGE
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setUpRecyclerView(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtrage en temps réel
                setUpRecyclerView(newText);
                return false;
            }
        });
    }

    // NOUVELLE MÉTHODE : Affiche la boîte de dialogue de confirmation avant la
    // suppression
    private void showDeleteConfirmationDialog(final int position) {
        // Crée la boîte de dialogue
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette fiche ?")
                // Bouton "Oui" (Supprimer)
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Si l'utilisateur confirme, nous procédons à la suppression réelle
                    adapter.deleteItem(position);
                    Toast.makeText(MainActivity.this, "Fiche supprimée", Toast.LENGTH_SHORT).show();
                })
                // Bouton "Non" (Annuler)
                .setNegativeButton("Non", (dialog, which) -> {
                    // Si l'utilisateur annule, nous notifions l'adaptateur pour redessiner
                    // la fiche qui a été glissée mais pas supprimée.
                    adapter.notifyItemChanged(position);
                    dialog.dismiss();
                })
                .show();
    }

    // Méthode pour configurer/mettre à jour la RecyclerView
    private void setUpRecyclerView(String searchText) {
        // Vérification de l'utilisateur pour éviter un crash si l'utilisateur est null
        // (même si la vérif est faite dans onCreate)
        if (mAuth.getCurrentUser() == null)
            return;

        String userId = mAuth.getCurrentUser().getUid();
        Query query;

        if (searchText == null || searchText.isEmpty()) {
            // Requête par défaut: toutes les fiches de l'utilisateur, triées par le titre
            // original
            query = fichesRef.whereEqualTo("userId", userId).orderBy("titre", Query.Direction.ASCENDING);
        } else {
            // Requête de recherche insensible à la casse sur le NOUVEAU champ 'titreLower'.
            String searchLower = searchText.toLowerCase();

            // Cette requête NÉCESSITE un index composite sur Firestore : (userId ASC,
            // titreLower ASC)
            // L'index sur (userId ASC, titre ASC) n'est PLUS suffisant.
            query = fichesRef.whereEqualTo("userId", userId)
                    .orderBy("titreLower") // Changement ici: utiliser titreLower
                    .startAt(searchLower)
                    .endAt(searchLower + "\uf8ff");
        }

        FirestoreRecyclerOptions<Fiche> options = new FirestoreRecyclerOptions.Builder<Fiche>()
                .setQuery(query, Fiche.class)
                .build();

        // Mettre à jour l'adaptateur si c'est déjà initialisé (changement de recherche)
        // Mettre à jour l'adaptateur si c'est déjà initialisé (changement de recherche)
        if (adapter != null) {
            // OPTIMISATION: Ne pas arrêter/démarrer l'écoute pour une simple mise à jour
            // des options.
            // updateOptions applique les changements immédiatement si l'adaptateur écoute
            // déjà.
            adapter.updateOptions(options);
        } else {
            // Initialisation de l'adaptateur (premier chargement)
            adapter = new FicheAdapter(options);

            recyclerViewFiches.setHasFixedSize(true);
            // FIX CRASH: Utilisation de WrapContentLinearLayoutManager pour éviter
            // "Inconsistency detected"
            recyclerViewFiches.setLayoutManager(new WrapContentLinearLayoutManager(this));
            recyclerViewFiches.setAdapter(adapter);

            // GESTION DES ERREURS (Index manquant, etc.)
            adapter.setOnDataChangedListener(new FicheAdapter.OnDataChangedListener() {
                @Override
                public void onDataChanged() {
                    // Liste vide ou chargée avec succès
                }

                @Override
                public void onError(com.google.firebase.firestore.FirebaseFirestoreException e) {
                    // Affiche l'erreur (souvent un lien pour créer l'index dans Logcat)
                    Toast.makeText(MainActivity.this, "Erreur de recherche: " + e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                    e.printStackTrace(); // Important pour voir le lien de création d'index dans Logcat
                }
            });

            // GESTION DE L'ÉDITION AU CLIC
            adapter.setOnItemClickListener((documentSnapshot, position) -> {
                Fiche fiche = documentSnapshot.toObject(Fiche.class);
                // L'ID du document est nécessaire pour la mise à jour/suppression
                String id = documentSnapshot.getId();

                Intent intent = new Intent(MainActivity.this, AddEditFicheActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("titre", fiche.getTitre());
                intent.putExtra("categorie", fiche.getCategorie());
                intent.putExtra("contenu", fiche.getContenu());
                startActivity(intent);
            });

            // GESTION DE LA SUPPRESSION AU SWIPE (SWIPE-TO-DELETE)
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                        @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    // Au lieu de supprimer directement, on affiche la boîte de dialogue de
                    // confirmation
                    int position = viewHolder.getBindingAdapterPosition();
                    showDeleteConfirmationDialog(position);
                }
            }).attachToRecyclerView(recyclerViewFiches);

            // Lancer l'écoute de Firestore après l'initialisation de l'adaptateur
            adapter.startListening();
        }

        // Afficher une instruction pour le "swipe-to-delete"
        if (adapter != null && adapter.getItemCount() > 0 && (searchText == null || searchText.isEmpty())) {
            Toast.makeText(MainActivity.this, "Balayez à gauche/droite pour supprimer une fiche.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    // Gestion du cycle de vie de l'adaptateur Firestore
    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    // Redirection vers l'activité d'authentification
    private void goToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        // Empêche l'utilisateur de revenir à MainActivity avec le bouton "Retour"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}