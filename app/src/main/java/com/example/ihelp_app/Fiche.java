package com.example.ihelp_app;

public class Fiche {
    private String id;
    private String titre;
    private String categorie;
    private String contenu;
    private String userId;

    public Fiche() {
        // Constructeur vide requis pour Firestore
    }

    public Fiche(String id, String titre, String categorie, String contenu, String userId) {
        this.id = id;
        this.titre = titre;
        this.categorie = categorie;
        this.contenu = contenu;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
