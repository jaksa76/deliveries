package com.zuhlke.deliveries.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.function.Consumer;
import java.util.function.Function;

public class DbUtils {
    public static void execute(EntityManagerFactory emf, Consumer<EntityManager> c) {
        retrieve(emf, toFunction(c));
    }

    public static <T> T retrieve(EntityManagerFactory emf, Function<EntityManager, T> f) {
        EntityManager em = emf.createEntityManager();
        try {
            return f.apply(em);
        } finally {
            em.close();
        }
    }

    public static void executeInTx(EntityManagerFactory emf, Consumer<EntityManager> c) {
        retrieveInTx(emf, toFunction(c));
    }

    public static <T> T retrieveInTx(EntityManagerFactory emf, Function<EntityManager, T> f) {
        return retrieve(emf, em -> {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                return f.apply(em);
            } catch (Exception e) {
                throw e;
            } finally {
                tx.commit();
            }
        });
    }

    public static <T> T persistInTx(EntityManager em, T entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
        } catch (Exception e) {
            throw e;
        } finally {
            tx.commit();
        }
        return entity;
    }

    private static <T> Function<EntityManager, T> toFunction(Consumer<EntityManager> f) {
        return em -> { f.accept(em); return null; };
    }
}
