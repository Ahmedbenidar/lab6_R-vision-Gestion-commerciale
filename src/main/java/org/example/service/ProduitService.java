package org.example.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.dao.FetchDAO;
import org.example.entities.Produit;

/**
 * Service dedie au produit : il ajoute les requetes metier
 * aux operations CRUD heritees.
 */
public class ProduitService extends CrudService<Produit, Long> {

    private final FetchDAO<Produit, Long> fetchDao = new FetchDAO<Produit, Long>(Produit.class);

    public ProduitService() {
        super(Produit.class);
    }

    /** Produits tries par libelle, categorie chargee dans la meme requete. */
    public List<Produit> findAllOrdonne() {
        return fetchDao.findAllAvecRelation("categorie", "libelle");
    }

    public Produit getByIdAvecCategorie(Long id) {
        return fetchDao.findByIdAvecRelation(id, "categorie");
    }

    /** Utilise la Named Query declaree sur l'entite Produit. */
    public List<Produit> findByCategorie(Long categorieId) {
        Map<String, Object> parametres = new HashMap<String, Object>();
        parametres.put("catId", categorieId);
        return dao().findByNamedQuery("Produit.findByCategorie", parametres);
    }

    public List<Produit> searchByLibelle(String terme) {
        Map<String, Object> parametres = new HashMap<String, Object>();
        parametres.put("term", terme);
        return dao().findByNamedQuery("Produit.searchByLibelle", parametres);
    }

    /** Recherche multi-criteres avec pagination (Criteria API). */
    public List<Produit> search(String terme, BigDecimal min, BigDecimal max,
                                int numeroPage, int taillePage) {
        return dao().search(terme, min, max, numeroPage, taillePage);
    }

    public List<Produit> findByPrixBetween(BigDecimal min, BigDecimal max) {
        return dao().findByPrixBetween(min, max);
    }

    /** Pagination JPQL avec JOIN FETCH sur la categorie. */
    public List<Produit> getPageAvecCategorie(int numeroPage, int taillePage) {
        String jpql = "SELECT p FROM Produit p JOIN FETCH p.categorie ORDER BY p.id";
        List<Produit> page = fetchDao.requete(jpql, null,
                (numeroPage - 1) * taillePage, taillePage);
        return page == null ? new ArrayList<Produit>() : page;
    }

    /**
     * Meme recherche multi-criteres que search(), mais la categorie
     * est ramenee dans la requete pour pouvoir afficher son nom.
     */
    public List<Produit> searchAvecCategorie(String terme, BigDecimal min, BigDecimal max,
                                             int numeroPage, int taillePage) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT p FROM Produit p JOIN FETCH p.categorie");
        jpql.append(" WHERE p.prix BETWEEN :min AND :max");

        Map<String, Object> parametres = new HashMap<String, Object>();
        parametres.put("min", min);
        parametres.put("max", max);

        if (terme != null && terme.trim().length() > 0) {
            jpql.append(" AND lower(p.libelle) LIKE :motif");
            parametres.put("motif", "%" + terme.toLowerCase() + "%");
        }
        jpql.append(" ORDER BY p.id");

        List<Produit> page = fetchDao.requete(jpql.toString(), parametres,
                (numeroPage - 1) * taillePage, taillePage);
        return page == null ? new ArrayList<Produit>() : page;
    }
}

