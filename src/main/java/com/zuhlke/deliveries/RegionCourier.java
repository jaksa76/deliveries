package com.zuhlke.deliveries;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class RegionCourier {
    @Id @GeneratedValue public Long id;
    public Long regionId;
    public Long courierId;

    public RegionCourier() { }

    public RegionCourier(Long regionId, Long courierId) {
        this.regionId = regionId;
        this.courierId = courierId;
    }
}
