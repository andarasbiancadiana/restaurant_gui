package restaurant.repository;

import jakarta.persistence.EntityManager;
import restaurant.model.Order;
import restaurant.model.User;
import restaurant.util.PersistenceManager;
import java.util.List;

public class OrderRepository {

    // Datorită setării cascade = CascadeType.ALL din clasa Order, va salva automat și toate rândurile (OrderItem) din ea

    public void save(Order order) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            if (order.getId() == null) {
                em.persist(order); // Comandă nouă
            } else {
                em.merge(order);   // Actualizare
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // storicul personal al unui ospătar

    public List<Order> findByWaiter(User waiter) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT o FROM Order o WHERE o.waiter = :w", Order.class)
                    .setParameter("w", waiter)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Returnează toate comenzile din sistem
    public List<Order> findAll() {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT o FROM Order o", Order.class).getResultList();
        } finally {
            em.close();
        }
    }
}