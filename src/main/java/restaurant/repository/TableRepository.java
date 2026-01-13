package restaurant.repository;

import jakarta.persistence.EntityManager;
import restaurant.model.RestaurantTable;
import restaurant.util.PersistenceManager;
import java.util.List;

public class TableRepository {
    public List<RestaurantTable> findAll() {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT t FROM RestaurantTable t", RestaurantTable.class).getResultList();
        } finally { em.close(); }
    }

    public void save(RestaurantTable table) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            if (table.getId() == null) em.persist(table);
            else em.merge(table);
            em.getTransaction().commit();
        } finally { em.close(); }
    }
}