package org.example.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * Entite associative entre Commande et Produit.
 * La cle est composee et partagee avec les deux ManyToOne via @MapsId.
 */
@Entity
@Table(name = "commande_produit")
public class CommandeProduit {

    @EmbeddedId
    private CommandeProduitPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    public CommandeProduit() {
    }

    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
        // la cle reprend les identifiants des deux entites
        this.id = new CommandeProduitPK(commande.getId(), produit.getId());
    }

    public CommandeProduitPK getId() {
        return id;
    }

    public void setId(CommandeProduitPK id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    /** Montant de la ligne : prix unitaire multiplie par la quantite. */
    public BigDecimal getTotalLigne() {
        if (produit == null || produit.getPrix() == null) {
            return BigDecimal.ZERO;
        }
        return produit.getPrix().multiply(BigDecimal.valueOf(quantite));
    }

    @Override
    public String toString() {
        return "Ligne{" + produit.getLibelle() + " x" + quantite + "}";
    }
}
