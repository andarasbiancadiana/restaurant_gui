package restaurant.repository;

import jakarta.persistence.EntityManager;
import restaurant.model.User;
import restaurant.util.PersistenceManager;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public List<User> findAll() {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<User> findByUsername(String username) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :u", User.class)
                    .setParameter("u", username)
                    .getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    public void save(User user) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            if (user.getId() == null) {
                em.persist(user);
            } else {
                em.merge(user);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(User user) {
        EntityManager em = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User toDelete = em.find(User.class, user.getId());
            if (toDelete != null) {
                em.remove(toDelete);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}