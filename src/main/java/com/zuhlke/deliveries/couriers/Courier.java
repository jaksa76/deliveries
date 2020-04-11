package com.zuhlke.deliveries.couriers;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Courier {
    @Id @GeneratedValue Long id;
    public Long userId;
    public String description;
    public Boolean active;

    public Courier() {
    }

    public Courier(Long userId, String description) {
        this.userId = userId;
        this.description = description;
        this.active = true;
    }
}
