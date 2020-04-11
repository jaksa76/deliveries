package com.zuhlke.deliveries;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class ObjectDBTest {
    @Test
    void testObjectDBIsWorking() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb:$objectdb/db/cars.odb");
        EntityManager em = emf.createEntityManager();

        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Car car = new Car();
            car.make = "Toyota";
            car.model = "Celica";
            car.color = "Gray";
            em.persist(car);
            tx.commit();
        } finally {
            em.close();
            emf.close();
        }
    }
}
