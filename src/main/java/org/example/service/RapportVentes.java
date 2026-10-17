package org.example.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.example.util.HibernateUtil;

/**
 * Exercice de synthese (etape 12) : rapport des ventes.
 * Pour chaque categorie on calcule le chiffre d'affaires
 * (somme de prix x quantite) des commandes passees dans un intervalle de dates.
 */
public class RapportVentes {

    private static final EntityManagerFactory EMF = HibernateUtil.getSessionFactory();

    /**
     * @param debut date de debut (incluse)
     * @param fin   date de fin (incluse)
     * @return une map "nom de categorie" -> chiffre d'affaires
     */
    public Map<String, BigDecimal> chiffreAffairesParCategorie(java.time.LocalDate debut,
                                                              java.time.LocalDate fin) {
        EntityManager em = EMF.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<org.example.entities.CommandeProduit> ligne =
                    cq.from(org.example.entities.CommandeProduit.class);

            Join<Object, Object> commande = ligne.join("commande");
            Join<Object, Object> produit = ligne.join("produit");
            Join<Object, Object> categorie = produit.join("categorie");
            Path<BigDecimal> prix = produit.get("prix");
            Path<Integer> quantite = ligne.get("quantite");

            // somme(prix * quantite) groupee par categorie
            cq.multiselect(categorie.get("nom"), cb.sum(cb.prod(prix, quantite)));
            cq.where(cb.between(commande.get("dateCommande"), debut, fin));
            cq.groupBy(categorie.get("nom"));
            cq.orderBy(cb.asc(categorie.get("nom")));

            List<Object[]> resultats = em.createQuery(cq).getResultList();

            Map<String, BigDecimal> rapport = new LinkedHashMap<String, BigDecimal>();
            for (Object[] ligneResultat : resultats) {
                rapport.put((String) ligneResultat[0], (BigDecimal) ligneResultat[1]);
            }
            return rapport;
        } finally {
            em.close();
        }
    }
}
