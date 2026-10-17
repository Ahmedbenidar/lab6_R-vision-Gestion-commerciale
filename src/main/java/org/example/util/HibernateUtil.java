package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Point d'acces unique a la SessionFactory.
 * L'instance est construite une seule fois, au premier appel.
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory = init();

    private static SessionFactory init() {
        try {
            // hibernate.cfg.xml est lu automatiquement depuis le classpath
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable erreur) {
            System.err.println("Echec de l'initialisation de la SessionFactory : " + erreur);
            throw new ExceptionInInitializerError(erreur);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /** A appeler a l'arret de l'application pour liberer la connexion. */
    public static void shutdown() {
        getSessionFactory().close();
    }
}
