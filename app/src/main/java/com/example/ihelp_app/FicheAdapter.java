package com.example.ihelp_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FicheAdapter extends FirestoreRecyclerAdapter<Fiche, FicheAdapter.FicheViewHolder> {

    private OnItemClickListener listener;

    public FicheAdapter(@NonNull FirestoreRecyclerOptions<Fiche> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FicheViewHolder holder, int position, @NonNull Fiche model) {
        holder.textViewTitre.setText(model.getTitre());
        holder.textViewCategorie.setText(model.getCategorie());
    }

    @NonNull
    @Override
    public FicheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fiche, parent, false);
        return new FicheViewHolder(v);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class FicheViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitre;
        TextView textViewCategorie;

        public FicheViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitre = itemView.findViewById(R.id.textViewTitre);
            textViewCategorie = itemView.findViewById(R.id.textViewCategorie);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(com.google.firebase.firestore.DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
