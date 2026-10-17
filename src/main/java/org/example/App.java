package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.example.dao.FetchDAO;
import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.example.entities.Client;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Fournisseur;
import org.example.entities.Produit;
import org.example.entities.User;
import org.example.service.CategorieService;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVentes;
import org.example.util.HibernateUtil;

/**
 * Programme principal du lab "Gestion commerciale".
 * Il enchaine les etapes du TP : entites, DAO, service,
 * requetes avancees, heritage, association N:N et rapport.
 */
public class App {

    private static final CategorieService CATEGORIE_SERVICE = new CategorieService();
    private static final ProduitService PRODUIT_SERVICE = new ProduitService();

    public static void main(String[] args) {
        try {
            titre("GESTION COMMERCIALE - HIBERNATE / JPA");

            // la base est persistante : on repart propre pour que le scenario
            // donne toujours exactement le meme resultat
            reinitialiserBase();

            etapeCategoriesEtProduits();
            etapeRequetesAvancees();
            etapeHeritageJoined();
            etapeCommandeAvecCleComposee();
            etapeRapportVentes();
            etapeTransactionAnnulee();

            System.out.println();
            System.out.println(">>> Fin du scenario, tout s'est deroule correctement.");
        } finally {
            HibernateUtil.shutdown();
        }
    }

    // =====================================================================
    // Mise a zero de la base (le scenario doit etre rejouable)
    // =====================================================================

    /**
     * Vide toutes les tables dans l'ordre inverse des dependances.
     * Sans cela, une seconde execution trouverait le nom "Electronique"
     * deja present et echouerait sur la contrainte unique.
     */
    private static void reinitialiserBase() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            String[] tables = {"CommandeProduit", "Commande", "Produit",
                    "Categorie", "Client", "Fournisseur", "User"};
            for (int i = 0; i < tables.length; i++) {
                em.createQuery("DELETE FROM " + tables[i]).executeUpdate();
            }

