package com.zuhlke.deliveries.couriers;

import com.zuhlke.deliveries.util.DbUtils;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class CouriersService {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb:db/couriers.odb");

    public CouriersService() {
        DbUtils.executeInTx(emf, em -> {
            Courier c = new Courier();
            c.userId = -1L;
            em.persist(c);
            em.createQuery("delete from Courier r where r.userId = -1");
        });
    }

    public Long registerAsCourier(Long userId, String description) {
        return DbUtils.retrieve(emf, em -> {
            Long count = em.createQuery("select count(c) from Courier c where c.userId = :userId", Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            if (count > 0) throw new IllegalArgumentException("User " + userId + " is already a courier");

            return DbUtils.persistInTx(em, new Courier(userId, description)).id;
        });
    }

    public List<Courier> getCouriers(List<Long> ids) {
        return DbUtils.retrieve(emf, em -> {
            return em.createQuery("select c from Courier c where c.id in :ids", Courier.class).setParameter("ids", ids).getResultList();
        });
    }

    public void deactivateCourier(Long id) {
        throw new RuntimeException("TODO");
    }

    public void wipeAllData() {
        DbUtils.executeInTx(emf, em -> {
            try {
                em.createQuery("delete from Courier c").executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
