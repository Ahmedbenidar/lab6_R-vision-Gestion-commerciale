package org.example.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "produits")
@NamedQueries({
        @NamedQuery(name = "Produit.findAll", query = "SELECT p FROM Produit p"),
        @NamedQuery(name = "Produit.findByCategorie",
                query = "SELECT p FROM Produit p WHERE p.categorie.id = :catId"),
        @NamedQuery(name = "Produit.searchByLibelle",
                query = "SELECT p FROM Produit p WHERE LOWER(p.libelle) LIKE LOWER(CONCAT('%', :term, '%'))")
})
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    // cote proprietaire : la colonne categorie_id est creee ici
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    public Produit() {
    }

    public Produit(String libelle, BigDecimal prix, Categorie categorie) {
        this.libelle = libelle;
        this.prix = prix;
        this.categorie = categorie;
        this.dateCreation = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "Produit{id=" + id + ", libelle='" + libelle + "', prix=" + prix + "}";
    }
}
