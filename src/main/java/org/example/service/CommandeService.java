package org.example.service;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.example.entities.Commande;
import org.example.entities.Produit;
import org.example.util.HibernateUtil;

/**
 * Service des commandes.
 * La creation se fait dans une seule session : les produits restent
 * attaches pendant l'enregistrement, on evite ainsi les entites detachees.
 */
public class CommandeService extends CrudService<Commande, Long> {

    public CommandeService() {
        super(Commande.class);
    }

    /**
     * Cree une commande et ses lignes dans une seule transaction.
     *
     * @param date        date de la commande
     * @param idsProduits identifiants des produits commandes
     * @param quantites   quantite commandee pour chaque produit
     */
    public Commande enregistrer(LocalDate date, List<Long> idsProduits, List<Integer> quantites) {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Commande commande = new Commande(date);
            em.persist(commande);
            em.flush();   // ici l'id est genere, la cle composee peut le reprendre

            for (int i = 0; i < idsProduits.size(); i++) {
                // reference managee : pas de chargement inutile et pas de detache
                Produit produit = em.getReference(Produit.class, idsProduits.get(i));
                commande.addProduit(produit, quantites.get(i));
            }

            tx.commit();
            return commande;
        } catch (RuntimeException erreur) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw erreur;
        } finally {
            em.close();
        }
    }
}
