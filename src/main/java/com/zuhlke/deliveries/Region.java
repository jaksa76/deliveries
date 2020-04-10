package com.zuhlke.deliveries;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Country, municipality, district or whatever...
 */
@Entity public class Region {
    @Id @GeneratedValue Long id;
    Long parentRegion;
    String name;

    public Region() { }

    public Region(Long parentRegion, String name) {
        this.parentRegion = parentRegion;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("[%d]%s in %s", id, name, parentRegion);
    }
}
