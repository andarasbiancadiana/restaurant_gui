package restaurant.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import restaurant.Product;
import restaurant.util.PersistenceManager;
import java.util.List;

public class ProductRepository {

    public List<Product> findAll() {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            Thread.sleep(1000);
            return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void save(Product p) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            p.syncFromProperties();

            em.getTransaction().begin();
            if (p.getId() == null) {
                em.persist(p); // Creează rând nou
            } else {
                em.merge(p);   // Actualizează rândul existent
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Product p) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Product toDelete = em.find(Product.class, p.getId());
            if (toDelete != null) {
                em.remove(toDelete);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}