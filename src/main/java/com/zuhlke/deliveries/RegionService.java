package com.zuhlke.deliveries;

import com.objectdb.Enhancer;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.stream.Collectors;

public class RegionService {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb:$objectdb/db/regions.odb");

    public RegionService() {
        DbUtils.executeInTx(emf, em -> {
            RegionCourier rc = new RegionCourier(-1L, -1L);
            em.persist(rc);
            em.createQuery("delete from RegionCourier r where r.regionId = -1 and r.courierId = -1");
        });
        DbUtils.executeInTx(emf, em -> {
            Region r = new Region();
            r.name = "_dummy";
            em.persist(r);
            em.createQuery("delete from Region r where r.name = '_dummy'");
        });
    }

    public Long createRootRegion(String name) {
        return DbUtils.execute(emf, em -> {
            Long count = em.createQuery("select count(r) from Region r where r.name = :name and r.parentRegion is null", Long.class)
                    .setParameter("name", name)
                    .getSingleResult();
            if (count > 0) throw new RuntimeException("Root region " + name + " already exists");

            return DbUtils.persistInTx(em, new Region(null, name)).id;
        });
    }

    public Long createRegion(Long parentRegion, String name) {
        return DbUtils.execute(emf, em -> {
            Long count = em.createQuery("select count(r) from Region r where r.name = :name and r.parentRegion = :parent", Long.class)
                    .setParameter("name", name)
                    .setParameter("parent", parentRegion)
                    .getSingleResult();
            if (count > 0) throw new RuntimeException("Root region " + name + " already exists in region " + parentRegion);

            return DbUtils.persistInTx(em, new Region(parentRegion, name)).id;
        });
    }

    public List<Region> getRootRegions() {
        return DbUtils.execute(emf, em -> {
            return em.createQuery("select r from Region r where r.parentRegion is null", Region.class).getResultList();
        });
    }

    public List<Region> getRegionsOf(Long parentRegionId) {
        if (parentRegionId == null) throw new IllegalArgumentException("selected region is null");
        return DbUtils.execute(emf, em -> {
            return em.createQuery("select r from Region r where r.parentRegion = :parent", Region.class)
                    .setParameter("parent", parentRegionId)
                    .getResultList();
        });
    }

    public List<Region> getAllRegions() {
        return DbUtils.execute(emf, em -> {
            return em.createQuery("select r from Region r", Region.class).getResultList();
        });
    }

    public void assignRegion(Long courierId, Long regionId) {
        if (courierId == null) throw new IllegalArgumentException("courierId must not be null");
        if (regionId == null) throw new IllegalArgumentException("regionId must not be null");
        DbUtils.execute(emf, em -> {
            Long count = em.createQuery("select count(r) from RegionCourier r where r.courierId = :courier and r.regionId = :regionId", Long.class)
                    .setParameter("regionId", regionId)
                    .setParameter("courier", courierId)
                    .getSingleResult();
            if (count > 0) return; // already there

            DbUtils.persistInTx(em, new RegionCourier(regionId, courierId));
        });
    }

    public List<Long> getCouriersIn(Long regionId) {
        return DbUtils.execute(emf, em -> {
            return em.createQuery("select rc from RegionCourier rc where rc.regionId = :regionId", RegionCourier.class)
                    .setParameter("regionId", regionId)
                    .getResultList().stream().map(rc -> rc.courierId).collect(Collectors.toList());
        });
    }

    public void wipeAllData() {
        DbUtils.executeInTx(emf, em -> {
            try {
                em.createQuery("delete from Region r").executeUpdate();
                em.createQuery("delete from RegionCourier rc").executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
