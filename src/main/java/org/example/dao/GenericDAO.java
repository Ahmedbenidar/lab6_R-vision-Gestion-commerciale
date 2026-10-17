package org.example.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.example.util.HibernateUtil;

/**
 * DAO generique base sur JPA.
 *
 * La SessionFactory de HibernateUtil est deja un EntityManagerFactory,
 * on la reutilise donc au lieu d'en creer une deuxieme a chaque appel.
 */
public class GenericDAO<T, ID extends Serializable> {

    private final Class<T> entityClass;

    private static final EntityManagerFactory EMF = HibernateUtil.getSessionFactory();

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    // ------------------------------------------------------------------ lecture

    public T find(ID id) {
        EntityManager em = EMF.createEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }

    public List<T> findAll(String ordreJpql) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e." + ordreJpql;
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }

    public Long count() {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    /** Pagination : on ne charge qu'une tranche du resultat. */
    public List<T> findPage(int numeroPage, int taillePage) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id";
            return em.createQuery(jpql, entityClass)
                    .setFirstResult((numeroPage - 1) * taillePage)
                    .setMaxResults(taillePage)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Named Query avec des parametres nommes passes dans une Map. */
    public List<T> findByNamedQuery(String nomRequete, Map<String, Object> parametres) {
        EntityManager em = EMF.createEntityManager();
        try {
            Query requete = em.createNamedQuery(nomRequete);
            if (parametres != null) {
                for (Map.Entry<String, Object> entree : parametres.entrySet()) {
                    requete.setParameter(entree.getKey(), entree.getValue());
                }
            }
            return requete.getResultList();
        } finally {
            em.close();
        }
    }

    /** Tous les enregistrements d'un sous-type (utile avec la strategie JOINED). */
    public List<T> findByType(Class<? extends T> sousType) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE TYPE(e) = :type";
            return em.createQuery(jpql, entityClass)
                    .setParameter("type", sousType)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Recherche par intervalle de prix (Criteria API, lecture seule). */
    public List<T> findByPrixBetween(java.math.BigDecimal min, java.math.BigDecimal max) {
        EntityManager em = EMF.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> racine = cq.from(entityClass);
            List<Predicate> filtres = new ArrayList<Predicate>();
            if (min != null) {
                filtres.add(cb.ge(racine.get("prix"), min));
            }
            if (max != null) {
                filtres.add(cb.le(racine.get("prix"), max));
            }
            cq.where(filtres.toArray(new Predicate[filtres.size()]));
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    /** Recherche paginee par libelle + intervalle de prix (Criteria API). */
    public List<T> search(String terme, java.math.BigDecimal min, java.math.BigDecimal max,
                          int numeroPage, int taillePage) {
        EntityManager em = EMF.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> racine = cq.from(entityClass);

            List<Predicate> filtres = new ArrayList<Predicate>();
            if (terme != null && !terme.isEmpty()) {
                filtres.add(cb.like(cb.lower(racine.<String>get("libelle")),
                        "%" + terme.toLowerCase() + "%"));
            }
            if (min != null) {
                filtres.add(cb.ge(racine.get("prix"), min));
            }
            if (max != null) {
                filtres.add(cb.le(racine.get("prix"), max));
            }
            cq.where(filtres.toArray(new Predicate[filtres.size()]));
            cq.orderBy(cb.asc(racine.get("id")));

            return em.createQuery(cq)
                    .setFirstResult((numeroPage - 1) * taillePage)
                    .setMaxResults(taillePage)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // ------------------------------------------------------------------ ecriture

    public void create(T entity) {
        EntityManager em = EMF.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (RuntimeException erreur) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw erreur;
        } finally {
            em.close();
        }
    }

    public T update(T entity) {
        EntityManager em = EMF.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T fusionne = em.merge(entity);
            tx.commit();
            return fusionne;
        } catch (RuntimeException erreur) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw erreur;
        } finally {
            em.close();
        }
    }

    public void delete(T entity) {
        EntityManager em = EMF.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T rattache = em.contains(entity) ? entity : em.merge(entity);
            em.remove(rattache);
            tx.commit();
        } catch (RuntimeException erreur) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw erreur;
        } finally {
            em.close();
        }
    }

    public void deleteById(ID id) {
        T entite = find(id);
        if (entite != null) {
            delete(entite);
        }
    }
}
