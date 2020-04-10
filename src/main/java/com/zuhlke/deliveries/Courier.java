package com.zuhlke.deliveries;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Courier {
    @Id @GeneratedValue Long id;
    Long userId;
    String description;
    Boolean active;

    public Courier() {
    }

    public Courier(Long userId, String description) {
        this.userId = userId;
        this.description = description;
        this.active = true;
    }
}
