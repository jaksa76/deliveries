package com.zuhlke.deliveries.regions;

import com.zuhlke.deliveries.util.DbUtils;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegionService {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb:db/regions.odb");

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
        return DbUtils.retrieve(emf, em -> {
            Long count = em.createQuery("select count(r) from Region r where r.name = :name and r.parentRegion is null", Long.class)
                    .setParameter("name", name)
                    .getSingleResult();
            if (count > 0) throw new RuntimeException("Root region " + name + " already exists");

            return DbUtils.persistInTx(em, new Region(null, name)).id;
        });
    }

    public Long createRegion(Long parentRegion, String name) {
        return DbUtils.retrieve(emf, em -> {
            Long count = em.createQuery("select count(r) from Region r where r.name = :name and r.parentRegion = :parent", Long.class)
                    .setParameter("name", name)
                    .setParameter("parent", parentRegion)
                    .getSingleResult();
            if (count > 0) throw new RuntimeException("Root region " + name + " already exists in region " + parentRegion);

            return DbUtils.persistInTx(em, new Region(parentRegion, name)).id;
        });
    }

    public List<Region> getRootRegions() {
        return DbUtils.retrieve(emf, em -> {
            return em.createQuery("select r from Region r where r.parentRegion is null", Region.class).getResultList();
        });
    }

    public List<Region> getRegionsOf(Long parentRegionId) {
        if (parentRegionId == null) throw new IllegalArgumentException("selected region is null");
        return DbUtils.retrieve(emf, em -> {
            return em.createQuery("select r from Region r where r.parentRegion = :parent", Region.class)
                    .setParameter("parent", parentRegionId)
                    .getResultList();
        });
    }

    public List<Region> getAllRegions() {
        return DbUtils.retrieve(emf, em -> {
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

    public List<Long> getAllCouriersIn(Long regionId) {
        List<Long> couriers = new ArrayList<>();
        while (regionId != null) {
            couriers.addAll(getCouriersIn(regionId));
            regionId = getParentRegion(regionId);
        }
        return couriers;
    }

    private Long getParentRegion(Long regionId) {
        return DbUtils.retrieve(emf, em -> em.createQuery("select r from Region r where r.id = :id", Region.class)
                .setParameter("id", regionId)
                .getSingleResult()
                .parentRegion);
    }

    public List<Long> getCouriersIn(Long regionId) {
        return DbUtils.retrieve(emf, em -> {
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
