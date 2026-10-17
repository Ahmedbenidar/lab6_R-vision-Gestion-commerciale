package org.example.service;

import java.util.List;
import org.example.dao.FetchDAO;
import org.example.entities.Categorie;
import org.example.entities.Produit;

/**
 * Service de la categorie. Les produits de la categorie sont
 * charges explicitement pour eviter le lazy hors session.
 */
public class CategorieService extends CrudService<Categorie, Long> {

    private final FetchDAO<Categorie, Long> fetchDao = new FetchDAO<Categorie, Long>(Categorie.class);

    public CategorieService() {
        super(Categorie.class);
    }

    /** Categorie avec sa liste de produits initialisee. */
    public Categorie getByIdAvecProduits(Long id) {
        return fetchDao.findByIdAvecCollection(id, "produits");
    }

    /** Repartition du nombre de produits par categorie. */
    public void afficherRepartition() {
        List<Categorie> categories = getAll();
        for (Categorie categorie : categories) {
            Categorie complete = getByIdAvecProduits(categorie.getId());
            System.out.println("   " + complete.getNom()
                    + " -> " + complete.getProduits().size() + " produit(s)");
        }
    }

    public void ajouterProduit(Long categorieId, String libelle, java.math.BigDecimal prix) {
        Categorie categorie = getByIdAvecProduits(categorieId);
        Produit produit = new Produit(libelle, prix, categorie);
        categorie.addProduit(produit);
        new CrudService<Produit, Long>(Produit.class).create(produit);
    }
}
