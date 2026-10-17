package org.example.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Cle primaire composee de la table commande_produit.
 * Une ligne est identifiee par le couple (commande, produit).
 */
@Embeddable
public class CommandeProduitPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "commande_id")
    private Long commandeId;

    @Column(name = "produit_id")
    private Long produitId;

    public CommandeProduitPK() {
    }

    public CommandeProduitPK(Long commandeId, Long produitId) {
        this.commandeId = commandeId;
        this.produitId = produitId;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long commandeId) {
        this.commandeId = commandeId;
    }

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    // obligatoire pour une cle composee
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof CommandeProduitPK)) {
            return false;
        }
        CommandeProduitPK autreCle = (CommandeProduitPK) autre;
        return Objects.equals(commandeId, autreCle.commandeId)
                && Objects.equals(produitId, autreCle.produitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeId, produitId);
    }
}