            // MySQL ne remet pas les compteurs AUTO_INCREMENT tout seul :
            // sans cette remise a zero les identifiants continueraient de grimper
            em.createNativeQuery(
                    "ALTER TABLE commande_produit AUTO_INCREMENT = 1").executeUpdate();
            em.createNativeQuery("ALTER TABLE commandes AUTO_INCREMENT = 1").executeUpdate();
            em.createNativeQuery("ALTER TABLE produits AUTO_INCREMENT = 1").executeUpdate();
            em.createNativeQuery("ALTER TABLE categories AUTO_INCREMENT = 1").executeUpdate();
            em.createNativeQuery("ALTER TABLE clients AUTO_INCREMENT = 1").executeUpdate();
            em.createNativeQuery("ALTER TABLE fournisseurs AUTO_INCREMENT = 1").executeUpdate();
            em.createNativeQuery("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();

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

    // =====================================================================
    // Etape 4 a 7 : entites Categorie / Produit et relation OneToMany
    // =====================================================================

    private static void etapeCategoriesEtProduits() {
        titre("1. Categorie, Produit et relation ManyToOne / OneToMany");

        // on enregistre d'abord les categories : Produit porte la cle etrangere
        String[] noms = {"Electronique", "Papeterie", "Informatique"};
        for (int i = 0; i < noms.length; i++) {
            Categorie categorie = new Categorie(noms[i]);
            CATEGORIE_SERVICE.create(categorie);
            System.out.println("Categorie creee -> id=" + categorie.getId() + " nom=" + categorie.getNom());
        }

        Long idElectronique = chercherIdCategorie("Electronique");
        Long idPapeterie = chercherIdCategorie("Papeterie");
        Long idInformatique = chercherIdCategorie("Informatique");

        // on passe par addProduit pour garder les deux cotes de la relation sync
        ajouterProduit("Television 55 pouces", "2499.00", idElectronique);
        ajouterProduit("Casque bluetooth", "349.90", idElectronique);
        ajouterProduit("Stylo plume", "12.50", idPapeterie);
        ajouterProduit("Cahier 200 pages", "8.75", idPapeterie);
        ajouterProduit("Ramette A4", "35.00", idPapeterie);
        ajouterProduit("Clavier mecanique", "549.00", idInformatique);
        ajouterProduit("Ecran 27 pouces", "1899.00", idInformatique);
        ajouterProduit("Souris sans fil", "199.00", idInformatique);
        ajouterProduit("Disque SSD 1 To", "899.00", idInformatique);

        System.out.println();
        System.out.println("Nombre de produits en base : " + PRODUIT_SERVICE.count());
        System.out.println("Liste triee par libelle :");
        List<Produit> tous = PRODUIT_SERVICE.findAllOrdonne();
        for (Produit produit : tous) {
            System.out.println("   - " + produit.getLibelle()
                    + " | prix=" + produit.getPrix()
                    + " | categorie=" + produit.getCategorie().getNom()
                    + " | cree le " + produit.getDateCreation());
        }

        // mise a jour puis relecture pour verifier que le merge fonctionne
        Produit aModifier = PRODUIT_SERVICE.searchByLibelle("Stylo plume").get(0);
        aModifier.setPrix(new BigDecimal("11.90"));
        PRODUIT_SERVICE.update(aModifier);
        System.out.println();
        System.out.println("Prix mis a jour pour " + aModifier.getLibelle() + " -> " + aModifier.getPrix());

        // on en profite pour supprimer un produit devenu inutile
        Produit aSupprimer = PRODUIT_SERVICE.searchByLibelle("Ramette A4").get(0);
        PRODUIT_SERVICE.delete(aSupprimer);
        System.out.println("Produit supprime : " + aSupprimer.getLibelle()
                + " (il reste " + PRODUIT_SERVICE.count() + " produits)");

        // relation inverse : une categorie connait ses produits
        System.out.println();
        System.out.println("Repartition des produits par categorie :");
        CATEGORIE_SERVICE.afficherRepartition();
    }

    // =====================================================================
    // Etape 8 et 9 : Named Queries, Criteria API, pagination
    // =====================================================================

    private static void etapeRequetesAvancees() {
        titre("2. Requetes avancees : Named Queries, Criteria API et pagination");

        Long idPapeterie = chercherIdCategorie("Papeterie");

        System.out.println("A. Named Query Produit.findByCategorie (Papeterie) :");
        List<Produit> parCategorie = PRODUIT_SERVICE.findByCategorie(idPapeterie);
        for (Produit produit : parCategorie) {
            System.out.println("   * " + produit.getLibelle() + " -> " + produit.getPrix());
        }

        System.out.println();
        System.out.println("B. Named Query Produit.searchByLibelle sur \"o\" :");
        List<Produit> parMotCle = PRODUIT_SERVICE.searchByLibelle("o");
        for (Produit produit : parMotCle) {
            System.out.println("   * " + produit.getLibelle());
        }

        System.out.println();
        System.out.println("C. Criteria API : prix entre 100 et 1000 :");
        List<Produit> parPrix = PRODUIT_SERVICE.findByPrixBetween(
                new BigDecimal("100.00"), new BigDecimal("1000.00"));
        for (Produit produit : parPrix) {
            System.out.println("   * " + produit.getLibelle() + " -> " + produit.getPrix());
        }

        System.out.println();
        System.out.println("D. Pagination JPQL + JOIN FETCH (2 produits par page) :");
        int taillePage = 2;
        Long total = PRODUIT_SERVICE.count();
        int nombrePages = (int) ((total + taillePage - 1) / taillePage);
        for (int page = 1; page <= nombrePages; page++) {
            List<Produit> tranche = PRODUIT_SERVICE.getPageAvecCategorie(page, taillePage);
            System.out.println("   Page " + page + " / " + nombrePages + " :");
            for (Produit produit : tranche) {
                System.out.println("      - " + produit.getLibelle()
                        + " | " + produit.getPrix()
                        + " | " + produit.getCategorie().getNom());
            }
        }

        System.out.println();
        System.out.println("E. Recherche multi-criteres (terme=\"e\", 100 < prix < 2000, 5 max) :");
        List<Produit> resultats = PRODUIT_SERVICE.searchAvecCategorie("e", new BigDecimal("100.00"),
                new BigDecimal("2000.00"), 1, 5);
        for (Produit produit : resultats) {
            System.out.println("   * " + produit.getLibelle() + " -> " + produit.getPrix()
                    + " (" + produit.getCategorie().getNom() + ")");
        }
    }

    // =====================================================================
    // Etape 9 : heritage JPA avec la strategie JOINED
    // =====================================================================

    private static void etapeHeritageJoined() {
        titre("3. Heritage JOINED : User / Client / Fournisseur");

        CrudService<Client, Long> clientService = new CrudService<Client, Long>(Client.class);
        CrudService<Fournisseur, Long> fournisseurService = new CrudService<Fournisseur, Long>(Fournisseur.class);
        CrudService<User, Long> userService = new CrudService<User, Long>(User.class);

        clientService.create(new Client("Youssef Alami", LocalDate.of(2026, 1, 15)));
        clientService.create(new Client("Salma Bennani", LocalDate.of(2026, 3, 4)));
        fournisseurService.create(new Fournisseur("TechDistrib", "12 rue des Orangers, Casablanca"));
        fournisseurService.create(new Fournisseur("PapeteriePro", "45 avenue Hassan II, Rabat"));

        System.out.println("Requete polymorphe sur User :");
        List<User> utilisateurs = userService.getAll();
        for (User utilisateur : utilisateurs) {
            System.out.println("   - " + utilisateur);
        }

        System.out.println();
        System.out.println("Filtre TYPE(e) = Client :");
        List<User> clients = new GenericDAO<User, Long>(User.class).findByType(Client.class);
        for (User client : clients) {
            System.out.println("   - " + client.getNom() + " (client)");
        }

        System.out.println();
        System.out.println("Filtre TYPE(e) = Fournisseur :");
        List<User> fournisseurs = new GenericDAO<User, Long>(User.class).findByType(Fournisseur.class);
        for (User fournisseur : fournisseurs) {
            System.out.println("   - " + fournisseur.getNom() + " (fournisseur : "
                    + ((Fournisseur) fournisseur).getAdresse() + ")");
        }

        System.out.println();
        System.out.println("Total utilisateurs : " + userService.count());
    }

    // =====================================================================
    // Etape 10 : association N:N via entite associative et cle composee
    // =====================================================================

    private static void etapeCommandeAvecCleComposee() {
        titre("4. Commande : N:N avec entite associative et cle composee");

        CommandeService commandeService = new CommandeService();
        FetchDAO<Commande, Long> commandeDao = new FetchDAO<Commande, Long>(Commande.class);

        // on prepare les produits et les quantites demandees
        List<Long> idsProduits = new ArrayList<Long>();
        idsProduits.add(chercherIdProduit("Clavier mecanique"));
        idsProduits.add(chercherIdProduit("Ecran 27 pouces"));
        idsProduits.add(chercherIdProduit("Disque SSD 1 To"));

        List<Integer> quantites = new ArrayList<Integer>();
        quantites.add(2);
        quantites.add(1);
        quantites.add(3);

        // tout est enregistre dans la meme transaction (produits + lignes)
        Commande commande = commandeService.enregistrer(LocalDate.now(), idsProduits, quantites);

        System.out.println("Commande enregistree -> id=" + commande.getId()
                + " date=" + commande.getDateCommande());

        Commande rechargee = commandeDao.findCommandeComplete(commande.getId());
        BigDecimal totalCommande = BigDecimal.ZERO;
        System.out.println("Detail des lignes :");
        for (CommandeProduit ligne : rechargee.getLignes()) {
            System.out.println("   - " + ligne.getProduit().getLibelle()
                    + " | quantite=" + ligne.getQuantite()
                    + " | total ligne=" + ligne.getTotalLigne());
            totalCommande = totalCommande.add(ligne.getTotalLigne());
        }
        System.out.println("Montant total de la commande : " + totalCommande);

        // la cle composee identifie chaque ligne par le couple (commande, produit)
        System.out.println("Cle de la premiere ligne : commande="
                + rechargee.getLignes().get(0).getId().getCommandeId()
                + ", produit=" + rechargee.getLignes().get(0).getId().getProduitId());
    }

    // =====================================================================
    // Etape 12 : exercice de synthese (rapport des ventes)
    // =====================================================================

    private static void etapeRapportVentes() {
        titre("5. Rapport des ventes par categorie (Criteria API)");

        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = debut.withDayOfMonth(debut.lengthOfMonth());

        RapportVentes rapportVentes = new RapportVentes();
        Map<String, BigDecimal> rapport = rapportVentes.chiffreAffairesParCategorie(debut, fin);

        if (rapport.isEmpty()) {
            System.out.println("Aucune vente sur la periode " + debut + " -> " + fin);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entree : rapport.entrySet()) {
            System.out.println("   " + entree.getKey() + " : " + entree.getValue());
            total = total.add(entree.getValue());
        }
        System.out.println("   TOTAL : " + total);
    }

    // =====================================================================
    // Etape 11 : transaction explicite et rollback
    // =====================================================================

    private static void etapeTransactionAnnulee() {
        titre("6. Transaction explicite : commit puis rollback");

        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Categorie categorie = new Categorie("Mobilier");
            em.persist(categorie);

            Produit produit = new Produit("Bureau droit", new BigDecimal("1250.00"), categorie);
            em.persist(produit);

            tx.commit();
            System.out.println("Transaction validee : produit id=" + produit.getId());
        } catch (Exception erreur) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Transaction annulee : " + erreur.getMessage());
        } finally {
            em.close();
        }

