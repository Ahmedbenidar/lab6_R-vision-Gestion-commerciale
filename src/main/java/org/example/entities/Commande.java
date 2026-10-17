package org.example.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_commande")
    private LocalDate dateCommande;

    // les lignes ne vivent pas sans leur commande
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeProduit> lignes = new ArrayList<CommandeProduit>();

    public Commande() {
    }

    public Commande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public List<CommandeProduit> getLignes() {
        return lignes;
    }

    /** Ajoute une ligne et met a jour les deux cotes de la relation. */
    public void addProduit(Produit produit, int quantite) {
        CommandeProduit ligne = new CommandeProduit(this, produit, quantite);
        lignes.add(ligne);
    }

    public void removeProduit(Produit produit) {
        for (int i = 0; i < lignes.size(); i++) {
            CommandeProduit ligne = lignes.get(i);
            if (ligne.getProduit().getId().equals(produit.getId())) {
                lignes.remove(i);
                ligne.setCommande(null);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Commande{id=" + id + ", date=" + dateCommande + ", lignes=" + lignes.size() + "}";
    }
}
