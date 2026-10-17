package org.example.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "fournisseurs")
public class Fournisseur extends User {

    @Column(name = "adresse")
    private String adresse;

    public Fournisseur() {
    }

    public Fournisseur(String nom, String adresse) {
        super(nom);
        this.adresse = adresse;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return "Fournisseur{nom='" + getNom() + "', adresse='" + adresse + "'}";
    }
}
