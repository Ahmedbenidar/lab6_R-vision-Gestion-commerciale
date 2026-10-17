package org.example.dao;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.example.util.HibernateUtil;

/**
 * DAO de lecture pour les relations LAZY.
 * On utilise JOIN FETCH pour charger la relation dans la meme requete
 * et ainsi eviter les LazyInitializationException hors session.
 */
public class FetchDAO<T, ID extends Serializable> {

    private final Class<T> entityClass;

    private static final EntityManagerFactory EMF = HibernateUtil.getSessionFactory();

    public FetchDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /** Charge une entite en initialisant la collection indiquee (ex. "lignes"). */
    public T findByIdAvecCollection(ID id, String nomCollection) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT DISTINCT e FROM " + entityClass.getSimpleName()
                    + " e LEFT JOIN FETCH e." + nomCollection + " WHERE e.id = :id";
            List<T> resultats = em.createQuery(jpql, entityClass)
                    .setParameter("id", id)
                    .getResultList();
            return resultats.isEmpty() ? null : resultats.get(0);
        } finally {
            em.close();
        }
    }

    /** Charge une entite en initialisant la relation simple (ex. "categorie"). */
    public T findByIdAvecRelation(ID id, String nomRelation) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName()
                    + " e JOIN FETCH e." + nomRelation + " WHERE e.id = :id";
            List<T> resultats = em.createQuery(jpql, entityClass)
                    .setParameter("id", id)
                    .getResultList();
            return resultats.isEmpty() ? null : resultats.get(0);
        } finally {
            em.close();
        }
    }

    /** Toutes les entites avec la relation simple initialisee. */
    public List<T> findAllAvecRelation(String nomRelation, String ordre) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName()
                    + " e JOIN FETCH e." + nomRelation + " ORDER BY e." + ordre;
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }

    /** Commande complete : lignes + produit de chaque ligne. */
    public T findCommandeComplete(ID id) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT DISTINCT c FROM Commande c"
                    + " LEFT JOIN FETCH c.lignes l"
                    + " LEFT JOIN FETCH l.produit"
                    + " WHERE c.id = :id";
            List<T> resultats = em.createQuery(jpql, entityClass)
                    .setParameter("id", id)
                    .getResultList();
            return resultats.isEmpty() ? null : resultats.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Execute une requete JPQL contenant deja ses JOIN FETCH.
     *
     * @param first index du premier resultat (null = depuis le debut)
     * @param max   nombre maximum de resultats (null = tout)
     */
    public List<T> requete(String jpql, java.util.Map<String, Object> parametres,
                           Integer first, Integer max) {
        EntityManager em = EMF.createEntityManager();
        try {
            javax.persistence.Query query = em.createQuery(jpql, entityClass);
            if (parametres != null) {
                for (java.util.Map.Entry<String, Object> entree : parametres.entrySet()) {
                    query.setParameter(entree.getKey(), entree.getValue());
                }
            }
            if (first != null) {
                query.setFirstResult(first);
            }
            if (max != null) {
                query.setMaxResults(max);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
