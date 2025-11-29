package com.example.ihelp_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
        fichesRef = db.collection("fiches");

        // Check auth
        if (mAuth.getCurrentUser() == null) {
            goToAuthActivity();
            return;
        }

        recyclerViewFiches = findViewById(R.id.recyclerViewFiches);
        fabAddFiche = findViewById(R.id.fabAddFiche);
        searchView = findViewById(R.id.searchView);

        setUpRecyclerView();

        fabAddFiche.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditFicheActivity.class);
            startActivity(intent);
        });

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });
    }

    private void setUpRecyclerView() {
        String userId = mAuth.getCurrentUser().getUid();
        Query query = fichesRef.whereEqualTo("userId", userId).orderBy("titre", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Fiche> options = new FirestoreRecyclerOptions.Builder<Fiche>()
                .setQuery(query, Fiche.class)
                .build();

        adapter = new FicheAdapter(options);

        recyclerViewFiches.setHasFixedSize(true);
        recyclerViewFiches.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFiches.setAdapter(adapter);

        // Item Click (Edit)
        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            Fiche fiche = documentSnapshot.toObject(Fiche.class);
            String id = documentSnapshot.getId();

            Intent intent = new Intent(MainActivity.this, AddEditFicheActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("titre", fiche.getTitre());
            intent.putExtra("categorie", fiche.getCategorie());
            intent.putExtra("contenu", fiche.getContenu());
            startActivity(intent);
        });

        // Swipe to Delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getBindingAdapterPosition());
                Toast.makeText(MainActivity.this, "Fiche supprimée", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerViewFiches);
    }

    private void firebaseSearch(String searchText) {
        String userId = mAuth.getCurrentUser().getUid();
        Query query;

        if (searchText.isEmpty()) {
            query = fichesRef.whereEqualTo("userId", userId).orderBy("titre", Query.Direction.ASCENDING);
        } else {
            // Note: Firestore doesn't support native full-text search or "contains".
            // We use startAt/endAt for prefix search on 'titre'.
            // For a better search, we would need Algolia or similar.
            // Here we assume the user searches by Title prefix.
            // Also, Firestore requires composite indexes for multiple fields.
            // We will try to filter by Title.

            // Capitalize first letter to match our inputType="textCapSentences" if needed,
            // but let's just pass the text.

            query = fichesRef.whereEqualTo("userId", userId)
                    .orderBy("titre")
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff");
        }

        FirestoreRecyclerOptions<Fiche> options = new FirestoreRecyclerOptions.Builder<Fiche>()
                .setQuery(query, Fiche.class)
                .build();

        adapter.updateOptions(options);
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            goToAuthActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}