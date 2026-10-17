package org.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.example.entities.Categorie;
import org.example.entities.Produit;
import org.example.service.CategorieService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.util.HibernateUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests d'integration : on verifie la persistance reelle
 * des entites Categorie et Produit.
 */
public class ProduitDaoTest {

    private static CrudService<Categorie, Long> categorieService;
    private static CrudService<Produit, Long> produitService;
    private static CategorieService serviceCategorieAvecProduits;

    private static ProduitService serviceProduitAvance;

    @BeforeClass
    public static void initialiser() {
        categorieService = new CrudService<Categorie, Long>(Categorie.class);
        produitService = new CrudService<Produit, Long>(Produit.class);
        serviceCategorieAvecProduits = new CategorieService();
        serviceProduitAvance = new ProduitService();

        // MySQL garde les donnees d'une execution a l'autre :
        // on repart d'une base vide pour que les tests soient rejouables
        viderLesTables();
    }

    private static void viderLesTables() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            String[] tables = {"CommandeProduit", "Commande", "Produit",
                    "Categorie", "Client", "Fournisseur", "User"};
            for (int i = 0; i < tables.length; i++) {
                em.createQuery("DELETE FROM " + tables[i]).executeUpdate();
            }
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

    @AfterClass
    public static void fermer() {
        HibernateUtil.shutdown();
    }

    @Test
    public void testCreationProduitLieAUneCategorie() {
        Categorie categorie = new Categorie("Papeterie");
        categorieService.create(categorie);
        assertNotNull("L'id doit etre genere", categorie.getId());

        Produit produit = new Produit("Stylo", new BigDecimal("1.20"), categorie);
        produitService.create(produit);
        assertNotNull("Le produit doit avoir un id", produit.getId());

        Produit relu = serviceCategorieAvecProduits.getByIdAvecProduits(categorie.getId())
                .getProduits().get(0);
        assertEquals("Stylo", relu.getLibelle());
        assertEquals("Papeterie", relu.getCategorie().getNom());
    }

    @Test
    public void testPrimaireNonNullEtUnique() {
        Categorie categorie = new Categorie("Mobilier");
        categorieService.create(categorie);

        Categorie recharginee = categorieService.getById(categorie.getId());
        assertNotNull(recharginee);
        assertEquals("Mobilier", recharginee.getNom());
    }

    @Test
    public void testRechercheParIntervalleDePrix() {
        Categorie categorie = new Categorie("Loisirs");
        categorieService.create(categorie);

        produitService.create(new Produit("Ballon", new BigDecimal("15.00"), categorie));
        produitService.create(new Produit("Velo", new BigDecimal("450.00"), categorie));
        produitService.create(new Produit("Raquette", new BigDecimal("89.50"), categorie));

        List<Produit> resultats = serviceProduitAvance
                .findByPrixBetween(new BigDecimal("10.00"), new BigDecimal("100.00"));

        assertTrue("Au moins deux produits dans l'intervalle", resultats.size() >= 2);
        for (Produit produit : resultats) {
            assertTrue(produit.getPrix().compareTo(new BigDecimal("10.00")) >= 0);
            assertTrue(produit.getPrix().compareTo(new BigDecimal("100.00")) <= 0);
        }
    }

    @Test
    public void testSuppressionProduit() {
        Categorie categorie = new Categorie("Divers");
        categorieService.create(categorie);

        Produit produit = new Produit("Article temporaire", new BigDecimal("9.99"), categorie);
        produitService.create(produit);

        Long id = produit.getId();
        produitService.delete(produit);

        assertEquals("Le produit ne doit plus exister", null, produitService.getById(id));
    }
}
