package com.example.ihelp_app;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WrapContentLinearLayoutManager extends LinearLayoutManager {

    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    // Cette méthode capture l'exception "Inconsistency detected" qui peut survenir
    // lors de mises à jour rapides des données (comme lors de la frappe dans la
    // recherche).
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("WrapContentLinearLayout", "Inconsistency detected in RecyclerView", e);
        }
    }
}