        // deuxieme essai volontairement invalide
        em = HibernateUtil.getSessionFactory().createEntityManager();
        tx = em.getTransaction();
        Long produitsAvant = PRODUIT_SERVICE.count();
        try {
            tx.begin();

            Categorie categorie = new Categorie("Test Rollback");
            em.persist(categorie);

            BigDecimal prix = new BigDecimal("-15.00");
            if (prix.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Le prix ne peut pas etre negatif");
            }
            em.persist(new Produit("Article invalide", prix, categorie));

            tx.commit();
        } catch (Exception erreur) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("Rollback effectue -> " + erreur.getMessage());
        } finally {
            em.close();
        }

        Long produitsApres = PRODUIT_SERVICE.count();
        System.out.println("Produits avant=" + produitsAvant
                + ", apres=" + produitsApres
                + " (identique, la categorie n'a pas ete enregistree)");
    }

    // =====================================================================
    // Utilitaires
    // =====================================================================

    private static void titre(String texte) {
        System.out.println();
        System.out.println("=======================================================");
        System.out.println("  " + texte);
        System.out.println("=======================================================");
    }

    private static void ajouterProduit(String libelle, String prix, Long categorieId) {
        Categorie categorie = CATEGORIE_SERVICE.getByIdAvecProduits(categorieId);
        Produit produit = new Produit(libelle, new BigDecimal(prix), categorie);
        categorie.addProduit(produit);
        PRODUIT_SERVICE.create(produit);
        System.out.println("Produit ajoute : " + libelle + " (" + prix + ") dans " + categorie.getNom());
    }

    /** Retourne l'id d'une categorie a partir de son nom. */
    private static Long chercherIdCategorie(String nom) {
        Map<String, Object> parametres = new HashMap<String, Object>();
        parametres.put("nom", nom);
        List<Categorie> trouvees = new GenericDAO<Categorie, Long>(Categorie.class)
                .findByNamedQuery("Categorie.findByNom", parametres);
        if (trouvees.isEmpty()) {
            throw new IllegalStateException("Categorie introuvable : " + nom);
        }
        return trouvees.get(0).getId();
    }

    /** Retourne l'id d'un produit a partir de son libelle. */
    private static Long chercherIdProduit(String libelle) {
        Map<String, Object> parametres = new HashMap<String, Object>();
        parametres.put("term", libelle);
        List<Produit> trouvees = new GenericDAO<Produit, Long>(Produit.class)
                .findByNamedQuery("Produit.searchByLibelle", parametres);
        for (Produit produit : trouvees) {
            if (produit.getLibelle().equalsIgnoreCase(libelle)) {
                return produit.getId();
            }
        }
        throw new IllegalStateException("Produit introuvable : " + libelle);
    }
}
