package restaurant.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PersistenceManager {
    // Instanța unică a clasei (Singleton)
    private static final PersistenceManager INSTANCE = new PersistenceManager();
    private final EntityManagerFactory emf;

    private PersistenceManager() {
        try {
            this.emf = Persistence.createEntityManagerFactory("RestaurantPU");
        } catch (Exception e) {
            System.err.println("Eroare critică la inițializarea JPA: " + e.getMessage());
            throw new RuntimeException("Nu s-a putut crea EntityManagerFactory", e);
        }
    }

    public static PersistenceManager getInstance() {
        return INSTANCE;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}